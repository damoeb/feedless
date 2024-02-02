import { NgModule } from '@angular/core';
import { ControlValueAccessorDirective } from './control-value-accessor.directive';

@NgModule({
  declarations: [ControlValueAccessorDirective],
  exports: [ControlValueAccessorDirective],
})
export class ControlValueAccessorModule {}
