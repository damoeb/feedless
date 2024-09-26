import { Injectable } from '@angular/core';
import { ReplaySubject } from 'rxjs';
import Flexsearch from 'flexsearch';
import { AlertController } from '@ionic/angular';
import { debounce, DebouncedFunc } from 'lodash-es';
import { v4 as uuidv4 } from 'uuid';
import { Router } from '@angular/router';
import { Completion } from '@codemirror/autocomplete';
import { notebookRepository } from './notebook-repository';
import { RepositoryService } from './repository.service';
import { AuthGuardService } from '../guards/auth-guard.service';
import {
  GqlCreateRepositoriesMutation,
  GqlFullRecordByIdsQuery,
  GqlProductCategory,
  GqlVisibility,
} from '../../generated/graphql';
import { ArrayElement } from '../types';
import { RecordService } from './record.service';
import dayjs from 'dayjs';

export type Notebook = ArrayElement<
  GqlCreateRepositoriesMutation['createRepositories']
> & { lastSyncedAt: Date };

export type Note = ArrayElement<GqlFullRecordByIdsQuery['records']> & {
  references: {
    hashtags: string[];
    links: string[];
  };
  repositoryId: string;
  isUpVoted: boolean;
  upVoteAnnotationId?: string;
};

export type AppAction = {
  id: string;
  name: string;
  hint?: string;
  callback: () => void;
};

export type SearchResultGroup = {
  name: string;
  notes?: () => Promise<Note[]>;
  actions?: AppAction[];
};

const firstNoteBody = `# Start Typing here...`;
// const firstNoteBody = `
// First Note
// ====
//
// ## Headings
// # Heading level 1
// ## Heading level 2
// ### Heading level 3
// #### Heading level 4
//
//
// Heading level 1
// ===============
//
// Heading level 2
// ---------------
//
//
// ## Hashtag
//
// #stronly-typed
//
// ## Urls
// https://example.org/foo/bar.html
//
// ## Code
// At the command prompt, type \`nano\`.
//
// ## Code Block
// \`\`\`
//   println('this')
// \`\`\`
//
//
// ## Horizontal Rules
// ***
//
// ---
//
// _________________
//
// ## Bold
// I just love **bold text**.
// I just love __bold text__.
// Love**is**bold
//
// ## Italic
// Italicized text is the *cat's meow*.
// Italicized text is the _cat's meow_.
// A*cat*meow
//
// ## Blockquote
// > Dorothy followed her through many of the beautiful rooms in her castle.
//
// > Dorothy followed her through many of the beautiful rooms in her castle.
// >
// > The Witch bade her clean the pots and kettles and sweep the floor and keep the fire fed with wood.
//
// > Dorothy followed her through many of the beautiful rooms in her castle.
// >
// >> The Witch bade her clean the pots and kettles and sweep the floor and keep the fire fed with wood.
//
// ## Lists
// ### Ordered Lists
// 1. First item
// 2. Second item
// 3. Third item
//     1. Indented item
//     2. Indented item
// 4. Fourth item
//
// ### Unordered Lists
// - First item
// - Second item
// - Third item
// - Fourth item
//
// ## Inline Image
//
// ![foo](https://mdg.imgix.net/assets/images/tux.png?auto=format&fit=clip&q=40&w=100)
//
//
//     `;

type IndexDocument = {
  id: string;
  note?: Note;
  action?: AppAction;
};

@Injectable({
  providedIn: 'root',
})
export class NotebookService {
  private actions: AppAction[] = [
    {
      name: 'Close Note',
      callback: () => {},
    },
    // {
    // //     name: 'Export Notebook',
    // //     callback: () => {}
    // //   }
  ].map((action, index) => ({ id: `${index}`, ...action }));

  private readonly LIMIT = 30;

  private notebooks: Notebook[] = [];

  notebooksChanges = new ReplaySubject<Notebook[]>(1);
  openNoteChanges = new ReplaySubject<Note>(1);
  notesChanges = new ReplaySubject<void>(1);
  searchResultsChanges = new ReplaySubject<SearchResultGroup[]>(1);
  queryChanges = new ReplaySubject<string>(1);
  systemBusyChanges = new ReplaySubject<boolean>(1);
  private index: Flexsearch.Document<IndexDocument>;
  findAllAsync: DebouncedFunc<(query: string) => void>;
  private currentRepositoryId: string;

  constructor(
    private readonly alertCtrl: AlertController,
    private readonly repositoryService: RepositoryService,
    private readonly recordService: RecordService,
    private readonly authGuard: AuthGuardService,
    private readonly router: Router,
  ) {
    this.findAllAsync = debounce(this.findAllAsyncInternal, 200);
    this.init();
  }

