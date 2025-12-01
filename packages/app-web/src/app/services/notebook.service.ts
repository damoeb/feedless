import { inject, Injectable } from '@angular/core';
import { firstValueFrom, forkJoin, map, Observable, of, OperatorFunction, ReplaySubject, switchMap, zip, } from 'rxjs';
import { Document } from 'flexsearch';
import { AlertController, ToastController } from '@ionic/angular/standalone';
import { get, map as mapObj, orderBy, slice, uniq } from 'lodash-es';
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
import { ArrayElement, isNonNull, NestedKeys, TypeAtPath } from '../types';
import { RecordService } from './record.service';
import dayjs from 'dayjs';
import { isNullish } from '@apollo/client/cache/inmemory/helpers';
import { liveQuery, Observable as DexieObservable } from 'dexie';
import { EditorView } from '@codemirror/view';
import { translations } from '../products/untold-notes/untold-notes-product.translations';
import { ServerConfigService } from './server-config.service';
import { createNoteHandleId } from '../pages/notebook-details/notebook-details.page';

export type CreateNoteParams = Partial<Pick<Note, 'title' | 'customId' | 'text' | 'parent'>>;

export type Notebook = ArrayElement<GqlCreateRepositoriesMutation['createRepositories']> & {
  lastSyncedAt: Date;
  offline: boolean;
};

export enum NotebookActionId {
  deleteNote = 'deleteNote',
  cloneNote = 'cloneNote',
  followupNote = 'followupNote',
  moveNote = 'moveNote',
  pinNote = 'pinNote',
  attachFile = 'attachFileToNote',
}

export type NotebookAction = {
  id: NotebookActionId;
  label: string;
};

export type NoteHandle = {
  body: Note;
  body$: () => Observable<Note>;
  expanded: boolean;
  disabled: boolean;
  level: number;
  childrenCount: () => Observable<number>;
  scrollTo: (event: MouseEvent) => void;
  children: () => Observable<NoteHandle[]>;
  toggleUpvote: () => void;
  incomingLinks$: () => Observable<NoteHandle[]>;
  outgoingLinks$: () => Observable<NoteHandle[]>;
};

export type Hashtag = string;

