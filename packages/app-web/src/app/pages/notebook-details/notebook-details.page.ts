import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  HostListener,
  inject,
  OnDestroy,
  OnInit,
  viewChildren,
} from '@angular/core';
import { debounce, interval, Subscription } from 'rxjs';
import { RepositoryWithFrequency } from '../../graphql/types';
import {
  Note,
  Notebook,
  NotebookService,
} from '../../services/notebook.service';
import { ActivatedRoute, Params, Router, RouterLink } from '@angular/router';
import {
  debounce as debounceFn,
  DebouncedFunc,
  isNull,
  isString,
  omit,
} from 'lodash-es';
import {
  AlertController,
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardTitle,
  IonContent,
  IonHeader,
  IonIcon,
  IonMenu,
  IonProgressBar,
  IonSplitPane,
  IonToolbar,
} from '@ionic/angular/standalone';
import { Completion } from '@codemirror/autocomplete';
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
  closeOutline,
  cloudDownloadOutline,
  cloudUploadOutline,
  ellipsisVerticalOutline,
  star,
  starOutline,
  trashOutline,
} from 'ionicons/icons';
import { RemoveIfProdDirective } from '../../directives/remove-if-prod/remove-if-prod.directive';
import { NgClass } from '@angular/common';
import { DarkModeButtonComponent } from '../../components/dark-mode-button/dark-mode-button.component';
import { LoginButtonComponent } from '../../components/login-button/login-button.component';
import {
  TypeaheadSuggestion,
  TypeheadComponent,
} from '../../components/typeahead/typehead.component';
import { notebookRepository } from '../../services/notebook-repository';

