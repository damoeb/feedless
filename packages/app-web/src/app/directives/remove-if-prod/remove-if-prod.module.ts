import { NgModule } from '@angular/core';
import { RemoveIfProdDirective } from './remove-if-prod.directive';

@NgModule({
  declarations: [RemoveIfProdDirective],
  exports: [RemoveIfProdDirective],
})
export class RemoveIfProdModule {}
