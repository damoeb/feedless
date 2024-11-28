import { Component, forwardRef, Input, OnInit } from '@angular/core';
import { isNull, isUndefined } from 'lodash-es';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { ControlValueAccessorDirective } from '../../directives/control-value-accessor/control-value-accessor.directive';
import { MenuComponent } from '../menu/menu.component';

export interface KeyLabelOption<T> {
  key: T;
  label: string;
  disabled?: boolean;
}

@Component({
  selector: 'app-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectComponent),
      multi: true,
    },
  ],
  imports: [MenuComponent],
  standalone: true,
})
export class SelectComponent<T>
  extends ControlValueAccessorDirective<T>
  implements OnInit
{
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

  ngOnInit() {
    super.ngOnInit();
  }

  label() {
    const currentValue = this.control.value;
    if (isUndefined(currentValue) || isNull(currentValue)) {
      return this.placeholder;
    } else {
      return this.items.find((item) => item.key === currentValue).label;
    }
  }

  setValue(value: T) {
    this.control.setValue(value);
  }
}
