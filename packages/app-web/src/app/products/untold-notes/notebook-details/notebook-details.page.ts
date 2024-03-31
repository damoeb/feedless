import { ChangeDetectionStrategy, ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { debounce, interval, Subscription } from 'rxjs';
import { SourceSubscription } from '../../../graphql/types';
import { AppAction, Note, NotebookService, SearchResultGroup } from '../services/notebook.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { debounce as debounceFn, isNull } from 'lodash-es';
import { AlertController, IonSearchbar } from '@ionic/angular';
import { Completion } from '@codemirror/autocomplete';
import { FormControl } from '@angular/forms';
import { Extension } from '@codemirror/state';
import { createNoteReferenceMarker } from './note-reference-marker';
import { createNoteReferenceWidget } from './note-reference-widget';
import { CodeEditorComponent } from '../../../elements/code-editor/code-editor.component';

type SearchResult = {
  namedId?: string;
  label: string;
  text?: string;
  isGroup?: boolean;
  onClick?: () => void;
};

interface OpenNote extends Note {
  textChangeHandler: (text: string) => void;
  dirty: boolean;
}

type NoteReferences = {
  [name: string]: Note[];
};

@Component({
  selector: 'app-notebook-details-page',
  templateUrl: './notebook-details.page.html',
  styleUrls: ['./notebook-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotebookDetailsPage implements OnInit, OnDestroy {
  busy = false;
  private subscriptions: Subscription[] = [];
  subscription: SourceSubscription;
  searchResults: SearchResult[] = [];
  focussedIndex: number = -1;

  // private query: string;
  systemBusy: boolean;
  currentNote: OpenNote = null;
  queryFc = new FormControl<string>('');

  @ViewChild('searchbar')
  searchbarElement: IonSearchbar;

  @ViewChild('codeEditor')
  codeEditorComponent: CodeEditorComponent;

  searchMode = false;
  // incomingLinks: Note[];
  // outgoingLinks: NoteReferences;
  // hashtagLinks: NoteReferences;
  private notebookId: string;

  extensions: Extension[] = [
    createNoteReferenceMarker(this.notebookService),
    createNoteReferenceWidget(this.notebookService),
  ];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly alertCtrl: AlertController,
    private readonly router: Router,
    private readonly notebookService: NotebookService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    this.loadAutoSuggestions = this.loadAutoSuggestions.bind(this);
  }

  ngOnInit() {
    let query = '';
    this.subscriptions.push(
      this.notebookService.searchResultsChanges.subscribe((groups) =>
        this.handleSearchResults(groups),
      ),
      this.activatedRoute.params.subscribe((params) =>
        this.handleParams(params),
      ),
      this.notebookService.systemBusyChanges.subscribe((systemBusy) => {
        this.systemBusy = systemBusy;
        this.changeRef.detectChanges();
      }),
      this.notebookService.queryChanges
        .pipe(debounce(() => interval(100)))
        .subscribe(async (query) => {
          if (this.queryFc.value !== query) {
            this.queryFc.setValue(query);
            // await this.searchbarElement.setFocus();
          }
        }),
      this.queryFc.valueChanges.subscribe((query) => {
        this.notebookService.queryChanges.next(query);
      }),
      this.notebookService.notesChanges.subscribe(() => {
        if (query) {
          this.notebookService.findAllAsync(query);
        }
      }),
      this.notebookService.openNoteChanges.subscribe((note) =>
        this.openNote(note),
      ),
      this.notebookService.queryChanges.subscribe((newQuery) => {
        if (query != newQuery) {
          console.log('query', newQuery);
          query = newQuery;
          this.notebookService.findAllAsync(newQuery);
        }
      }),
    );
  }

  @HostListener('window:keydown.esc', ['$event'])
  async handleKeyEsc(event: KeyboardEvent) {
    this.toggleSearchMode();
  }

  loadAutoSuggestions(query: string, type: string): Promise<Completion[]> {
    return this.notebookService.suggestByType(query, type);
  }

  private async handleParams(params: Params) {
    if (params.notebookId) {
      this.notebookId = params.notebookId;

      const failSafe = async (
        header: string,
        message: string,
        redirect: () => Promise<any>,
        actionFn: () => Promise<void>,
      ) => {
        try {
          await actionFn();
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

      await failSafe(
        'Notebook',
        'The requested notebook does not exist',
        () => this.router.navigateByUrl('../'),
        () => this.notebookService.openNotebook(params.notebookId),
      );

      await failSafe(
        'Note',
        'The requested note does not exist',
        () => this.router.navigateByUrl('./'),
        async () => {
          if (params.noteId) {
            const noteId = decodeURIComponent(params.noteId);
            await this.openNote(
              await this.notebookService.findByNamedId(noteId),
            );
          }
        },
      );
    }
  }

  private async handleSearchResults(groups: SearchResultGroup[]) {
    // console.log('handleSearchResults', groups);
    this.searchResults = [];

    await groups.reduce(
      (waitFor: Promise<void>, group) =>
        waitFor.then(async () => {
          this.searchResults.push({
            label: group.name,
            isGroup: true,
          });
          const notes = await group.notes();
          for (const note of notes) {
            this.searchResults.push({
              namedId: note.namedId,
              label: note.title,
              text: note.text,
              onClick: () => {
                this.openNote(note);
              },
            });
          }
        }),
      Promise.resolve(),
    );

    this.focussedIndex = -1;
    this.busy = false;
    this.changeRef.detectChanges();
  }

  @HostListener('window:keydown.arrowup', ['$event'])
  handleKeyUp() {
    if (this.focussedIndex === 0) {
      return;
    }
    this.focussedIndex--;
    while (
      this.focussedIndex > 0 &&
      this.searchResults[this.focussedIndex].isGroup
    ) {
      this.focussedIndex--;
    }
    if (this.focussedIndex < 0) {
      this.focussedIndex = this.searchResults.length - 1;
    }
    console.log(this.focussedIndex);
    this.changeRef.detectChanges();
  }

  @HostListener('window:keydown.arrowdown', ['$event'])
  handleKeyDown() {
    if (this.focussedIndex === this.searchResults.length - 1) {
      return;
    }
    this.focussedIndex++;
    while (
      this.focussedIndex < this.searchResults.length - 1 &&
      this.searchResults[this.focussedIndex].isGroup
    ) {
      this.focussedIndex++;
    }
    if (this.focussedIndex > this.searchResults.length - 1) {
      this.focussedIndex = 0;
    }
    console.log(this.focussedIndex);
    this.changeRef.detectChanges();
  }

  @HostListener('window:keydown.enter', ['$event'])
  async handleEnter(event: KeyboardEvent) {
    console.log('handleEnter', this.focussedIndex);
    if (this.focussedIndex >= 0 && this.searchResults?.length > 0) {
      const searchResult = this.searchResults[this.focussedIndex];
      if (!searchResult.isGroup) {
        setTimeout(() => searchResult.onClick(), 1);
      }
    } else {
      // const note = await this.notebookService.createNote(this.query);
      // this.openNote(note);
    }
    event.stopPropagation();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async openNote(note: Note) {
    if (this.currentNote?.id === note.id) {
      return;
    }
    const updateAsync = debounceFn(async (openNote: OpenNote) => {
      await this.notebookService.updateNote(openNote);
      openNote.dirty = false;
      // await this.refreshReferences(openNote);
      this.changeRef.detectChanges();
    }, 1000);

    const openNote: OpenNote = {
      ...note,
      dirty: false,
      textChangeHandler: (text: string) => {
        openNote.text = text;
        openNote.dirty = true;
        // this.changeRef.detectChanges();
        updateAsync(openNote);
      },
    };
    this.notebookService.findAllAsync(note.namedId);
    // await this.refreshReferences(openNote);
    this.currentNote = null;
    this.changeRef.detectChanges();

    this.currentNote = openNote;
    this.searchMode = false;
    this.changeRef.detectChanges();
  }

  private performAction(action: AppAction) {}

  handleQuery(query: string) {
    this.notebookService.queryChanges.next(query);
  }

  async toggleSearchMode(searchMode: boolean | null = null) {
    if (isNull(searchMode)) {
      this.searchMode = !this.searchMode;
    } else {
      this.searchMode = searchMode;
    }
    if (this.searchMode) {
      await this.focusSearchElement();
    } else {
      this.codeEditorComponent?.setFocus();
    }
    this.changeRef.detectChanges();
  }

  createNote() {
    const note = this.notebookService.createNote();
    return this.openNote(note);
  }

  // private async refreshReferences(openNote: OpenNote) {
  //   const search = async (query: string): Promise<Note[]> => {
  //     const notes = await Promise.all(this.notebookService.findAll(query).map(group => group.notes()))
  //     return notes.flat().filter(note => note.id !== openNote.id);
  //   }
  //
  //   // incoming links
  //   const incomingRefs= await search(openNote.namedId)
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
  //   return `/notebook/${this.notebookId}/${encodeURIComponent(note.namedId)}`;
  // }

  deleteCurrentNote() {
    this.notebookService.deleteById(this.currentNote.id);
    this.currentNote = null;
  }

  private async focusSearchElement() {
    this.focussedIndex = 0;
    await this.searchbarElement.setFocus();
    const input = await this.searchbarElement.getInputElement();
    input.select();
  }
}