export type Note = ArrayElement<GqlFullRecordByIdsQuery['records']> & {
  references: {
    hashtags: Hashtag[];
    links: string[];
  };
  pos?: number; // todo allow sorting
  parent?: string | undefined;
  customId: string;
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

export type UntoldLanguages = 'en' | 'de';

export type NoteShortcutType = 'recent' | 'off' | 'pinned';

export interface NotebookSettings {
  // extends: 'https://foo.bar/untold-dark.json',
  // storage: {
  //   notesDirectory: './Notes',
  //   extension: 'md',
  // },
  shortcuts: {
    limit: number;
    type: NoteShortcutType;
  };
  general: {
    darkMode: boolean;
    fontName: string;
    fontSize: number;
    textAlignment: 'left' | 'right';
    hideInternalNotes: boolean;
    //   theme: 'default',
    startupNote: 'new' | 'blank' | 'lastChanged' | 'index' | 'none';
    language: UntoldLanguages;
  };
  editor: {
    //   useMarkdown: true,
    //   wysiwyg: true,
    noteIdDatePattern: string;
    useDefaultTemplates: boolean;
    customNoteTemplates: {
      [templateName: string]: string;
    };
    style: 'one-document' | 'fields';
  };
  // saving: {
  //   mode: 'auto',
  //   autoSaveInterval: 60,
  //   manualSaveWithReview: false,
  // },
  // search: {
  //   showNoteTree: true,
  //   sortOrder: 'modified',
  //   searchType: 'fuzzy',
  //   maxResults: 20,
  //   autocomplete: {
  //     labels: true,
  //     actions: true,
  //   },
  //      history: {
  //        maxSize: number
  //      }
  // },
  // hotkeys: {
  //   createNewNote: 'Cmd+N',
  //   searchNotes: 'Cmd+F',
  // },
  css?: {
    dark: {
      variables: {
        [n: string]: string;
      };
    };
    light: {
      variables: {
        [n: string]: string;
      };
    };
  };
}

export const defaultSettings: NotebookSettings = {
  shortcuts: {
    limit: 5,
    type: 'pinned',
  },
  general: {
    darkMode: false,
    fontName: 'serif',
    textAlignment: 'left',
    fontSize: 13,
    hideInternalNotes: true,
    startupNote: 'index',
    language: 'en',
  },
  editor: {
    noteIdDatePattern: 'YYYYMMDDHHmm',
    useDefaultTemplates: true,
    customNoteTemplates: {},
    style: 'one-document',
  },
};

type NoteHandleProvider = (note: Note) => NoteHandle;

export type NoteId = string;

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

@Injectable({
  providedIn: 'root',
})
export class NotebookService {
  private readonly alertCtrl = inject(AlertController);
  private readonly repositoryService = inject(RepositoryService);
  private readonly recordService = inject(RecordService);
  private readonly authGuard = inject(AuthGuardService);
  private readonly router = inject(Router);
  private readonly toastCtrl = inject(ToastController);
  private readonly serverConfig = inject(ServerConfigService);

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

  moveStartChanges = new ReplaySubject<NoteId>(1);
  moveEndChanges = new ReplaySubject<NoteId>(1);
  notebooksChanges = new ReplaySubject<Notebook[]>(1);
  openNoteChanges = new ReplaySubject<NoteHandle>(1);
  closeNoteChanges = new ReplaySubject<Note>(1);
  notesChanges = new ReplaySubject<Tuple<NoteId, ChangeModifier>>(1);
  systemBusyChanges = new ReplaySubject<boolean>(1);
  private index!: Document<NoteDocument>;
  private currentRepositoryId!: string;
  private notebookJsonName: string = 'notebook.json';

  constructor() {
    this.init();
    this.filterInternalNotes = this.filterInternalNotes.bind(this);
  }

  private async withOnline<T>(online: () => Promise<T>, offline: () => Promise<T>): Promise<T> {
    try {
      if (this.serverConfig.isConnected()) {
        return await online();
      }
    } catch (e) {
      // fall through to offline
      console.warn('Online call failed, falling back to offline handler', e);
    }
    return offline();
  }

  async createNotebook(name: string) {
    // await this.authGuard.assertLoggedIn();
    console.log('createNotebook');

    const repository = await this.withOnline(
      () =>
        this.repositoryService
          .createRepositories([
            {
              title: name,
              description: '',
              product: GqlVertical.UntoldNotes,
              sources: [],
              withShareKey: false,
              visibility: GqlVisibility.IsPrivate,
            },
          ])
          .then((r) => r[0]),
      async () =>
        ({
          id: uuidv4(),
          title: name,
          description: '',
          product: GqlVertical.UntoldNotes,
          sources: [],
          withShareKey: false,
          visibility: GqlVisibility.IsPrivate,
          lastUpdatedAt: new Date(0),
        }) as any
    );
    const notebook: Notebook = { ...repository, lastSyncedAt: new Date() };

    notebookRepository.notebooks.add(notebook);
    this.notebooks.push(notebook);
    await new Promise((resolve) => setTimeout(resolve, 500));
    this.notebooksChanges.next(this.notebooks);
    return notebook;
  }

  async suggestByType(query: string, type: string, note: Note): Promise<Completion[]> {
    // switch (type) {
    //   case 'Hashtag':
    //     return [
    //       {
    //         label: 'Resolve',
    //         apply: () => this.findAllAsync(query),
    //       },
    //     ];
    //   default:
    const internalSuggestions = [
      ...(await this.suggestCustomTemplates(note)),
      ...(await this.suggestDefaultTemplates(note)),
      ...(await this.suggestActions()),
    ].filter(
      (completion) => completion.label.toLowerCase().indexOf(query.toLowerCase().trim()) > -1
    );
    const noteSuggestion = await this.suggestNotes(query);

    return [...internalSuggestions, ...noteSuggestion];
  }

  async findAll(query: string, limit: number = 200): Promise<Note[]> {
    try {
      const results = this.index.search(query, {
        limit: 10,
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

  getSettingsValue<T extends NestedKeys<NotebookSettings>>(
    path: T
  ): Observable<TypeAtPath<NotebookSettings, T>> {
    return this.getSettingsOrDefault().pipe(
      map((settings) => get(settings, path) as TypeAtPath<NotebookSettings, T>)
    );
  }

  hasSettingsValue<
    T extends NestedKeys<NotebookSettings>,
    V extends TypeAtPath<NotebookSettings, T>,
  >(path: T, value: V): Observable<boolean> {
    return this.getSettingsValue(path).pipe(map((actualValue) => actualValue === value));
  }

  async createNote(params: CreateNoteParams = {}, triggerOpen: boolean = false) {
    console.log('create note params', params);
    const title = params.title ?? '';
    const now = new Date();
    const technicalId = uuidv4();
    const note: Note = {
      id: technicalId,
      customId: params.customId ?? (await this.createNoteId()),
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
    await this.recordService.createRecords([this.covertNoteToRecord(note)]);

    // this.findAllAsync(title);
    if (triggerOpen) {
      this.openNote(this.toNoteHandle(0)(note));
    }
    return note;
  }

  private filterInternalNotes(note: Note): boolean {
    return note.title.trim() !== this.notebookJsonName;
  }

  private filterInternalNotes$(): OperatorFunction<Note[], Note[]> {
    return switchMap((notes) =>
      this.getSettingsValue('general.hideInternalNotes').pipe(
        map((hide) => (hide ? notes.filter((root) => this.filterInternalNotes(root)) : notes))
      )
    );
  }

  private attachChildCount$(): OperatorFunction<Note[], Note[]> {
    return switchMap((notes: Note[]) =>
      forkJoin(
        notes.map((note) => {
          return of(note);
          // return this.findAllChildrenById(note.id).pipe(
          //   map((children) => ({
          //     ...note,
          //     childrenCount: children.length,
          //   })),
          //   catchError(() => of({ ...note, childrenCount: 0 })),
          // );
        })
      )
    );
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

    const notebook: Notebook = await notebookRepository.notebooks.get(repositoryId);

    if (notebook) {
      this.createIndex();

      const notes = await notebookRepository.notes
        .where('repositoryId')
        .equals(repositoryId)
        .toArray();

      notes.forEach((note) => this.index.add(this.toIndexDocument(note)));
      console.log('Notes indexed');

      if (!notebook.offline) {
        // sync up
        const upSyncNew = notes.filter((note) => note.createdAt > notebook.lastSyncedAt);
        await this.recordService.createRecords(
          upSyncNew.map((note) => this.covertNoteToRecord(note))
        );

        const upSyncChanged = notes.filter((note) => note.updatedAt > notebook.lastSyncedAt);
        await Promise.all(upSyncChanged.map((note) => this.updateNoteInternal(note)));

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
          if (e.toString().indexOf('not found')) {
            console.log('repository not found');
          } else {
            console.error(e);
          }
        }

        notebook.lastSyncedAt = new Date();
        notebookRepository.notebooks.update(notebook.id, notebook);
      }

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
        message: 'The requested notebook does not exist locally and cannot be fetched',
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

  async updateNote(noteHandle: NoteHandle, syncOnly: boolean = false) {
    return this.updateNoteInternal(noteHandle.body, syncOnly);
  }

  private async updateNoteInternal(note: Note, syncOnly: boolean = false) {
    note.title =
      note.text
        .trimStart()
        .split(/\n/g)[0]
        .replace(/^[ ]*#[ ]*/, '') ?? '';
    const reLink = /\[([^\]]+)\]/g;
    const reHashtag = /(#[^ \n\t]+)/g;

    const extractByRegExp = (re: RegExp): string[] => {
      let m: RegExpExecArray | null;
      const matches = [];
      while ((m = re.exec(note.text))) {
        matches.push(m[1]);
      }
      return matches;
    };

    if (!syncOnly) {
      note.updatedAt = new Date();
    }

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
                apply: `[${note.customId}]`,
                // @ts-ignore
                label: `${note.title}: ${note[perField.field]}`,
                // label: `${note.title}: ${this.getHint(perField.field, query, note)}`
              };
            }
          })
        );
      })
    );
  }

  private async suggestCustomTemplates(note: Note): Promise<Completion[]> {
    if (this.isEmptyNote(note)) {
      return [];
    } else {
      const name2Template = await firstValueFrom(
        this.getSettingsValue('editor.customNoteTemplates')
      );
      return mapObj(name2Template, (value, key) => {
        return {
          label: `template-${key}`,
          apply: value,
        };
      });
    }
  }

  private async suggestDefaultTemplates(note: Note): Promise<Completion[]> {
    if (this.isEmptyNote(note)) {
      return [];
    } else {
      return firstValueFrom(
        zip(
          this.getSettingsValue('editor.useDefaultTemplates'),
          this.getSettingsValue('general.language')
        ).pipe(
          map(([useDefaultTemplates, language]) => {
            if (useDefaultTemplates) {
              return translations[language].defaultTemplates.map<Completion>((template) => ({
                label: template.label,
                apply: template.templateValue,
              }));
            } else {
              return [] as Completion[];
            }
          })
        )
      );
    }
  }

  private async suggestActions(): Promise<Completion[]> {
    const newNoteId = await this.createNoteId();
    const applyAction = (actionCallback: () => void) => {
      return (view: EditorView, completion: Completion, from: number, to: number) => {
        actionCallback();
        view.dispatch({
          changes: {
            from,
            to,
            insert: '',
          },
        });
      };
    };

    return [
      {
        label: 'new-note-id',
        apply: `[${newNoteId}]`,
      },
      {
        label: 'delete-note',
        apply: applyAction(() => console.log('detele')),
      },
      {
        label: 'clone-note',
        apply: applyAction(() => console.log('detele')),
      },
      {
        label: 'followup-note',
        apply: applyAction(() => console.log('followup')),
      },
      {
        label: 'pin-note',
        apply: applyAction(() => console.log('toggle pin')),
      },
      {
        label: 'attach-file',
        apply: applyAction(() => console.log('attach file')),
      },
    ];
  }

  private async init() {
    await this.authGuard.assertLoggedIn();
    const remoteNotebooks = await this.withOnline<
      ArrayElement<GqlCreateRepositoriesMutation['createRepositories']>[]
    >(
      () =>
        this.repositoryService.listRepositories({
          where: {
            product: {
              eq: GqlVertical.UntoldNotes,
            },
          },
          cursor: {
            page: 0,
          },
        }),
      async (): Promise<ArrayElement<GqlCreateRepositoriesMutation['createRepositories']>[]> => []
    );
    console.log('remoteNotebooks', remoteNotebooks);

    this.notebooks = (await notebookRepository.notebooks.toArray()).map((notebook) => {
      notebook.offline = !remoteNotebooks.some(
        (remoteNotebook) => remoteNotebook.id === notebook.id
      );
      return notebook;
    });

    const newRepositories = remoteNotebooks.filter(
      (r) => !this.notebooks.some((notebook) => notebook.id == r.id)
    );

    if (newRepositories.length > 0) {
      newRepositories.forEach((repository) =>
        notebookRepository.notebooks.add({
          ...repository,
          offline: false,
          lastSyncedAt: new Date(0),
        })
      );
      this.notebooks = await notebookRepository.notebooks.toArray();
    }

    // notebookRepository.notes.toArray().then((notes) => {
    //   notes.forEach((note) => {
    //     convertToRx(
    //       liveQuery(() => {
    //         return notebookRepository.notes.get(note.id);
    //       })
    //     ).subscribe((note) => console.log('changed', note.title));
    //   });
    // });

    this.notebooksChanges.next(this.notebooks);
    this.initActions();
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

  findById(noteId: string): Observable<NoteHandle> {
    return this.findByIdInternal(noteId).pipe(map((note) => this.toNoteHandle(0)(note)));
  }

  private findByIdInternal(noteId: string): Observable<Note> {
    return convertToRx(liveQuery(() => notebookRepository.notes.get(noteId)));
  }

  async openNoteById(noteId: string) {
    const note = await notebookRepository.notes.get(`${noteId}`);
    if (note) {
      this.openNote(this.toNoteHandle(0)(note));
    } else {
      await this.showToast(`No note with ID ${noteId} found.`, 'warning');
    }
  }

  async showToast(message: string, color: 'light' | 'warning' = 'light') {
    const toast = await this.toastCtrl.create({
      message: message,
      duration: 3000,
      color,
      cssClass: 'toast-small',
    });

    await toast.present();
  }

  openNote(noteHandle: NoteHandle) {
    this.openNoteChanges.next(noteHandle);
  }

  async deleteById(id: string) {
    // todo check if children exist
    const note = await notebookRepository.notes.get(id);

    this.index.remove(id);
    await notebookRepository.notes.delete(id);
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
    this.notesChanges.next({ a: id, b: ChangeModifier.remove });

    if (note.parent) {
      const parent = await notebookRepository.notes.get(note.parent);
      await this.updateNoteInternal(parent);
    }
  }

  deleteAll() {
    notebookRepository.notes.clear();
  }

  private scrollTo = (note: Note) => {
    setTimeout(() => {
      const noteHandle = document.getElementById(createNoteHandleId(note));
      noteHandle?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  private toNoteHandle(level: number): NoteHandleProvider {
    return (note: Note): NoteHandle => {
      return {
        body: note,
        body$: () => convertToRx(liveQuery(() => notebookRepository.notes.get(note.id))),
        incomingLinks$: () => of([]),
        outgoingLinks$: () =>
          this.findOutgoingNotes(note.id).pipe(
            map((notes) => {
              return notes.map(this.toNoteHandle(level + 1));
            })
          ),
        expanded: true,
        disabled: false,
        level,
        scrollTo: (event) => {
          this.scrollTo(note);
          event.stopPropagation();
        },
        childrenCount: () => this.countChildren(note.id),
        toggleUpvote: () => {
          note.isUpVoted = !note.isUpVoted;
          this.updateNoteInternal(note);
        },
        children: () => {
          return zip([
            this.findAllChildren(note.id, note.title),
            this.findByIdInternal(note.id),
          ]).pipe(
            map(([notes, note]) => {
              const embeddedIds = note.references.links;
              return orderBy(
                notes.map(this.toNoteHandle(level + 1)),
                [
                  (noteHandle) => embeddedIds.indexOf(noteHandle.body.id),
                  (noteHandle) => noteHandle.body.createdAt,
                ],
                ['asc', 'asc']
              );
            })
          );
        },
      };
    };
  }

  findAllRoots(): Observable<NoteHandle[]> {
    const roots$ = convertToRx<Note[]>(
      liveQuery(() =>
        notebookRepository.notes
          .where('repositoryId')
          .equals(this.currentRepositoryId)
          .filter((note) => isNullish(note.parent))
          .toArray()
      )
    );

    return roots$
      .pipe(this.filterInternalNotes$())
      .pipe(this.attachChildCount$())
      .pipe(map((roots) => roots.map(this.toNoteHandle(0))));
  }

  private toIndexDocument(note: Note): NoteDocument {
    return {
      id: note.id,
      parent: note.parent,
      note,
    };
  }

  findAllChildren(id: string, title: string = null): Observable<Note[]> {
    const children$ = this.findAllChildrenById(id, title);
    return children$.pipe(this.filterInternalNotes$()).pipe(this.attachChildCount$());
  }

  private findOutgoingNotes(id: string): Observable<Note[]> {
    return this.findByIdInternal(id).pipe(
      switchMap((note) => {
        const outgoingIds = note.references.links;
        if (outgoingIds.length === 0) {
          return of([]);
        } else {
          const notes$ = outgoingIds.map((id) => this.findByIdInternal(id));
          return forkJoin(notes$);
        }
      })
    );
  }

  private findAllChildrenById(id: string, title: string = null): Observable<Note[]> {
    const findAllChildrenById = liveQuery(() =>
      notebookRepository.notes
        .where('parent')
        .equals(id)
        .filter((note) => note.repositoryId === this.currentRepositoryId)
        .toArray()
    );
    findAllChildrenById.subscribe((notes) =>
      console.log(`${id} ${title} findAllChildrenById`, notes)
    );
    return convertToRx<Note[]>(findAllChildrenById);
  }

  countChildren(id: string): Observable<number> {
    return convertToRx<number>(
      liveQuery(() =>
        notebookRepository.notes
          .where('parent')
          .equals(id)
          .filter((note) => note.repositoryId === this.currentRepositoryId)
          .count()
      )
    );
  }

  getSettingsOrDefault(): Observable<NotebookSettings> {
    // todo activate
    // const settingsNote = await this.getSettingsQuery().toArray();
    // try {
    //   if (settingsNote.length > 0) {
    //     // todo fix
    //     return JSON.parse(settingsNote[0].text);
    //   } else {
    //     return defaultSettings;
    //   }
    // } catch (e) {
    //   console.error(e);
    //   await this.showToast('Invalid notebook.json', 'warning');
    return of(defaultSettings);
    // }
  }

  private getSettingsQuery() {
    return notebookRepository.notes
      .where('title')
      .equals(this.notebookJsonName)
      .filter((note) => note.repositoryId === this.currentRepositoryId)
      .limit(1);
  }

  private createSettingsNote() {
    return this.createNote(
      {
        title: this.notebookJsonName,
        text: JSON.stringify(defaultSettings, null, 2),
      },
      true
    );
  }

  findAllRecent(limit: number): Observable<NoteHandle[]> {
    return convertToRx<Note[]>(
      liveQuery(() =>
        notebookRepository.notes
          .where('repositoryId')
          .equals(this.currentRepositoryId)
          .sortBy('updatedAt')
          .then((notes) =>
            slice(notes.filter((note) => note.title !== this.notebookJsonName).reverse(), 0, limit)
          )
      )
    ).pipe(map((roots) => roots.map(this.toNoteHandle(0))));
  }

  findAllPinned(limit: number): Observable<NoteHandle[]> {
    return convertToRx<Note[]>(
      liveQuery(() =>
        notebookRepository.notes
          .where('repositoryId')
          .equals(this.currentRepositoryId)
          .sortBy('updatedAt')
          .then((notes) => slice(notes.filter((note) => note.isUpVoted).reverse(), 0, limit))
      )
    ).pipe(map((roots) => roots.map(this.toNoteHandle(0))));
  }

  private createNoteId(): Promise<string> {
    return firstValueFrom(
      this.getSettingsValue('editor.noteIdDatePattern').pipe(
        map((noteIdDatePattern) => dayjs().format(noteIdDatePattern))
      )
    );
  }

  private async initActions() {
    this.getSettingsValue('general.language').pipe(
      map((lang) => {
        notebookRepository.actions.clear();
        for (const action in NotebookActionId) {
          notebookRepository.actions.add({
            id: action as NotebookActionId,
            label: `${translations[lang].actions[action as keyof typeof NotebookActionId]}`,
          });
        }
      })
    );
  }

  async changeParentById(noteId: string, parentId: string) {
    const noteHandle = await firstValueFrom(this.findById(noteId));
    const note = noteHandle.body;
    note.parent = parentId;
    await this.updateNoteInternal(note);
    this.moveEndChanges.next(noteId);
  }

  private isEmptyNote(note: Note) {
    return note.text.replace(/[ \n\t#]+/g, '').length === 0;
  }

  findByCustomId(customId: string): Observable<NoteHandle[]> {
    return convertToRx<Note[]>(
      liveQuery(() =>
        notebookRepository.notes
          .where('repositoryId')
          .equals(this.currentRepositoryId)
          .filter((note) => note.customId === customId)
          .toArray()
      )
    ).pipe(map((roots) => roots.map(this.toNoteHandle(0))));
  }
}

function convertToRx<T>(customObservable: DexieObservable<T>): Observable<T> {
  return new Observable<T>((subscriber) => {
    const subscription = customObservable.subscribe((value: T) => {
      subscriber.next(value);
    });

    // Return cleanup logic
    return () => {
      subscription.unsubscribe();
    };
  });
}
