import { Component, Input } from '@angular/core';
import { isNull, isUndefined } from 'lodash-es';
import { FormControl } from '@angular/forms';

export interface KeyLabelOption<T> {
  key: T,
  label: string,
  disabled?: boolean
}

@Component({
  selector: 'app-select2',
  templateUrl: './select2.component.html',
  styleUrls: ['./select2.component.scss']
})
export class Select2Component<T> {

  @Input({required: true})
  formControl: FormControl<T>;

  @Input()
  hideFilter: boolean = false;

  @Input()
  placeholder: string = 'Empty';

  @Input()
  disabled: boolean = false;

  @Input()
  color: string = 'light';

  @Input({ required: true })
  items: KeyLabelOption<T>[];

  constructor() {
  }

  label() {
    if (!this.formControl.value && this.formControl.defaultValue) {
      this.formControl.setValue(this.formControl.defaultValue);
    }
    const currentValue = this.formControl.value;
    if (isUndefined(currentValue) || isNull(currentValue)) {
      return this.placeholder;
    } else {
      return this.items.find(item => item.key === currentValue).label;
    }
  }
}
