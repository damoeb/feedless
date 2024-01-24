import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';

@Component({
  selector: 'app-searchbar',
  templateUrl: './searchbar.component.html',
  styleUrls: ['./searchbar.component.scss']
})
export class SearchbarComponent implements OnInit, OnChanges {

  @Input()
  value: string;

  @Input()
  buttonText: string;

  @Input()
  placeholder: string;

  @Input()
  color: string;

  @Output()
  querySubmit: EventEmitter<string> = new EventEmitter<string>();

  queryFc = new FormControl<string>('', [Validators.required]);

  constructor() {
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
