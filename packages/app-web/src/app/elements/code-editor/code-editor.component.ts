import { Component, forwardRef, OnInit } from '@angular/core';
import { ControlValueAccessorDirective } from '../../directives/control-value-accessor/control-value-accessor.directive';
import { NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-code-editor',
  templateUrl: './code-editor.component.html',
  styleUrls: ['./code-editor.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CodeEditorComponent),
      multi: true,
    },
  ],
})
export class CodeEditorComponent<T>
  extends ControlValueAccessorDirective<T>
  implements OnInit
{
  ngOnInit() {
    super.ngOnInit();
  }
}
