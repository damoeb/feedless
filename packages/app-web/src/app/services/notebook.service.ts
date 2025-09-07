import { inject, Injectable } from '@angular/core';
import { Observable, ReplaySubject } from 'rxjs';
import { Document } from 'flexsearch';
import { AlertController } from '@ionic/angular/standalone';
import { uniq } from 'lodash-es';
import { v4 as uuidv4 } from 'uuid';
import { Router } from '@angular/router';
import { Completion } from '@codemirror/autocomplete';
import { notebookRepository } from './notebook-repository';
import { RepositoryService } from './repository.service';
import { AuthGuardService } from '../guards/auth-guard.service';
import {
  GqlCreateRecordInput,
  GqlCreateRepositoriesMutation,
  GqlFullRecordByIdsQuery,
  GqlVertical,
  GqlVisibility,
} from '../../generated/graphql';
import { ArrayElement, isNonNull } from '../types';
import { RecordService } from './record.service';
import dayjs from 'dayjs';
import { isNullish } from '@apollo/client/cache/inmemory/helpers';
import { liveQuery } from 'dexie';

export type CreateNoteParams = Partial<
  Pick<Note, 'title' | 'id' | 'text' | 'parent'>
>;

export type Notebook = ArrayElement<
  GqlCreateRepositoriesMutation['createRepositories']
> & { lastSyncedAt: Date };

