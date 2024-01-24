import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductTitleComponent } from './product-title.component';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [ProductTitleComponent],
  exports: [ProductTitleComponent],
  imports: [CommonModule, RouterLink]
})
export class ProductTitleModule {
}
