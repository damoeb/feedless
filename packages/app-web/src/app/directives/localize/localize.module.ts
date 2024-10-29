import { NgModule } from '@angular/core';
import { LocalizeDirective } from './localize.directive';

@NgModule({
  declarations: [LocalizeDirective],
  exports: [LocalizeDirective],
})
export class LocalizeModule {}
