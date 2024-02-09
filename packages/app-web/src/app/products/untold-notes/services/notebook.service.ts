import { Injectable } from '@angular/core';
import { ReplaySubject } from 'rxjs';
import Flexsearch from 'flexsearch';
import { ProfileService } from '../../../services/profile.service';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { WebDocumentService } from '../../../services/web-document.service';
import { AlertController } from '@ionic/angular';
import { debounce, DebouncedFunc, uniq, uniqBy } from 'lodash-es';
import { v4 as uuidv4 } from 'uuid';
import { GqlVisibility } from '../../../../generated/graphql';
import { Router } from '@angular/router';
import dayjs from 'dayjs';
import { Completion } from '@codemirror/autocomplete';
import { environment } from '../../../../environments/environment';

export type NotebookHead = {
  id: string;
  name: string;
  // lastOpened: Date
};

export type Notebook = {
  head: NotebookHead;
  lastSyncAt: Date | null;
  notes: Note[];
};

export type Note = {
  id: string;
  name: string;
  text: string;
  createdAt?: Date;
  updatedAt?: Date;
  metadata?: string;
  fileType?: string;
};

export type AppAction = {
  id: string;
  name: string;
  hint?: string;
  callback: () => void;
};
export type SearchResultGroup = {
  name: string;
  notes?: Note[];
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

  private notebooks: NotebookHead[] = [];
  private store: Note[] = [];

  notebooksChanges = new ReplaySubject<NotebookHead[]>(1);
  notesChanges = new ReplaySubject<SearchResultGroup[]>(1);
  openedNoteChanges = new ReplaySubject<Note>(1);
  queryChanges = new ReplaySubject<string>(1);
  systemBusyChanges = new ReplaySubject<boolean>(1);
  focusSearchbar = new ReplaySubject<void>(1);
  private index: Flexsearch.Document<IndexDocument>;
  searchAsync: DebouncedFunc<(query: string) => void>;

  constructor(
    private readonly profileService: ProfileService,
    private readonly alertCtrl: AlertController,
    private readonly router: Router,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly webDocumentService: WebDocumentService,
  ) {
    this.notebooks = this.loadLocalNotebooks();
    this.notebooksChanges.next(this.notebooks);
    this.searchAsync = debounce(this.searchAsyncInternal, 400);
  }

  async createNotebook(name: string) {
    console.log('createNotebook');
    const head: NotebookHead = {
      id: uuidv4(),
      name,
    };
    const notebook: Notebook = {
      head,
      lastSyncAt: null,
      notes: [],
    };

    this.persistLocalNotebook(notebook);
    await this.persistRemoteNotebook(notebook);
    this.notebooks.push(head);
    await new Promise((resolve) => setTimeout(resolve, 500));
    this.notebooksChanges.next(this.notebooks);
    return notebook;
  }

  suggestByType(query: string, type: string): Completion[] {
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
          notes: perField.result.map((id) =>
            this.store.find((note) => note.id == id),
          ),
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
    };
    this.store.push(note);
    this.index.add({
      id: note.id,
      note,
    });
    // this.searchAsync(name);
    return note;
  }

  async openNotebook(notebookId: string) {
    console.log('openNotebook', notebookId);
    this.systemBusyChanges.next(true);

    const localNotebook: Notebook = this.loadLocalNotebook(notebookId);
    const remoteNotebook = await this.loadRemoteNotebook(
      notebookId,
      localNotebook?.lastSyncAt,
    );

    if (localNotebook || remoteNotebook) {
      this.createIndex();

      const notes = [
        ...(localNotebook?.notes ?? []),
        ...(remoteNotebook?.notes ?? []),
      ];

      notes.forEach((note) =>
        this.index.add({
          id: note.id,
          note,
        }),
      );
      this.store = [];
      this.store.push(...notes);

      if (notes.length === 0) {
        this.createNote('First Note', firstNoteBody);
      }

      if (remoteNotebook) {
        remoteNotebook.notes.push(...(localNotebook?.notes ?? []));
        this.persistLocalNotebook(remoteNotebook);
      }
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
        notes: this.store.filter((_, index) => index < this.LIMIT),
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

  private persistLocalNotebook(notebook: Notebook) {
    const headIndex = this.loadLocalNotebooks();
    headIndex.push(notebook.head);
    localStorage.setItem(
      `unless-notebook-index`,
      JSON.stringify(uniqBy(headIndex, 'head.id')),
    );
    localStorage.setItem(
      `unless-notebook-${notebook.head.id}`,
      JSON.stringify(notebook),
    );
  }

  private loadLocalNotebooks(): NotebookHead[] {
    const persistedHeadIndex = localStorage.getItem(`unless-notebook-index`);
    return persistedHeadIndex ? JSON.parse(persistedHeadIndex) : [];
  }

  private syncWithRemote() {
    // todo
    // this.sourceSubscriptionService.listSourceSubscriptions({
    //   cursor: {
    //     page: 0
    //   }
    // })
    this.webDocumentService.findAllByStreamId({
      cursor: {
        page: 0,
      },
      where: {
        sourceSubscription: {
          where: {
            id: '',
          },
        },
        updatedAt: {
          after: {
            value: '',
          },
        },
      },
    });
  }

  updateNode(openedNote: Note) {}

  private loadLocalNotebook(notebookId: string): Notebook {
    const data = localStorage.getItem(`unless-notebook-${notebookId}`);
    if (data) {
      return JSON.parse(data);
    }
  }

  private async loadRemoteNotebook(
    notebookId: string,
    lastSyncAt: Date,
  ): Promise<Notebook> {
    if (this.profileService.isAuthenticated()) {
      // sync or create
      const sourceSubscription =
        await this.sourceSubscriptionService.getSubscriptionById(notebookId);

      return {
        head: {
          id: sourceSubscription.id,
          name: sourceSubscription.title,
        },
        lastSyncAt: new Date(),
        notes: await this.loadRemoteNotes(notebookId, lastSyncAt),
      };
    }
  }

  private async loadRemoteNotes(
    notebookId: string,
    since: Date,
  ): Promise<Note[]> {
    // this.webDocumentService.findAllByStreamId()
    return [];
  }

  private async persistRemoteNotebook(notebook: Notebook) {
    if (this.profileService.isAuthenticated()) {
      await this.sourceSubscriptionService.createSubscriptions({
        subscriptions: [
          {
            sources: [],
            product: environment.product(),
            sinkOptions: {
              title: notebook.head.name,
              description: '',
              visibility: GqlVisibility.IsPrivate,
            },
          },
        ],
      });
    }
  }

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

  private suggestNotes(query: string) {
    if (query.length > 0) {
      const results = this.index.search({
        query,
        limit: this.LIMIT,
      });
      console.log(results);
      return uniq(
        results.flatMap((perField) => {
          return perField.result
            .map((id) => this.store.find((note) => note.id == id))
            .map((note) => ({
              apply: `[${note.name}]`,
              label: this.getHint(perField.field, query, note),
            }));
        }),
      );
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
}
