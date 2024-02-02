import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductHeadlineComponent } from './product-headline.component';

@NgModule({
  declarations: [ProductHeadlineComponent],
  exports: [ProductHeadlineComponent],
  imports: [CommonModule],
})
export class ProductHeadlineModule {}
