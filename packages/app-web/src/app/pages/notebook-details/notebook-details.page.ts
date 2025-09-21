import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  HostListener,
  inject,
  OnDestroy,
  OnInit,
  viewChild,
  viewChildren,
} from '@angular/core';
import { firstValueFrom, from, map, Observable, Subscription, zip } from 'rxjs';
import { RepositoryWithFrequency } from '../../graphql/types';
import {
  Note,
  Notebook,
  NotebookService,
  NotebookSettings,
  NoteShortcutType,
} from '../../services/notebook.service';
import { ActivatedRoute, Params, Router, RouterLink } from '@angular/router';
import { isString, orderBy } from 'lodash-es';
import {
  AlertController,
  IonAccordion,
  IonAccordionGroup,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonMenu,
  IonMenuButton,
  IonProgressBar,
  IonSegment,
  IonSegmentButton,
  IonSpinner,
  IonSplitPane,
  IonText,
  IonToolbar,
} from '@ionic/angular/standalone';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Extension } from '@codemirror/state';
import { createNoteReferenceMarker } from './note-reference-marker';
import { createNoteReferenceWidget } from './note-reference-widget';
import { CodeEditorComponent } from '../../elements/code-editor/code-editor.component';
import { UploadService } from '../../services/upload.service';
import { AppConfigService } from '../../services/app-config.service';
import { AnnotationService } from '../../services/annotation.service';
import { addIcons } from 'ionicons';
import {
  attachOutline,
  chevronForwardOutline,
  closeOutline,
  cloudDownloadOutline,
  cloudUploadOutline,
  copyOutline,
  ellipse,
  ellipseOutline,
  ellipsisVerticalOutline,
  pinOutline,
  returnDownForwardOutline,
  returnUpForwardOutline,
  swapVerticalOutline,
  trashOutline,
} from 'ionicons/icons';
import { RemoveIfProdDirective } from '../../directives/remove-if-prod/remove-if-prod.directive';
import { DarkModeButtonComponent } from '../../components/dark-mode-button/dark-mode-button.component';
import { LoginButtonComponent } from '../../components/login-button/login-button.component';
import {
  TypeaheadSuggestion,
  TypeheadComponent,
} from '../../components/typeahead/typehead.component';
import { NoteDetailsComponent } from '../../components/note-details/note-details.component';
import { AsyncPipe, NgClass, NgStyle } from '@angular/common';
import { Completion } from '@codemirror/autocomplete';
import { NestedKeys, Nullable, TypeAtPath } from '../../types';
import {
  CdkNestedTreeNode,
  CdkTree,
  CdkTreeNode,
  CdkTreeNodeDef,
  CdkTreeNodeOutlet,
} from '@angular/cdk/tree';
import { BubbleComponent } from '../../components/bubble/bubble.component';
import { SessionService } from '../../services/session.service';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { NotebookSettingsComponent } from '../../components/notebook-settings/notebook-settings.component';

export type EditorHandle = {
  note: Note;
  noteHandle: NoteHandle;
  formControl: FormControl<string>;
  subscriptions: Subscription[];
};

enum Focussable {
  editor,
  searchbar,
}

export type NoteHandle = {
  body: Note;
  expanded: boolean;
  disabled: boolean;
  level: number;
  childrenCount: () => Observable<number>;
  scrollTo: (event: MouseEvent) => void;
  children: () => Observable<NoteHandle[]>;
  toggleUpvote: () => void;
};

