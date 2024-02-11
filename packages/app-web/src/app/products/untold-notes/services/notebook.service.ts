import { Injectable } from '@angular/core';
import { ReplaySubject } from 'rxjs';
import Flexsearch from 'flexsearch';
import { ProfileService } from '../../../services/profile.service';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { WebDocumentService } from '../../../services/web-document.service';
import { AlertController } from '@ionic/angular';
import { debounce, DebouncedFunc, uniq } from 'lodash-es';
import { v4 as uuidv4 } from 'uuid';
import { Router } from '@angular/router';
import dayjs from 'dayjs';
import { Completion } from '@codemirror/autocomplete';
import { notebookRepository } from './notebook-repository';
import { UntoldNotesProductModule } from '../untold-notes-product.module';

export interface Notebook {
  id: string;
  name: string;
  lastSyncAt: Date | null;
};

export interface Note {
  id: string;
  name: string;
  text: string;
  createdAt: Date;
  updatedAt: Date;
  metadata?: string;
  fileType?: string;
  notebookId: string;
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

const firstNoteBody = `
First Note
====

## Headings
# Heading level 1
## Heading level 2
### Heading level 3
#### Heading level 4


Heading level 1
===============

Heading level 2
---------------


## Hashtag

#stronly-typed

## Urls
https://example.org/foo/bar.html

## Code
At the command prompt, type \`nano\`.

## Code Block
\`\`\`
  println('this')
\`\`\`


## Horizontal Rules
***

---

_________________

## Bold
I just love **bold text**.
I just love __bold text__.
Love**is**bold

## Italic
Italicized text is the *cat's meow*.
Italicized text is the _cat's meow_.
A*cat*meow

## Blockquote
> Dorothy followed her through many of the beautiful rooms in her castle.

> Dorothy followed her through many of the beautiful rooms in her castle.
>
> The Witch bade her clean the pots and kettles and sweep the floor and keep the fire fed with wood.

> Dorothy followed her through many of the beautiful rooms in her castle.
>
>> The Witch bade her clean the pots and kettles and sweep the floor and keep the fire fed with wood.

## Lists
### Ordered Lists
1. First item
2. Second item
3. Third item
    1. Indented item
    2. Indented item
4. Fourth item

### Unordered Lists
- First item
- Second item
- Third item
- Fourth item

## Inline Image

![foo](https://mdg.imgix.net/assets/images/tux.png?auto=format&fit=clip&q=40&w=100)


    `;

type IndexDocument = {
  id: string;
  note?: Note;
  action?: AppAction;
};

@Injectable()
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
  notesChanges = new ReplaySubject<SearchResultGroup[]>(1);
  queryChanges = new ReplaySubject<string>(1);
  systemBusyChanges = new ReplaySubject<boolean>(1);
  private index: Flexsearch.Document<IndexDocument>;
  searchAsync: DebouncedFunc<(query: string) => void>;
  private currentNotebookId: string;

  constructor(
    private readonly profileService: ProfileService,
    private readonly alertCtrl: AlertController,
    private readonly router: Router,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly webDocumentService: WebDocumentService,
  ) {
    this.searchAsync = debounce(this.searchAsyncInternal, 400);
    this.init();
  }

  async createNotebook(name: string) {
    console.log('createNotebook');
    const notebook: Notebook = {
      id: uuidv4(),
      name,
      lastSyncAt: null,
    };

    notebookRepository.notebooks.add(notebook);
    this.notebooks.push(notebook);
    await new Promise((resolve) => setTimeout(resolve, 500));
    this.notebooksChanges.next(this.notebooks);
    return notebook;
  }

  async suggestByType(query: string, type: string): Promise<Completion[]> {
    console.log('query', query, type);
    switch (type) {
      case 'Hashtag':
        return [
          {
            label: 'Resolve',
            apply: () => console.log(query),
          },
        ];
      default:
        return this.suggestNotes(query);
    }
  }

  protected searchAsyncInternal(query: string) {
    console.log('searchAsync', query);
    this.queryChanges.next(query);
    if (query.trim()) {
      const results = this.index.search({
        query,
        index: ['note:name', 'note:text'],
        limit: this.LIMIT,
      });
      console.log(results);
      const groups: SearchResultGroup[] = results.map((perField) => {
        return {
          name: perField.field,
          notes: () => notebookRepository.notes.bulkGet(perField.result.map((id) => `${id}`)),
        };
      });
      this.notesChanges.next(groups);
    } else {
      this.propagateRecentNotes();
    }
  }

