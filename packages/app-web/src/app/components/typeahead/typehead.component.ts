import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  HostListener,
  inject,
  input,
  OnChanges,
  OnDestroy,
  OnInit,
  output,
  SimpleChanges,
  viewChild,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { IonItem, IonLabel, IonList, IonSearchbar } from '@ionic/angular/standalone';
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
  imports: [IonLabel, IonItem, IonSearchbar, IonList, NgClass, ReactiveFormsModule],
  standalone: true,
})
export class TypeheadComponent implements OnInit, OnDestroy, OnChanges {
  private readonly changeRef = inject(ChangeDetectorRef);
  readonly pick = output<TypeaheadSuggestion | string>();
  readonly query = input<string>('');
  readonly suggestions = input.required<TypeaheadSuggestion[]>();
  readonly queryChange = output<string>();
  readonly searchbar = viewChild<IonSearchbar>('searchbar');

  private subscriptions: Subscription[] = [];
  queryControl: FormControl<string> = new FormControl<string>('');
  // busy: boolean = false;
  focussedMatchIndex: number = -1;
  protected isFocussed: boolean = false;

  async ngOnInit() {
    this.subscriptions.push(
      this.queryControl.valueChanges.subscribe(async (queryString) => {
        // this.busy = true;
        this.changeRef.detectChanges();
        this.queryChange.emit(queryString);
        // this.busy = false;
        // this.changeRef.detectChanges();
      })
    );
    this.queryControl.setValue(this.query());
  }

  async ngOnChanges(changes: SimpleChanges): Promise<void> {
    this.isFocussed = await this.hasFocus();
    this.changeRef.markForCheck();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  pickSuggestion(suggestion: TypeaheadSuggestion) {
    this.pick.emit(suggestion);
    this.isFocussed = true;
  }

  @HostListener('window:keydown.arrowup', ['$event'])
  handleKeyUp(event: KeyboardEvent) {
    console.log('up', this.focussedMatchIndex);
    if (this.focussedMatchIndex === 0) {
      return;
    }
    this.focussedMatchIndex--;
    if (this.focussedMatchIndex < 0) {
      this.focussedMatchIndex = this.getSuggestions().length - 1;
    }
    event.stopPropagation();
    event.preventDefault();
  }

  protected getSuggestions() {
    return this.suggestions() ?? [];
  }

  @HostListener('window:keydown.arrowdown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    console.log('down', this.focussedMatchIndex);
    if (this.focussedMatchIndex === this.getSuggestions().length - 1) {
      return;
    }
    this.focussedMatchIndex++;
    if (this.focussedMatchIndex > this.getSuggestions().length - 1) {
      this.focussedMatchIndex = 0;
    }
    event.stopPropagation();
    event.preventDefault();
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

  async setFocus() {
    const input = await this.searchbar().getInputElement();
    console.log('setFocus', input);
    input.focus();
  }

  onFocus() {
    console.log('focus');
    this.isFocussed = true;
  }

  // onBlur() {
  //   this.isFocussed = false;
  // }

  async hasFocus() {
    const input = await this.searchbar().getInputElement();
    return document.activeElement == input;
  }
}