@Component({
  selector: 'app-notebook-details-page',
  templateUrl: './notebook-details.page.html',
  styleUrls: ['./notebook-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonSplitPane,
    IonMenu,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    RouterLink,
    RemoveIfProdDirective,
    IonIcon,
    FormsModule,
    IonContent,
    ReactiveFormsModule,
    DarkModeButtonComponent,
    LoginButtonComponent,
    IonProgressBar,
    TypeheadComponent,
    NoteDetailsComponent,
    IonAccordionGroup,
    IonAccordion,
    IonItem,
    IonLabel,
    NgClass,
    CodeEditorComponent,
    IonSpinner,
    CdkTree,
    CdkNestedTreeNode,
    // noinspection ES6UnusedImports
    CdkTreeNode,
    CdkTreeNodeOutlet,
    // noinspection ES6UnusedImports
    CdkTreeNodeDef,
    NgStyle,
    BubbleComponent,
    IonText,
    IonSegment,
    IonSegmentButton,
    AsyncPipe,
    NotebookSettingsComponent,
    IonMenuButton,
  ],
  standalone: true,
})
export class NotebookDetailsPage implements OnInit, OnDestroy, AfterViewInit {
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly alertCtrl = inject(AlertController);
  private readonly annotationService = inject(AnnotationService);
  private readonly appConfig = inject(AppConfigService);
  protected readonly upload = inject(UploadService);
  private readonly router = inject(Router);
  private readonly notebookService = inject(NotebookService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly sessionService = inject(SessionService);

  busy = false;
  private subscriptions: Subscription[] = [];
  repository: RepositoryWithFrequency;
  systemBusy: boolean;
  currentEditorHandle: EditorHandle = null;

  readonly searchbarElement = viewChild<TypeheadComponent>('searchbar');
  readonly codeEditorComponents = viewChildren(CodeEditorComponent);

  extensions: Extension[] = [
    createNoteReferenceMarker(this.notebookService),
    createNoteReferenceWidget(this.notebookService),
  ];
  protected notebook: Notebook;
  protected suggestions: TypeaheadSuggestion[] = [];
  protected autosafe = new FormControl<boolean>(true);
  shortcutsFC = new FormControl<NoteShortcutType>('off');
  shortcuts$: Observable<NoteHandle[]> = from([]);
  shortcutValueRecent: NoteShortcutType = 'recent';
  shortcutValuePinned: NoteShortcutType = 'pinned';
  shortcutValueOff: NoteShortcutType = 'off';

  treeRoots: Observable<NoteHandle[]> = from([]);
  private setSystemReady: (value: PromiseLike<void> | void) => void;
  private waitForReady: Promise<void> = new Promise(
    (resolve) => (this.setSystemReady = resolve),
  );

  constructor() {
    this.searchNotes = this.searchNotes.bind(this);
    this.childrenAccessor = this.childrenAccessor.bind(this);
    this.toNoteHandle = this.toNoteHandle.bind(this);
    this.loadAutoSuggestions = this.loadAutoSuggestions.bind(this);
    addIcons({
      cloudDownloadOutline,
      cloudUploadOutline,
      ellipsisVerticalOutline,
      closeOutline,
      trashOutline,
      returnDownForwardOutline,
      copyOutline,
      returnUpForwardOutline,
      chevronForwardOutline,
      swapVerticalOutline,
      attachOutline,
      pinOutline,
      ellipseOutline,
      ellipse,
    });
  }

  ngOnInit() {
    this.subscribeEvents();
  }

  private subscribeEvents() {
    this.subscriptions.push(
      this.shortcutsFC.valueChanges.subscribe((value) =>
        this.handleShortcutValue(value),
      ),
      this.activatedRoute.params.subscribe((params) =>
        this.handleParams(params),
      ),
      this.notebookService.systemBusyChanges.subscribe(async (systemBusy) => {
        this.systemBusy = systemBusy;
        if (!systemBusy) {
          this.onReady();
        }

        this.changeRef.detectChanges();
      }),
      this.notebookService.openNoteChanges.subscribe((note) =>
        this.openNote(note),
      ),
      this.notebookService.notesChanges
        .asObservable()
        .subscribe(() => this.loadTree()),
    );
  }

  ngOnDestroy(): void {
    this.unsubscribeEvents();
  }

  private unsubscribeEvents(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  loadAutoSuggestions(query: string, type: string): Promise<Completion[]> {
    return this.notebookService.suggestByType(
      query,
      type,
      this.currentEditorHandle.note,
    );
  }

  @HostListener('window:keydown.esc', ['$event'])
  async handleKeyEsc(event: KeyboardEvent) {
    // if (this.currentNote) {
    //   if (this.searchbarElement().hasFocus()) {
    //     await this.focusEditor();
    //   } else {
    //     this.searchbarElement().setFocus();
    //   }
    // } else {
    this.searchbarElement().setFocus();
    // }
  }

  private async handleParams(params: Params) {
    if (params.notebookId) {
      const failSafe = async <T>(
        header: string,
        message: string,
        redirect: () => Promise<any>,
        actionFn: () => Promise<T>,
      ) => {
        try {
          return actionFn();
        } catch (e) {
          const alert = await this.alertCtrl.create({
            header,
            backdropDismiss: false,
            message,
            cssClass: 'fatal-alert',
            buttons: [
              {
                role: 'cancel',
                text: 'OK',
                handler: redirect,
              },
            ],
          });

          await alert.present();
        }
      };

      this.notebook = await failSafe(
        'Notebook',
        'The requested notebook does not exist',
        () => this.router.navigateByUrl('../'),
        () => this.notebookService.openNotebook(params.notebookId),
      );

      this.appConfig.setPageTitle(`Notebook ${this.notebook.title}`);

      await failSafe(
        'Note',
        'The requested note does not exist',
        () => this.router.navigateByUrl('./'),
        async () => {
          if (params.noteId) {
            const noteId = decodeURIComponent(params.noteId);
            await this.openNote(await this.notebookService.findById(noteId));
          }
        },
      );
    }
  }

  async openNote(note: Note) {
    console.log('openNote', note.title);
    const formControl = new FormControl(note.text);
    const editor: EditorHandle = {
      note,
      noteHandle: this.toNoteHandle(0)(note),
      // upVoteAnnotationId: upVoted?.id,
      formControl,
      subscriptions: [
        formControl.valueChanges.subscribe(async (text) => {
          if (editor.note.text != text) {
            editor.note.text = text;
            await this.updateNote(note);
          }

          formControl.markAsPristine();
        }),
      ],
    };
    // await this.refreshReferences(openNote);
    this.currentEditorHandle = null;
    this.changeRef.detectChanges();

    this.scrollTo(editor.note);
    this.currentEditorHandle = editor;
    this.changeRef.detectChanges();
    setTimeout(async () => {
      await this.setFocus(Focussable.editor);
    }, 1000);
  }

  hasSettingsValue<
    T extends NestedKeys<NotebookSettings>,
    V extends TypeAtPath<NotebookSettings, T>,
  >(path: T, value: V): Promise<boolean> {
    return firstValueFrom(this.notebookService.hasSettingsValue(path, value));
  }

  getSettingsValue<
    T extends NestedKeys<NotebookSettings>,
    V extends TypeAtPath<NotebookSettings, T>,
  >(path: T): Promise<V> {
    return firstValueFrom(this.notebookService.getSettingsValue(path));
  }

  private async setFocus(element: Focussable) {
    console.log('setFocus', element);
    switch (element) {
      case Focussable.searchbar:
        return await this.focusSearchElement();
      case Focussable.editor:
        return await this.focusEditor();
    }
  }

  async createNoteByParent(parent: Nullable<string> = null) {
    const note = await this.notebookService.createNote({ parent }, true);
    return this.openNote(note);
  }

  // deleteCurrentNote() {
  //   this.notebookService.deleteById(this.currentNote.id);
  //   this.currentNote = null;
  // }

  private async focusSearchElement() {
    const searchbarElement = this.searchbarElement();
    if (searchbarElement) {
      console.log('focusSearchElement');
      await searchbarElement.setFocus();
    } else {
      console.warn('cannot focusSearchElement');
    }
  }

  private scrollTo = (note: Note) => {
    setTimeout(() => {
      const noteHandle = document.getElementById(createNoteHandleId(note));
      noteHandle?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  async ngAfterViewInit() {
    setTimeout(async () => {
      await this.focusSearchElement();
    }, 3000);
  }

  // closeNote(openNote: OpenNote) {
  //   openNote.subscriptions.forEach((subscription) =>
  //     subscription.unsubscribe(),
  //   );
  //   this.openNotes = this.openNotes.filter((note) => note.id != openNote.id);
  // }

  // async toggleUpvote(note: OpenNote) {
  //   if (note.isUpVoted) {
  //     await this.annotationService.deleteAnnotation({
  //       where: {
  //         id: note.upVoteAnnotationId,
  //       },
  //     });
  //     note.isUpVoted = false;
  //     note.upVoteAnnotationId = null;
  //   } else {
  //     const annotation = await this.annotationService.createAnnotation({
  //       where: {
  //         repository: {
  //           id: note.repositoryId,
  //         },
  //       },
  //       annotation: {
  //         upVote: {
  //           set: true,
  //         },
  //       },
  //     });
  //     note.isUpVoted = true;
  //     note.upVoteAnnotationId = annotation.id;
  //   }
  //   await this.updateNote(note);
  // }

  private async updateNote(note: Note) {
    await this.notebookService.updateNote(note);
  }

  async searchNotes(query: string) {
    await this.waitForReady;
    console.log('?', query);
    const parts = query
      .split(' ')
      .map((part) => part.trim())
      .filter(Boolean);
    this.suggestions = await this.notebookService
      .findAll(query, null, 5)
      .then((notes) =>
        notes.map((note) => this.toTypeaheadSuggestion(note, parts)),
      );
    this.changeRef.detectChanges();
  }

  private toTypeaheadSuggestion(
    note: Note,
    queryParts: string[],
  ): TypeaheadSuggestion {
    return {
      id: note.id,
      highlightedTitle: this.highlightMatches(note.text, queryParts),
    };
  }

  private highlightMatches(haystack: string, parts: string[]) {
    if (parts.length === 0) {
      return haystack;
    }

    const escapedParts = parts.map((part) =>
      part.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'),
    );

    const regex = new RegExp(`(${escapedParts.join('|')})`, 'gi');

    return haystack.replace(regex, '<strong>$1</strong>');
  }

  async pickSuggestionOrQuery(suggestionOrQuery: TypeaheadSuggestion | string) {
    if (isString(suggestionOrQuery)) {
    } else {
      const note = await this.notebookService.findById(suggestionOrQuery.id);
      await this.openNote(note);
    }
  }

  trackByFn(index: number, item: NoteHandle) {
    const trackId = `${item.body.id}/${item.body.updatedAt}`;
    // const trackId = `${item.body.id}/${item.body.references.childrenCount}:${item.body.updatedAt}`;
    // console.log(trackId);
    return trackId;
  }

  private loadTree() {
    console.log('loadTree');
    this.treeRoots = this.notebookService
      .findAllRoots()
      .pipe(map((roots) => roots.map(this.toNoteHandle(0))));
    this.changeRef.detectChanges();
  }

  private toNoteHandle(level: number): (note: Note) => NoteHandle {
    return (note: Note): NoteHandle => {
      return {
        body: note,
        expanded: true,
        disabled: false,
        level,
        scrollTo: (event) => {
          this.scrollTo(note);
          event.stopPropagation();
        },
        childrenCount: () => this.notebookService.countChildren(note.id),
        toggleUpvote: () => {
          note.isUpVoted = !note.isUpVoted;
          this.notebookService.updateNote(note);
        },
        children: () => {
          return zip([
            this.notebookService.findAllChildren(note.id),
            from(this.notebookService.findById(note.id)),
          ]).pipe(
            map(([notes, note]) => {
              const embeddedIds = note.references.links;
              return orderBy(
                notes.map(this.toNoteHandle(level + 1)),
                [
                  (noteHandle) => embeddedIds.indexOf(noteHandle.body.id),
                  (noteHandle) => noteHandle.body.createdAt,
                ],
                ['asc', 'asc'],
              );
            }),
          );
        },
      };
    };
  }

  childrenAccessor(node: NoteHandle): Observable<NoteHandle[]> {
    return node.children();
  }

  closeNote() {
    this.notebookService.closeNoteChanges.next(this.currentEditorHandle.note);
    this.currentEditorHandle = null;
    this.changeRef.detectChanges();
  }

  async deleteNote() {
    await this.notebookService.deleteById(this.currentEditorHandle.note.id);
    this.closeNote();
    await this.notebookService.showToast('Deleted');
  }

  async createFollowUpNote() {
    const editor = this.currentEditorHandle;
    this.closeNote();
    await this.createNoteByParent(editor.note.id);
  }

  async changeParent() {
    this.notebookService.moveStartChanges.next(
      this.currentEditorHandle.note.id,
    );
  }

  async cloneNote() {
    const editor = this.currentEditorHandle;
    this.closeNote();
    await this.notebookService.createNote(editor.note, true);
  }

  private async focusEditor() {
    if (this.codeEditorComponents().length > 0) {
      this.codeEditorComponents().at(0).setFocus();
    } else {
      console.warn('cannot focus');
    }
  }

  private async handleShortcutValue(value: NoteShortcutType) {
    const toShortcutHandle = (notes: Note[]): NoteHandle[] =>
      notes.map<NoteHandle>((note) => this.toNoteHandle(0)(note));
    const settings = await firstValueFrom(
      this.notebookService.getSettingsOrDefault(),
    );
    switch (value) {
      case 'pinned':
        this.shortcuts$ = this.notebookService
          .findAllPinned(settings.shortcuts.limit)
          .pipe(map(toShortcutHandle));
        break;
      case 'recent':
        this.shortcuts$ = this.notebookService
          .findAllRecent(settings.shortcuts.limit)
          .pipe(map(toShortcutHandle));
        break;
    }
  }

  private async initShortcuts() {
    this.shortcutsFC.setValue(await this.getSettingsValue('shortcuts.type'));
    this.changeRef.markForCheck();
  }

  private async onReady() {
    this.setSystemReady();
    await this.initShortcuts();
    this.loadTree();

    this.sessionService.setColorScheme(
      await this.hasSettingsValue('general.darkMode', true),
    );

    await this.initEditor();
  }

  private async initEditor() {
    const startupNote = await this.getSettingsValue('general.startupNote');

    switch (startupNote) {
      case 'index':
        return this.notebookService.openNoteById('index');
      // case 'lastChanged':
      //   return this.notebookService.openLastNote();
    }
  }

  onDrop(event: CdkDragDrop<any, any>) {
    console.log(event);
  }
}

export function createNoteHandleId(note: Note): string {
  return `note-handle-${note.id}`;
}
