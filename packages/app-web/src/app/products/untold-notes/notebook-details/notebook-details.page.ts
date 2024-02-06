import { ChangeDetectionStrategy, ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { debounce, interval, Subscription } from 'rxjs';
import { SourceSubscription } from '../../../graphql/types';
import { AppAction, Note, NotebookService, SearchResultGroup } from '../services/notebook.service';
import { ActivatedRoute, Params } from '@angular/router';
import { debounce as debounceFn, DebouncedFunc, without } from 'lodash-es';
import { AlertController } from '@ionic/angular';
import { Completion } from '@codemirror/autocomplete';

type SearchResult = {
  id?: string,
  label: string
  isGroup?: boolean
  onClick?: () => void
}

interface OpenNote extends Note {
  textChangeHandler: (text: string) => void;
  dirty: boolean;
}

@Component({
  selector: 'app-notebook-details-page',
  templateUrl: './notebook-details.page.html',
  styleUrls: ['./notebook-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
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

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly alertCtrl: AlertController,
    private readonly notebookService: NotebookService,
    private readonly activatedRoute: ActivatedRoute
  ) {
    this.loadAutoSuggestions = this.loadAutoSuggestions.bind(this);
  }

  ngOnInit() {
    this.subscriptions.push(
      this.notebookService.notesChanges
        .subscribe(groups => {
          this.handleSearchResults(groups);
        }),
      this.activatedRoute.params.subscribe(async params => {
        await this.handleParams(params);
      }),
      this.notebookService.systemBusyChanges.subscribe(systemBusy => {
        this.systemBusy = systemBusy;
        this.changeRef.detectChanges();
      }),
      this.notebookService.openedNoteChanges.subscribe(note => {
        this.openNote(note);
      }),
      this.notebookService.queryChanges
        // .pipe(debounce(() => interval(300)))
        .subscribe(query => {
          console.log('query', query);
          if (this.query !== query) {
            this.query = query;
            this.notebookService.searchAsync(query);
          }
        }),
    );

    // this.notebookService.createNote('New Note')
  }

  loadAutoSuggestions(query: string): Completion[] {
    return this.notebookService.search(query).map(note => {
      return {label: note.hint, apply: `[${note.name}]`}
    });
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
        message: 'Looks like the requested notebook you requested does not exist',
        cssClass: 'fatal-alert',
        buttons: [
          {
            role: 'cancel',
            text: 'OK'
          }]
      });

      await alert.present();
    }
    }
  }

  private handleSearchResults(groups: SearchResultGroup[]) {
    console.log('handleSearchResults', groups);
    this.searchResults = [];
    groups.forEach(group => {
      this.searchResults.push({
        label: group.name,
        isGroup: true
      });
      group.notes?.forEach(note => {
        this.searchResults.push({
          id: note.id,
          label: note.name,
          onClick: () => this.openNote(note)
        });
      });
      group.actions?.forEach(action => {
        this.searchResults.push({
          label: action.name,
          onClick: () => this.performAction(action)
        });
      });
    });
    if (this.focussedIndex !== 0 || this.busy) {
      this.focussedIndex = -1;
      this.busy = false;
      this.changeRef.detectChanges();
    }
  }

  @HostListener('window:keydown.arrowup', ['$event'])
  handleKeyUp() {
    this.focussedIndex--;
    if (this.focussedIndex < 0) {
      this.focussedIndex = this.searchResults.length - 1;
    }
    this.changeRef.detectChanges();
  }

  @HostListener('window:keydown.arrowdown', ['$event'])
  handleKeyDown() {
    this.focussedIndex++;
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
    console.log('open', note.id);
    const updateAsync = debounceFn((openNote: OpenNote) => {
      this.notebookService.updateNode(openNote);
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
      }
    };
    this.openedNotes.push(openNote);
    this.changeRef.detectChanges();
  }

  private performAction(action: AppAction) {

  }

  // private handleTextChange(note: Note) {
  //   // if (this.openedNote) {
  //     note.text = this.editorElement.getText();
  //     this.notebookService.updateNode(note);
  //   // } else {
  //   //   this.openedNote = this.notebookService.createNote(`New Note ${new Date().toLocaleDateString("en-US")}`, text)
  //   // }
  // }
  closeNote(note: OpenNote) {
    this.openedNotes = without(this.openedNotes, note);
  }
}
