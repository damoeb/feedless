import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';

@Component({
  selector: 'app-searchbar',
  templateUrl: './searchbar.component.html',
  styleUrls: ['./searchbar.component.scss'],
})
export class SearchbarComponent implements OnInit {

  @Input()
  value: string;

  @Input()
  placeholder: string;

  @Output()
  querySubmit: EventEmitter<string> = new EventEmitter<string>();

  urlFc = new FormControl<string>('', [Validators.required]);

  constructor() {}

  triggerUpdate() {
    this.querySubmit.emit(this.urlFc.value)
  }

  ngOnInit(): void {
    this.urlFc.setValue(this.value)
  }
}
