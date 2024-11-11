import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PricingPageRoutingModule } from './pricing-routing.module';

import { PricingPage } from './pricing.page';
import { ProductHeadlineModule } from '../../components/product-headline/product-headline.module';
import { PlanColumnModule } from '../../components/plan-column/plan-column.module';
import { PricingModule } from '../../components/pricing/pricing.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    PricingPageRoutingModule,
    ProductHeadlineModule,
    ReactiveFormsModule,
    PlanColumnModule,
    PricingModule,
    FeedlessHeaderModule,
    IonContent,
  ],
  declarations: [PricingPage],
})
export class PricingPageModule {}