export type Note = ArrayElement<GqlFullRecordByIdsQuery['records']> & {
  references: {
    hashtags: string[];
    links: string[];
  };
  parent?: string | undefined;
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

export type NotebookSettings = {};

export const defaultSettings: NotebookSettings = {
  extends: 'https://foo.bar/untold-dark.json',
  storage: {
    notesDirectory: './Notes',
    extension: 'md',
  },
  editing: {
    useMarkdown: true,
    newNoteTemplate: '',
  },
  saving: {
    mode: 'auto',
    autoSaveInterval: 60,
    manualSaveWithReview: false,
  },
  general: {
    fontName: 'Menlo',
    fontSize: 13,
    theme: 'default',
    startupNote: 'Scratchpad',
    language: 'en',
  },
  search: {
    showNoteTree: true,
    sortOrder: 'modified',
    searchType: 'fuzzy',
    maxResults: 20,
    autocomplete: {
      labels: true,
      actions: true,
    },
  },
  hotkeys: {
    createNewNote: 'Cmd+N',
    searchNotes: 'Cmd+F',
  },
};

export type NoteId = string;

// | 'isUpVoted'
// | 'references:links'
// | 'references:hashtags'

type NoteDocument = {
  id: string;
  parent: string;
  note: Note;
  // action?: AppAction;
};

export enum ChangeModifier {
  remove,
  update,
  create,
}

export type Tuple<A, B> = { a: A; b: B };

export type NoteIndex = 'id' | 'note:parent';

@Injectable({
  providedIn: 'root',
})
export class NotebookService {
  private readonly alertCtrl = inject(AlertController);
  private readonly repositoryService = inject(RepositoryService);
  private readonly recordService = inject(RecordService);
  private readonly authGuard = inject(AuthGuardService);
  private readonly router = inject(Router);

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

  private readonly LIMIT = 20;

  private notebooks: Notebook[] = [];

  notebooksChanges = new ReplaySubject<Notebook[]>(1);
  openNoteChanges = new ReplaySubject<Note>(1);
  closeNoteChanges = new ReplaySubject<Note>(1);
  notesChanges = new ReplaySubject<Tuple<NoteId, ChangeModifier>>(1);
  systemBusyChanges = new ReplaySubject<boolean>(1);
  private index!: Document<NoteDocument>;
  private currentRepositoryId!: string;

  constructor() {
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
          product: GqlVertical.UntoldNotes,
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
    // switch (type) {
    //   case 'Hashtag':
    //     return [
    //       {
    //         label: 'Resolve',
    //         apply: () => this.findAllAsync(query),
    //       },
    //     ];
    //   default:
    return this.suggestNotes(query);
    // }
  }

  async findAll(
    query: string,
    index: NoteIndex[] = [],
    limit: number = 200,
  ): Promise<Note[]> {
    try {
      const results = this.index.search(query, {
        limit: 10,
        index: index,
        suggest: true,
        highlight: '<b>$1</b>',
      });

      const ids = uniq(results.flatMap((result) => result.result));
      return notebookRepository.notes
        .bulkGet(ids.map((id) => `${id}`))
        .then((notes) => notes.filter(isNonNull));
    } catch (e) {
      console.error(e);
      return [];
    }
  }

  async createNote(
    params: CreateNoteParams = {},
    triggerOpen: boolean = false,
  ) {
    console.log('create note params', params);
    const title = params.title ?? '';
    const now = new Date();
    const note: Note = {
      id: params.id ?? uuidv4(),
      title: title,
      text: `# ${title}\n\n${params.text ?? ''}`.trim(),
      references: {
        hashtags: [],
        links: [],
      },
      parent: params.parent,
      url: '',
      isUpVoted: false,
      publishedAt: now,
      updatedAt: now,
      createdAt: now,
      // updatedAt: new Date(),
      repositoryId: this.currentRepositoryId,
    };

    notebookRepository.notes.add(note);
    this.index.add(this.toIndexDocument(note));

    console.log('new note', note);

    this.notesChanges.next({ a: note.id, b: ChangeModifier.create });
    // await this.recordService.createRecords([this.covertNoteToRecord(note)]);

    // this.findAllAsync(title);
    if (triggerOpen) {
      this.openNote(note);
    }
    return note;
  }

  private covertNoteToRecord(note: Note): GqlCreateRecordInput {
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

      notes.forEach((note) => this.index.add(this.toIndexDocument(note)));
      console.log('Notes indexed');

      // sync up
      const upSyncNew = notes.filter(
        (note) => note.createdAt > notebook.lastSyncedAt,
      );
      // await this.recordService.createRecords(
      //   upSyncNew.map((note) => this.covertNoteToRecord(note)),
      // );
      // todo activate
      // const upSyncChanged = notes.filter(
      //   (note) => note.updatedAt > notebook.lastSyncedAt,
      // );
      // await Promise.all(upSyncChanged.map((note) => this.updateNote(note)));

      // sync down
      // try {
      //   let page = 0;
      //   while (true) {
      //     const records = await this.recordService.findAllFullByRepositoryId({
      //       where: {
      //         repository: {
      //           id: notebook.id,
      //         },
      //         updatedAt: {
      //           after: notebook.lastUpdatedAt,
      //         },
      //       },
      //       cursor: {
      //         page,
      //       },
      //     });
      //     if (records.length == 0) {
      //       break;
      //     }
      //     page++;
      //   }
      // } catch (e) {
      //   console.error(e);
      // }

      notebook.lastSyncedAt = new Date();
      notebookRepository.notebooks.update(notebook.id, notebook);

      // if (notes.length === 0) {
      //   this.createNote({ title: 'First Note', text: firstNoteBody });
      // }

      // if (remoteNotebook) {
      //   remoteNotebook.notes.push(...(await localNotebook?.notes()));
      //   this.persistLocalNotebook(remoteNotebook);
      // }
      // this.propagateRecentNotes();

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

  // private propagateRecentNotes() {
  //   this.searchResultsChanges.next([
  //     {
  //       name: 'Recent',
  //       notes: () =>
  //         notebookRepository.notes
  //           .where('repositoryId')
  //           .equals(this.currentRepositoryId)
  //           .limit(this.LIMIT)
  //           .sortBy('updatedAt'),
  //     },
  //   ]);
  // }

  private createIndex() {
    this.index = new Document<NoteDocument>({
      cache: false,
      store: true,
      document: {
        id: 'id',
        index: [
          { field: 'id', tokenize: 'strict' },
          { field: 'note:parent', tokenize: 'strict' },
          { field: 'note:text', tokenize: 'full' },
          { field: 'note:title', tokenize: 'full' },
          // 'level',
          // 'isUpVoted',
          // 'references:links',
          // 'references:hashtags',
        ],
      },
    });
    console.log('Created index');

    // this.actions.forEach((action) => {
    //   this.index.add({
    //     id: action.id,
    //     action,
    //   });
    // });
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

    // await this.recordService.updateRecord({
    //   data: {
    //     title: {
    //       set: note.title,
    //     },
    //     text: {
    //       set: note.text,
    //     },
    //   },
    //   where: {
    //     id: note.id,
    //   },
    // });
    this.index.update(this.toIndexDocument(note));
    console.log('updateNote', note);
    await notebookRepository.notes.update(note.id, note);
    this.notesChanges.next({ a: note.id, b: ChangeModifier.update });
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
              if (note) {
                return {
                  apply: `[${note.id}]`,
                  // @ts-ignore
                  label: `${note.title}: ${note[perField.field]}`,
                  // label: `${note.title}: ${this.getHint(perField.field, query, note)}`
                };
              }
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
          eq: GqlVertical.UntoldNotes,
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
      // todo use this return notebookRepository.notes.get(noteId).then(isDefined)
      this.index.search({
        query: noteId,
        index: ['id'],
        limit: 1,
      }).length > 0
    );
  }

  findById(noteId: string) {
    const resultGroups = this.index.search({
      query: noteId,
      index: ['id'],
      limit: 1,
    });

    if (resultGroups.length > 0) {
      return this.getById(`${resultGroups[0].result[0]}`);
    }
  }

  getById(noteId: string) {
    return notebookRepository.notes.get(noteId);
  }

  async openNoteById(noteId: string) {
    const note = await notebookRepository.notes.get(`${noteId}`);
    this.openNote(note);
  }

  openNote(note: Note) {
    this.openNoteChanges.next(note);
  }

  async deleteById(id: string) {
    // todo check if children exist
    const note = await notebookRepository.notes.get(id);

    this.index.remove(id);
    await notebookRepository.notes.delete(id);
    // await this.recordService.removeById({
    //   where: {
    //     id: {
    //       eq: id,
    //     },
    //     repository: {
    //       id: this.currentRepositoryId,
    //     },
    //   },
    // });
    this.notesChanges.next({ a: id, b: ChangeModifier.remove });

    if (note.parent) {
      const parent = await notebookRepository.notes.get(note.parent);
      await this.updateNote(parent);
    }
  }

  deleteAll() {
    // debugger;
    notebookRepository.notes.clear();
  }

  async findAllRoots(): Promise<Note[]> {
    return notebookRepository.notes
      .filter((note) => isNullish(note.parent))
      .toArray();
  }

  private toIndexDocument(note: Note): NoteDocument {
    return {
      id: note.id,
      parent: note.parent,
      note,
    };
  }

  findAllChildren(id: string): Observable<Note[]> {
    return convertToRx<Note[]>(
      liveQuery(() =>
        notebookRepository.notes
          .where('parent')
          .equals(id)
          .limit(100)
          .toArray(),
      ),
    );
  }

  countChildren(id: string): Observable<number> {
    return convertToRx<number>(
      liveQuery(() =>
        notebookRepository.notes.where('parent').equals(id).count(),
      ),
    );
  }

  async openSettingsNote() {
    const settingsQuery = notebookRepository.notes
      .where('title')
      .equals('notebook.json');
    const exists = await settingsQuery.count().then((count) => count === 1);
    if (exists) {
      console.log('Open settings');
      settingsQuery
        .limit(1)
        .toArray()
        .then((settings) => this.openNote(settings[0]));
    } else {
      await this.createSettingsNote();
    }
  }

  private createSettingsNote() {
    return this.createNote(
      {
        title: 'notebook.json',
        text: JSON.stringify(defaultSettings, null, 2),
      },
      true,
    );
  }
}

function convertToRx<T>(customObservable: any): Observable<T> {
  return new Observable<T>((subscriber) => {
    const unsubscribe = customObservable.subscribe((value: any) => {
      subscriber.next(value);
    });

    // Return cleanup logic
    return () => {
      unsubscribe();
    };
  });
}