  async createNotebook(name: string) {
    // await this.authGuard.assertLoggedIn();
    console.log('createNotebook');

    const repository = (
      await this.repositoryService.createRepositories([
        {
          title: name,
          description: '',
          product: GqlProductCategory.UntoldNotes,
          sources: [],
          withShareKey: false,
          visibility: GqlVisibility.IsPrivate,
        },
      ])
    )[0];
    const notebook: Notebook = { ...repository, lastSyncedAt: new Date() };

    notebookRepository.notebooks.add(notebook);
    this.notebooks.push(notebook);
    await new Promise((resolve) => setTimeout(resolve, 500));
    this.notebooksChanges.next(this.notebooks);
    return notebook;
  }

  async suggestByType(query: string, type: string): Promise<Completion[]> {
    switch (type) {
      case 'Hashtag':
        return [
          {
            label: 'Resolve',
            apply: () => this.findAllAsync(query),
          },
        ];
      default:
        return this.suggestNotes(query);
    }
  }

  protected findAllAsyncInternal(query: string) {
    console.log('searchAsync', query);
    this.queryChanges.next(query);
    if (query.trim()) {
      const groups: SearchResultGroup[] = this.findAll(query);
      this.searchResultsChanges.next(groups);
    } else {
      this.propagateRecentNotes();
    }
  }

  findAll(
    query: string,
    index: string[] = ['text', 'id', 'references:links', 'references:hashtags'],
  ): SearchResultGroup[] {
    if (query.trim()) {
      const results = this.index.search({
        query,
        index,
        limit: this.LIMIT,
      });
      return results.map((perField) => {
        return {
          name: perField.field,
          notes: async () =>
            notebookRepository.notes.bulkGet(
              perField.result.map((id) => `${id}`),
            ),
        };
      });
    } else {
      return [];
    }
  }

  async createNote(
    params: Partial<Pick<Note, 'title' | 'id' | 'text'>> = {},
    triggerOpen: boolean = false,
  ) {
    console.log('create note', params, triggerOpen);
    const title = params.title ?? '';
    const now = new Date();
    const note: Note = {
      id: uuidv4(),
      title: title,
      text: `# ${title}\n\n${params.text ?? ''}`.trim(),
      references: {
        hashtags: [],
        links: [],
      },
      url: '',
      isUpVoted: false,
      publishedAt: now,
      updatedAt: now,
      createdAt: now,
      // updatedAt: new Date(),
      repositoryId: this.currentRepositoryId,
    };
    notebookRepository.notes.add(note);
    this.index.add({
      id: note.id,
      note,
    });
    await this.recordService.createRecords([this.covertNoteToRecord(note)]);

    this.findAllAsync(title);
    if (triggerOpen) {
      this.openNote(note);
    }
    return note;
  }

  private covertNoteToRecord(note: Note) {
    return {
      id: note.id,
      url: '',
      title: note.title,
      text: note.text,
      publishedAt: dayjs(note.publishedAt).toDate().getTime(),
      repositoryId: {
        id: this.currentRepositoryId,
      },
    };
  }

  async openNotebook(repositoryId: string) {
    this.currentRepositoryId = repositoryId;
    console.log('openNotebook', repositoryId);
    this.systemBusyChanges.next(true);

    const notebook: Notebook =
      await notebookRepository.notebooks.get(repositoryId);

    if (notebook) {
      this.createIndex();

      const notes = await notebookRepository.notes
        .where({ repositoryId })
        .toArray();

      notes.forEach((note) => this.index.add(note));

      // sync up
      const upSyncNew = notes.filter(
        (note) => note.createdAt > notebook.lastSyncedAt,
      );
      await this.recordService.createRecords(
        upSyncNew.map((note) => this.covertNoteToRecord(note)),
      );
      const upSyncChanged = notes.filter(
        (note) => note.updatedAt > notebook.lastSyncedAt,
      );
      await Promise.all(upSyncChanged.map((note) => this.updateNote(note)));

      // sync down
      try {
        let page = 0;
        while (true) {
          const records = await this.recordService.findAllFullByRepositoryId({
            where: {
              repository: {
                id: notebook.id,
              },
              updatedAt: {
                after: notebook.lastUpdatedAt,
              },
            },
            cursor: {
              page,
            },
          });
          if (records.length == 0) {
            break;
          }
          page++;
        }
      } catch (e) {
        console.error(e.message);
      }

      notebook.lastSyncedAt = new Date();
      notebookRepository.notebooks.update(notebook.id, notebook);

      // if (notes.length === 0) {
      //   this.createNote({ title: 'First Note', text: firstNoteBody });
      // }

      // if (remoteNotebook) {
      //   remoteNotebook.notes.push(...(await localNotebook?.notes()));
      //   this.persistLocalNotebook(remoteNotebook);
      // }
      this.propagateRecentNotes();

      this.systemBusyChanges.next(false);
    } else {
      const alert = await this.alertCtrl.create({
        header: 'Notebook',
        backdropDismiss: false,
        message:
          'The requested notebook does not exist locally and cannot be fetched',
        cssClass: 'fatal-alert',
        buttons: [
          {
            role: 'cancel',
            text: 'OK',
            handler: () => this.router.navigateByUrl('/'),
          },
        ],
      });

      await alert.present();
    }
    return notebook;
  }

