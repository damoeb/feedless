import { Component, forwardRef, input, OnInit } from '@angular/core';
import {
  FormsModule,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { ControlValueAccessorDirective } from '@feedless/directives';
import { IonInput } from '@ionic/angular/standalone';

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
  imports: [IonInput, FormsModule, ReactiveFormsModule],
  standalone: true,
})
export class InputComponent<T>
  extends ControlValueAccessorDirective<T>
  implements OnInit
{
  readonly placeholder = input<string>('');

  readonly type = input<string>('text');

  readonly color = input<string>('light');

  ngOnInit() {
    super.ngOnInit();
  }

  setValue(value: T) {
    this.control.setValue(value);
  }
}
