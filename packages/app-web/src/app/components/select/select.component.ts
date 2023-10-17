import { Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation } from '@angular/core';
import { isNull, isObject, isString, isUndefined } from 'lodash-es';
import { labelProvider } from '../menu/menu.component';

export interface KeyLabelOption<T> {
  key: T,
  label: string,
  default?: boolean
  disabled?: boolean
}


@Component({
  selector: 'app-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss']
})
export class SelectComponent<T> implements OnInit {

  @Input()
  required: string | boolean;

  @Input()
  hideFilter: boolean = false;

  @Input()
  placeholder: string;

  @Input()
  disabled: boolean = false;

  @Input()
  color: string = 'light';

  @Input()
  labelFn: keyof T | ((value: T) => string);

  @Output()
  valueChanged: EventEmitter<T> = new EventEmitter<T>();

  @Input({ required: true })
  items: T[];

  @Input()
  value: T;

  invalid: boolean;
  currentValue: T;

  constructor() {
  }

  label() {
    if (isUndefined(this.currentValue) || isNull(this.currentValue)) {
      return this.placeholder;
    } else {
      return labelProvider<T>(this.currentValue, this.labelFn);
    }
  }

  ngOnInit(): void {
    if (!this.value && this.items.length > 0 && isObject(this.items[0])) {
      this.value = this.items.find(o => o['default'] === true)
    }
    this.currentValue = this.value;

    if (this.isRequired() && !this.hasValue(this.currentValue)) {
      this.invalid = true;
    }
  }

  private isRequired(): boolean {
    return isString(this.required) && this.required === 'true' || this.required === true;
  }

  private hasValue(value: any): boolean {
    return !(isNull(value) || isUndefined(value));
  }

  handleValueChanged(value: T) {
    this.currentValue = value;
    this.invalid = this.isRequired() && !this.hasValue(value);
    this.valueChanged.emit(value);
  }
}
