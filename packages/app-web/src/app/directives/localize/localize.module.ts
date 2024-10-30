import { NgModule } from '@angular/core';
import { LocalizeDirective } from './localize.directive';
import { LocalizeAttrDirective } from './localize-attr.directive';

@NgModule({
  declarations: [LocalizeDirective, LocalizeAttrDirective],
  exports: [LocalizeDirective, LocalizeAttrDirective],
})
export class LocalizeModule {}
