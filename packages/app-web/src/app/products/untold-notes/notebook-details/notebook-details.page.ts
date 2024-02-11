import { ChangeDetectionStrategy, ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { debounce, interval, Subscription } from 'rxjs';
import { SourceSubscription } from '../../../graphql/types';
import { AppAction, Note, NotebookService, SearchResultGroup } from '../services/notebook.service';
import { ActivatedRoute, Params } from '@angular/router';
import { debounce as debounceFn, isNull } from 'lodash-es';
import { AlertController, IonSearchbar } from '@ionic/angular';
import { Completion } from '@codemirror/autocomplete';
import { FormControl } from '@angular/forms';

type SearchResult = {
  id?: string;
  label: string;
  textHighlighted?: string;
  isGroup?: boolean;
  onClick?: () => void;
};

interface OpenNote extends Note {
  textChangeHandler: (text: string) => void;
  dirty: boolean;
}

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
  searchResults: SearchResult[];
  focussedIndex: number = -1;

  private query: string;
  systemBusy: boolean;
  openedNotes: OpenNote[] = [];
  currentNote: OpenNote = null;
  queryFc = new FormControl<string>('');

  @ViewChild('searchbar')
  searchbarElement: IonSearchbar;
  searchMode = false;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly alertCtrl: AlertController,
    private readonly notebookService: NotebookService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    this.loadAutoSuggestions = this.loadAutoSuggestions.bind(this);
  }

  ngOnInit() {
    this.subscriptions.push(
      this.notebookService.notesChanges.subscribe((groups) => this.handleSearchResults(groups)),
      this.activatedRoute.params.subscribe((params) => this.handleParams(params)),
      this.notebookService.systemBusyChanges.subscribe((systemBusy) => {
        this.systemBusy = systemBusy;
        this.changeRef.detectChanges();
      }),
      this.notebookService.queryChanges
        .pipe(debounce(() => interval(100)))
        .subscribe(async (query) => {
          if (this.queryFc.value !== query) {
            this.queryFc.setValue(query);
            await this.searchbarElement.setFocus();
          }
        }),
      this.queryFc.valueChanges.subscribe((query) => {
        this.notebookService.queryChanges.next(query);
      }),
      this.notebookService.queryChanges.subscribe((query) => {
        if (this.query != query) {
          console.log('query', query);
          this.query = query;
          this.notebookService.searchAsync(query);
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
      console.log('notebookId', params.notebookId);

      try {
        await this.notebookService.openNotebook(params.notebookId);
      } catch (e) {
        const alert = await this.alertCtrl.create({
          header: 'Notebook',
          backdropDismiss: false,
          message:
            'Looks like the requested notebook you requested does not exist',
          cssClass: 'fatal-alert',
          buttons: [
            {
              role: 'cancel',
              text: 'OK',
            },
          ],
        });

        await alert.present();
      }
    }
  }

  private async handleSearchResults(groups: SearchResultGroup[]) {
    console.log('handleSearchResults', groups);
    this.searchResults = [];

    await groups.reduce((waitFor: Promise<void>, group) => waitFor.then(async () => {
      this.searchResults.push({
        label: group.name,
        isGroup: true,
      });
      const notes = await group.notes();
      for (const note of notes) {
        this.searchResults.push({
          id: note.id,
          label: note.name,
          textHighlighted: note.text,
          onClick: () => {
            this.openNote(note);
          },
        });
      }

    }), Promise.resolve());

    this.focussedIndex = -1;
    this.busy = false;
    this.changeRef.detectChanges();
  }

  @HostListener('window:keydown.arrowup', ['$event'])
  handleKeyUp() {
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
    this.changeRef.detectChanges();
  }

  @HostListener('window:keydown.arrowdown', ['$event'])
  handleKeyDown() {
    this.focussedIndex++;
    while (
      this.searchResults.length - 1 &&
      this.searchResults[this.focussedIndex].isGroup
    ) {
      this.focussedIndex++;
    }
    if (this.focussedIndex > this.searchResults.length - 1) {
      this.focussedIndex = 0;
    }
    this.changeRef.detectChanges();
  }

  @HostListener('window:keydown.enter', ['$event'])
  async handleEnter() {
    console.log('handleEnter', this.focussedIndex);
    if (this.focussedIndex >= 0 && this.searchResults?.length > 0) {
      const searchResult = this.searchResults[this.focussedIndex];
      if (!searchResult.isGroup) {
        searchResult.onClick();
      }
    } else {
      const note = await this.notebookService.createNote(this.query);
      this.openNote(note);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  openNote(note: Note) {
    const openedNote = this.openedNotes.find((otherNote) => otherNote.id === note.id);
    if (openedNote) {
      this.currentNote = openedNote;
    } else {
      const updateAsync = debounceFn((openNote: OpenNote) => {
        this.notebookService.updateNote(openNote);
        openNote.dirty = false;
        this.changeRef.detectChanges();
      }, 1000);

      const openNote = {
        ...note,
        dirty: false,
        textChangeHandler: (text: string) => {
          note.text = text;
          openNote.dirty = true;
          // this.changeRef.detectChanges();
          updateAsync(openNote);
        },
      };
      this.openedNotes.push(openNote);
      this.currentNote = openNote;
    }
    this.searchMode = false;
    this.changeRef.detectChanges();
  }

  private performAction(action: AppAction) {}

  closeNote(note: OpenNote) {
    this.openedNotes = this.openedNotes.filter(
      (otherNote) => otherNote.id !== note.id,
    );
    if (this.currentNote === note) {
      this.currentNote = null;
    }
  }

  handleQuery(query: string) {
    this.notebookService.queryChanges.next(query);
  }

  async toggleSearchMode(searchMode: boolean|null = null) {
    if (isNull(searchMode)) {
      this.searchMode = !this.searchMode;
    } else {
      this.searchMode = searchMode;
    }
    if (this.searchMode) {
      await this.searchbarElement.setFocus();
      const input = await this.searchbarElement.getInputElement();
      input.select();
    }
    this.changeRef.detectChanges();
  }

  createNote() {
    const note = this.notebookService.createNote('', '');
    this.openNote(note);
  }
}
