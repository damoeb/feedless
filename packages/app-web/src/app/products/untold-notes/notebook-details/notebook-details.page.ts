import { ChangeDetectionStrategy, ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { debounce, interval, Subscription } from 'rxjs';
import { SourceSubscription } from '../../../graphql/types';
import { AppAction, Note, NotebookService } from '../services/notebook.service';
import { FormControl } from '@angular/forms';
import { CodeEditorComponent } from '../../../elements/code-editor/code-editor.component';

type SearchResult = {
  label: string
  isGroup?: boolean
  onClick?: () => void
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
  focussedIndex: number = 0;
  private currentNote: Note;

  @ViewChild('editor')
  editorElement: CodeEditorComponent;
  noteFc = new FormControl<string>('');
  private query: string;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly notebookService: NotebookService
  ) {
  }

  ngOnInit() {
    this.subscriptions.push(
      this.notebookService.notesChanges
        .subscribe(groups => {
          this.searchResults = [];
          groups.forEach(group => {
            this.searchResults.push({
              label: group.name,
              isGroup: true
            });
            group.notes?.forEach(note => {
              this.searchResults.push({
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
            this.focussedIndex = 0;
            this.busy = false;
            this.changeRef.detectChanges();
          }
        }),
      this.notebookService.openedNoteChanges.subscribe(note => {
        this.currentNote = note;
        this.noteFc.setValue(note.text);
        console.log('focus');
        this.editorElement.setFocus();
      }),
      this.notebookService.queryChanges
        .subscribe(() => {
          this.busy = true;
          this.changeRef.detectChanges();
        }),
      this.notebookService.queryChanges
        .pipe(debounce(() => interval(300)))
        .subscribe(query => {
          this.query = query;
          this.notebookService.search(query);
        }),
      this.noteFc.valueChanges.subscribe(text => {
        if (this.currentNote) {
          console.log(`patch ${this.currentNote.id} with ${text}`);
        }
      })
    );
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
    if (this.focussedIndex >= 0) {
      const searchResult = this.searchResults[this.focussedIndex];
      if (!searchResult.isGroup) {
        searchResult.onClick();
      }
    } else {
      await this.notebookService.createNote(this.query);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  openNote(note: Note) {
    console.log('open', note.id);
    this.editorElement.setText(note.text);
  }

  private performAction(action: AppAction) {

  }
}