  createNote(name: string, text: string = '') {
    console.log('create note');
    const note: Note = {
      id: uuidv4(),
      name: this.createNoteId(),
      text: `# ${name}\n\n${text}`,
      createdAt: new Date(),
      updatedAt: new Date(),
      notebookId: this.currentNotebookId
    };
    notebookRepository.notes.add(note);
    this.index.add({
      id: note.id,
      note,
    });
    this.searchAsync(name);
    return note;
  }

  async openNotebook(notebookId: string) {
    this.currentNotebookId = notebookId;
    console.log('openNotebook', notebookId);
    this.systemBusyChanges.next(true);

    const localNotebook: Notebook = await notebookRepository.notebooks.get(notebookId);

    if (localNotebook) {
      this.createIndex();

      const notes = await notebookRepository.notes.where({notebookId}).toArray();
      notes.forEach((note) =>
        this.index.add({
          id: note.id,
          note,
        }),
      );

      if (notes.length === 0) {
        this.createNote('First Note', firstNoteBody);
      }

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
  }

  private propagateRecentNotes() {
    this.notesChanges.next([
      {
        name: 'Recent',
        notes: () => notebookRepository.notes
          .where('notebookId').equals(this.currentNotebookId)
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
        index: ['id', 'action:name', 'note:name', 'note:text'],
      },
    });

    this.actions.forEach((action) => {
      this.index.add({
        id: action.id,
        action,
      });
    });
  }

  updateNote(note: Note) {
    notebookRepository.notes.update(note.id, note)
  }

  // private async loadRemoteNotebook(
  //   notebookId: string,
  //   lastSyncAt: Date,
  // ): Promise<Notebook> {
  //   if (this.profileService.isAuthenticated()) {
  //     // sync or create
  //     const sourceSubscription =
  //       await this.sourceSubscriptionService.getSubscriptionById(notebookId);
  //
  //     return {
  //       id: sourceSubscription.id,
  //       name: sourceSubscription.title,
  //       lastSyncAt: new Date(),
  //       notes: () => this.loadRemoteNotes(notebookId, lastSyncAt),
  //     };
  //   }
  // }

  // private async loadRemoteNotes(
  //   notebookId: string,
  //   since: Date,
  // ): Promise<Note[]> {
  //   // this.webDocumentService.findAllByStreamId()
  //   return [];
  // }

  // private async persistRemoteNotebook(notebook: Notebook) {
  //   if (this.profileService.isAuthenticated()) {
  //     const sub = await this.sourceSubscriptionService.createSubscriptions({
  //       subscriptions: [
  //         {
  //           sources: [],
  //           product: environment.product(),
  //           sinkOptions: {
  //             title: notebook.name,
  //             description: '',
  //             visibility: GqlVisibility.IsPrivate,
  //           },
  //         },
  //       ],
  //     }).then(response => response[0]);
  //
  //     // todo remove notebook by id
  //   }
  // }

  private getHint(field: string, query: string, note: Note) {
    const fieldValue = note[field];
    const index = fieldValue.indexOf(query);
    if (index > -1) {
      const from = Math.max(index - 10, 0);
      const to = Math.min(query.length + index + 10, fieldValue.length);
      return fieldValue.substring(from, to);
    } else {
      return fieldValue;
    }
  }

  private async suggestNotes(query: string): Promise<Completion[]> {
    if (query.length > 0) {
      const results = this.index.search({
        query,
        limit: this.LIMIT,
      });
      return Promise.all(results.flatMap((perField) => {
          return perField.result
            .map((id) => notebookRepository.notes.get(id.toString()).then(note => ({
                apply: `[${note.name}]`,
                label: this.getHint(perField.field, query, note),
              }))
            )
        }));
    } else {
      return [
        {
          label: 'New Note ID',
          apply: `[${this.createNoteId()}]`,
        },
      ];
    }
  }

  private createNoteId(): string {
    return `${dayjs().format('YYYY-MM')}/${uuidv4().split('-')[0]}`;
  }

  private async init() {
    this.notebooks = await notebookRepository.notebooks.toArray();
    this.notebooksChanges.next(this.notebooks);
  }
}
