import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BuyPageRoutingModule } from './buy-routing.module';

import { BuyPage } from './buy.page';
import { NewsletterModule } from '../../components/newsletter/newsletter.module';
import { ProductHeadlineModule } from '../../components/product-headline/product-headline.module';
import { PlanColumnModule } from '../../components/plan-column/plan-column.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BuyPageRoutingModule,
    NewsletterModule,
    ProductHeadlineModule,
    ReactiveFormsModule,
    PlanColumnModule,
  ],
  declarations: [BuyPage],
})
export class BuyPageModule {}
