import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ProductsPageRoutingModule } from './products-routing.module';

import { ProductsPage } from './products.page';
import { NewsletterModule } from '../../../components/newsletter/newsletter.module';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ProductsPageRoutingModule,
    NewsletterModule,
    ProductHeadlineModule
  ],
  declarations: [ProductsPage],
})
export class ProductsPageModule {}
