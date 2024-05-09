import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductHeaderComponent } from './product-header.component';
import { ProductHeadlineModule } from '../product-headline/product-headline.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';

@NgModule({
  declarations: [ProductHeaderComponent],
  exports: [ProductHeaderComponent],
  imports: [CommonModule, ProductHeadlineModule, SearchbarModule],
})
export class ProductHeaderModule {}
