import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { addIcons } from 'ionicons';
import { chevronForwardOutline } from 'ionicons/icons';

@Component({
  selector: 'app-searchbar',
  templateUrl: './searchbar.component.html',
  styleUrls: ['./searchbar.component.scss'],
})
export class SearchbarComponent implements OnInit, OnChanges {
  @Input()
  value: string;

  @Input()
  loading: boolean;

  @Input()
  buttonText: string;

  @Input()
  placeholder: string;

  @Input()
  color: string;

  @Output()
  querySubmit: EventEmitter<string> = new EventEmitter<string>();

  @Output()
  cancelReceived: EventEmitter<void> = new EventEmitter<void>();

  queryFc = new FormControl<string>('', [Validators.required]);

  constructor() {
    addIcons({ chevronForwardOutline });
  }

  triggerUpdate() {
    this.querySubmit.emit(this.queryFc.value);
  }

  ngOnInit(): void {
    this.queryFc.setValue(this.value);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.value) {
      this.queryFc.setValue(changes.value.currentValue);
    }
  }
}