  private propagateRecentNotes() {
    this.searchResultsChanges.next([
      {
        name: 'Recent',
        notes: () =>
          notebookRepository.notes
            .where('repositoryId')
            .equals(this.currentRepositoryId)
            .limit(this.LIMIT)
            .sortBy('updatedAt'),
      },
    ]);
  }

  private createIndex() {
    this.index = new Flexsearch.Document<IndexDocument>({
      tokenize: 'full',
      language: 'en',
      preset: 'match',
      cache: true,
      context: true,
      document: {
        id: 'id',
        index: [
          'id',
          'action:name',
          'text',
          'isUpVoted',
          'references:links',
          'references:hashtags',
        ],
      },
    });

    this.actions.forEach((action) => {
      this.index.add({
        id: action.id,
        action,
      });
    });
  }

  async updateNote(note: Note) {
    note.title =
      note.text
        .trimStart()
        .split(/\n/g)[0]
        .replace(/^[ ]*#[ ]*/, '') ?? '';
    const reLink = /\[([^\]]+)\]/g;
    const reHashtag = /(#[^ \n\t]+)/g;

    function extractByRegExp(re: RegExp): string[] {
      let m: RegExpExecArray | null;
      const matches = [];
      while ((m = re.exec(note.text))) {
        matches.push(m[1]);
      }
      return matches;
    }

    note.updatedAt = new Date();
    note.references = {
      hashtags: extractByRegExp(reHashtag),
      links: extractByRegExp(reLink),
    };

    await this.recordService.updateRecord({
      data: {
        title: {
          set: note.title,
        },
        text: {
          set: note.text,
        },
      },
      where: {
        id: note.id,
      },
    });
    this.index.update(note);
    console.log('updateNote', note);
    notebookRepository.notes.update(note.id, note);
    this.notesChanges.next();
  }

  // private getHint(field: string, query: string, note: Note) {
  //   const fieldValue = note[field] as string;
  //   const tokens = uniq(compact(query.split(/ /g)));
  //   let highlights = `${fieldValue}`;
  //   tokens.forEach(t => highlights = highlights.replaceAll(t, `<mark>${t}</mark>`));
  //   // trim left
  //   const from = Math.max(highlights.indexOf('<mark>') - 15, 0);
  //
  //   return highlights.substring(from, highlights.length - 1).replaceAll(/\n|\t/g, ' ;;').trim() || fieldValue;
  // }

  private async suggestNotes(query: string): Promise<Completion[]> {
    if (query.length > 0) {
      const results = this.index.search({
        query,
        limit: this.LIMIT,
      });
      return Promise.all(
        results.flatMap((perField) => {
          return perField.result.map((id) =>
            notebookRepository.notes.get(id.toString()).then((note) => {
              return {
                apply: `[${note.id}]`,
                label: `${note.title}: ${note[perField.field]}`,
                // label: `${note.title}: ${this.getHint(perField.field, query, note)}`
              };
            }),
          );
        }),
      );
    } else {
      return [
        {
          label: 'New Note ID',
          apply: `[${uuidv4()}]`,
        },
      ];
    }
  }

  private async init() {
    this.notebooks = await notebookRepository.notebooks.toArray();
    await this.authGuard.assertLoggedIn();
    const remoteNotebooks = await this.repositoryService.listRepositories({
      where: {
        product: {
          eq: GqlProductCategory.UntoldNotes,
        },
      },
      cursor: {
        page: 0,
      },
    });
    console.log('remoteNotebooks', remoteNotebooks);

    const newRepositories = remoteNotebooks.filter(
      (r) => !this.notebooks.some((notebook) => notebook.id == r.id),
    );

    if (newRepositories.length > 0) {
      newRepositories.forEach((repository) =>
        notebookRepository.notebooks.add({
          ...repository,
          lastSyncedAt: new Date(0),
        }),
      );
      this.notebooks = await notebookRepository.notebooks.toArray();
    }

    this.notebooksChanges.next(this.notebooks);
  }

  existsById(noteId: string) {
    return (
      this.index.search({
        query: noteId,
        index: ['id'],
        limit: 1,
      }).length > 0
    );
  }

  async findById(noteId: string) {
    const note = this.index.search({
      query: noteId,
      index: ['id'],
      limit: 1,
    });

    if (note.length > 0) {
      return notebookRepository.notes.get(`${note[0].result[0]}`);
    }
  }

  openNote(note: Note) {
    this.openNoteChanges.next(note);
  }

  async deleteById(id: string) {
    this.index.remove(id);
    await this.recordService.removeById({
      where: {
        id: {
          eq: id,
        },
        repository: {
          id: this.currentRepositoryId,
        },
      },
    });
    notebookRepository.notes.delete(id);
    this.notesChanges.next();
  }
}
