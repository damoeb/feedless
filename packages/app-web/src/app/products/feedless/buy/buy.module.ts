import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ProductsPageRoutingModule } from './buy-routing.module';

import { BuyPage } from './buy.page';
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
  declarations: [BuyPage],
})
export class ProductsPageModule {}