interface OpenNote extends Note {
  formControl: FormControl<string>;
  subscriptions: Subscription[];
}

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
    NgClass,
    DarkModeButtonComponent,
    LoginButtonComponent,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardContent,
    CodeEditorComponent,
    IonProgressBar,
    TypeheadComponent,
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

  busy = false;
  private subscriptions: Subscription[] = [];
  repository: RepositoryWithFrequency;
  openNotes: OpenNote[] = [];
  systemBusy: boolean;
  currentNote: OpenNote = null;

  // readonly searchbarElement = viewChild<IonSearchbar>('searchbar');

  readonly codeEditorComponents = viewChildren(CodeEditorComponent);

  searchMode = false;
  // incomingLinks: Note[];
  // outgoingLinks: NoteReferences;
  // hashtagLinks: NoteReferences;

  extensions: Extension[] = [
    createNoteReferenceMarker(this.notebookService),
    createNoteReferenceWidget(this.notebookService),
  ];
  protected notebook: Notebook;
  protected toggleSearchModeDebounced: DebouncedFunc<
    (searchMode?: boolean | null) => Promise<void>
  >;
  protected starredNotes: Note[];
  protected suggestions: TypeaheadSuggestion[] = [];

  constructor() {
    this.loadAutoSuggestions = this.loadAutoSuggestions.bind(this);
    this.toggleSearchModeDebounced = debounceFn(this.toggleSearchMode, 400);
    this.searchNotes = this.searchNotes.bind(this);
    addIcons({
      cloudDownloadOutline,
      cloudUploadOutline,
      star,
      starOutline,
      ellipsisVerticalOutline,
      closeOutline,
      trashOutline,
    });
  }

  ngOnInit() {
    this.subscriptions.push(
      // this.notebookService.searchResultsChanges.subscribe((groups) =>
      //   this.handleSearchResults(groups),
      // ),
      this.activatedRoute.params.subscribe((params) =>
        this.handleParams(params),
      ),
      this.notebookService.systemBusyChanges.subscribe(async (systemBusy) => {
        this.systemBusy = systemBusy;
        this.changeRef.detectChanges();
      }),
      // this.notebookService.notesChanges.subscribe(async () => {
      //   if (query) {
      //     this.notebookService.findAllAsync(query);
      //   }
      //   this.starredNotes = await this.notebookService
      //     .findAll('true', ['isUpVoted'])[0]
      //     .notes();
      //   this.changeRef.detectChanges();
      // }),
      this.notebookService.openNoteChanges.subscribe((note) =>
        this.openNote(note),
      ),
      // this.notebookService.queryChanges.subscribe((newQuery) => {
      //   if (query != newQuery) {
      //     console.log('query', newQuery);
      //     query = newQuery;
      //     this.notebookService.findAllAsync(newQuery);
      //   }
      // }),
    );
  }

  @HostListener('window:keydown.esc', ['$event'])
  async handleKeyEsc(event: KeyboardEvent) {
    await this.toggleSearchModeDebounced();
  }

  loadAutoSuggestions(query: string, type: string): Promise<Completion[]> {
    return this.notebookService.suggestByType(query, type);
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

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async openNote(note: Note) {
    console.log('note', note);
    const isAlreadyOpen = this.openNotes.find((it) => it.id == note.id);
    if (isAlreadyOpen) {
      this.currentNote = isAlreadyOpen;
      this.scrollTo(isAlreadyOpen);
    } else {
      const formControl = new FormControl(note.text);

      const openNote: OpenNote = {
        ...note,
        // upVoteAnnotationId: upVoted?.id,
        formControl,
        subscriptions: [
          formControl.valueChanges
            .pipe(debounce(() => interval(4000)))
            .subscribe(async (text) => {
              if (openNote.text != text) {
                openNote.text = text;
                await this.updateNote(openNote);
              }

              formControl.markAsPristine();
            }),
        ],
      };
      // this.notebookService.findAll(note.id);
      // await this.refreshReferences(openNote);
      this.currentNote = null;
      this.changeRef.detectChanges();

      this.openNotes.push(openNote);
      this.scrollTo(openNote);
      await this.toggleSearchMode(false);
      this.currentNote = openNote;
      this.searchMode = false;
      this.changeRef.detectChanges();
    }
  }

  private async toggleSearchMode(searchMode: boolean | null = null) {
    if (isNull(searchMode)) {
      this.searchMode = !this.searchMode;
    } else {
      this.searchMode = searchMode;
    }
    console.log('toggleSearchMode', this.searchMode);
    if (this.searchMode) {
      await this.focusSearchElement();
    } else {
      const index = this.openNotes.findIndex(
        (note) => note.id === this.currentNote?.id,
      );
      if (index > -1) {
        this.codeEditorComponents().at(index).setFocus();
      }
    }
    this.changeRef.detectChanges();
  }

  async createNote() {
    const note = await this.notebookService.createNote();
    return this.openNote(note);
  }

  // private async refreshReferences(openNote: OpenNote) {
  //   const search = async (query: string): Promise<Note[]> => {
  //     const notes = await Promise.all(this.notebookService.findAll(query).map(group => group.notes()))
  //     return notes.flat().filter(note => note.id !== openNote.id);
  //   }
  //
  //   // incoming links
  //   const incomingRefs= await search(openNote.id)
  //
  //   // outgoing links
  //   const { hashtags, links } = openNote.references;
  //
  //   const outlinkRefs: NoteReferences = {};
  //   for (let index in links) {
  //     const link = links[index];
  //     outlinkRefs[link] = await search(link)
  //   }
  //   const hashtagRefs: NoteReferences = {};
  //   for (let index in hashtags) {
  //     const hashtag = hashtags[index];
  //     hashtagRefs[hashtag] = await search(hashtag)
  //   }
  //   this.incomingLinks = incomingRefs;
  //   this.outgoingLinks = outlinkRefs;
  //   this.hashtagLinks = hashtagRefs;
  //
  //   console.log(incomingRefs);
  //   console.log(outlinkRefs);
  //   console.log(hashtagRefs);
  // }

  // linkTo(note: Note): string {
  //   return `/notebook/${this.notebookId}/${encodeURIComponent(note.id)}`;
  // }

  deleteCurrentNote() {
    this.notebookService.deleteById(this.currentNote.id);
    this.currentNote = null;
  }

  private async focusSearchElement() {
    console.log('focusSearchElement');
    // const searchbarElement = this.searchbarElement();
    // if (searchbarElement) {
    //   await searchbarElement.setFocus();
    //   const input = await searchbarElement.getInputElement();
    //   input.select();
    // }
  }

  private scrollTo = (note: OpenNote) => {
    setTimeout(() => {
      const noteHandle = document.getElementById(`open-note-handle-${note.id}`);
      console.log('scrollTo', noteHandle);
      noteHandle?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  async ngAfterViewInit() {
    await this.focusSearchElement();
  }

  closeNote(openNote: OpenNote) {
    openNote.subscriptions.forEach((subscription) =>
      subscription.unsubscribe(),
    );
    this.openNotes = this.openNotes.filter((note) => note.id != openNote.id);
  }

  async toggleUpvote(note: OpenNote) {
    if (note.isUpVoted) {
      await this.annotationService.deleteAnnotation({
        where: {
          id: note.upVoteAnnotationId,
        },
      });
      note.isUpVoted = false;
      note.upVoteAnnotationId = null;
    } else {
      const annotation = await this.annotationService.createAnnotation({
        where: {
          repository: {
            id: note.repositoryId,
          },
        },
        annotation: {
          upVote: {
            set: true,
          },
        },
      });
      note.isUpVoted = true;
      note.upVoteAnnotationId = annotation.id;
    }
    await this.updateNote(note);
  }

  private async updateNote(openNote: OpenNote) {
    const omitFields: (keyof OpenNote)[] = ['formControl', 'subscriptions'];
    const cleanNote: Note = omit(openNote, omitFields) as any;
    await this.notebookService.updateNote(cleanNote);
  }

  async searchNotes(query: string) {
    console.log('?', query);
    const parts = query
      .split(' ')
      .map((part) => part.trim())
      .filter(Boolean);
    this.suggestions = await this.notebookService
      .findAll(query)
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
}
