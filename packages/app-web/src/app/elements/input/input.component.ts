import { Component, forwardRef, Input, OnInit } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { ControlValueAccessorDirective } from '../../directives/control-value-accessor/control-value-accessor.directive';

@Component({
  selector: 'app-input',
  templateUrl: './input.component.html',
  styleUrls: ['./input.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputComponent),
      multi: true,
    },
  ],
})
export class InputComponent<T>
  extends ControlValueAccessorDirective<T>
  implements OnInit
{
  @Input()
  placeholder: string = '';

  @Input()
  type: string = '';

  @Input()
  color: string = 'light';

  ngOnInit() {
    super.ngOnInit();
  }

  setValue(value: T) {
    this.control.setValue(value);
  }
}
