import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  HostListener,
  inject,
  input,
  OnDestroy,
  OnInit,
  output,
} from '@angular/core';
import { Subscription } from 'rxjs';
import {
  IonItem,
  IonLabel,
  IonList,
  IonProgressBar,
  IonSearchbar,
  IonSpinner,
} from '@ionic/angular/standalone';
import { NgClass } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

export type TypeaheadSuggestion = {
  id: string;
  highlightedTitle: string;
};

@Component({
  selector: 'app-typeahead',
  templateUrl: './typehead.component.html',
  styleUrls: ['./typehead.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonLabel,
    IonItem,
    IonSearchbar,
    IonList,
    NgClass,
    ReactiveFormsModule,
    IonSpinner,
    IonProgressBar,
  ],
  standalone: true,
})
export class TypeheadComponent implements OnInit, OnDestroy {
  private readonly changeRef = inject(ChangeDetectorRef);
  readonly pick = output<TypeaheadSuggestion | string>();
  readonly query = input.required<string>();
  readonly suggestions = input.required<TypeaheadSuggestion[]>();
  readonly queryChange = output<string>();

  private subscriptions: Subscription[] = [];
  queryControl: FormControl<string> = new FormControl<string>('');
  busy: boolean = false;
  focussedMatchIndex: number = -1;

  async ngOnInit() {
    this.subscriptions.push(
      this.queryControl.valueChanges.subscribe(async (queryString) => {
        this.busy = true;
        this.changeRef.detectChanges();
        this.queryChange.emit(queryString);
        // this.busy = false;
        // this.changeRef.detectChanges();
      }),
    );
    this.queryControl.setValue(this.query());
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  pickSuggestion(suggestion: TypeaheadSuggestion) {
    this.pick.emit(suggestion);
  }

  @HostListener('window:keydown.arrowup', ['$event'])
  handleKeyUp() {
    console.log('up', this.focussedMatchIndex);
    if (this.focussedMatchIndex === 0) {
      return;
    }
    this.focussedMatchIndex--;
    if (this.focussedMatchIndex < 0) {
      this.focussedMatchIndex = this.getSuggestions().length - 1;
    }
    this.changeRef.detectChanges();
  }

  protected getSuggestions() {
    return this.suggestions() ?? [];
  }

  @HostListener('window:keydown.arrowdown', ['$event'])
  handleKeyDown() {
    console.log('down', this.focussedMatchIndex);
    if (this.focussedMatchIndex === this.getSuggestions().length - 1) {
      return;
    }
    this.focussedMatchIndex++;
    if (this.focussedMatchIndex > this.getSuggestions().length - 1) {
      this.focussedMatchIndex = 0;
    }
    this.changeRef.detectChanges();
  }

  @HostListener('window:keydown.enter', ['$event'])
  async handleEnter(event: KeyboardEvent) {
    console.log('handleEnter', this.focussedMatchIndex);
    if (this.focussedMatchIndex >= 0 && this.getSuggestions().length > 0) {
      const searchResult = this.getSuggestions()[this.focussedMatchIndex];
      this.pickSuggestion(searchResult);
    } else {
      this.pick.emit(this.queryControl.value);
    }
    event.stopPropagation();
  }
}
