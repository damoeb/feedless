import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { PricingPageRoutingModule } from './pricing-routing.module';

import { PricingPage } from './pricing.page';
import { ProductHeadlineModule } from '../../components/product-headline/product-headline.module';
import { PlanColumnModule } from '../../components/plan-column/plan-column.module';
import { PricingModule } from '../../components/pricing/pricing.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    PricingPageRoutingModule,
    ProductHeadlineModule,
    ReactiveFormsModule,
    PlanColumnModule,
    PricingModule,
  ],
  declarations: [PricingPage],
})
export class PricingPageModule {}
