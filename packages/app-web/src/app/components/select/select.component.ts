import { Component, EventEmitter, Input, OnInit, Output, ViewChild, ViewEncapsulation } from '@angular/core';
import { isNull, isString, isUndefined } from 'lodash-es';
import { AppMenuOption, MenuComponent } from '../menu/menu.component';

export type AppSelectOption = AppMenuOption

@Component({
  selector: 'app-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class SelectComponent implements OnInit {

  @Input()
  label: string

  @Input()
  required: string|boolean

  @Input()
  hideFilter: boolean = false

  @Input()
  placeholder: string

  @Input()
  color: string = 'light'

  @Input()
  displayLabel: boolean = false

  @Output()
  valueChanged: EventEmitter<any> = new EventEmitter<any>()

  @Input()
  options: object | string[] | AppSelectOption[]

  @Input()
  value: any;

  @ViewChild('menu')
  menuElement: MenuComponent

  invalid: boolean;
  currentValue: any;

  constructor() {
  }


  getLabel() {
    if (this.displayLabel) {
      return this.label
    } else {
      if (this.currentValue && this.menuElement) {
        return this.menuElement.options.find(o => o.value == this.currentValue)?.label || this.placeholder
      } else {
        return this.placeholder
      }
    }
  }

  ngOnInit(): void {
    this.currentValue = this.value;
    if (this.isRequired() && !this.hasValue(this.value)) {
      this.invalid = true;
    }
  }

  private isRequired(): boolean {
    return isString(this.required) && this.required === 'true' || this.required === true;
  }

  private hasValue(value: any): boolean {
    return !(isNull(value) || isUndefined(value));
  }

  handleValueChanged(value: any) {
    this.currentValue = value;
    this.invalid = this.isRequired() && !this.hasValue(value);
    this.valueChanged.emit(value);
  }
}
