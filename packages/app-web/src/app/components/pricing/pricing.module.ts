import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { PricingComponent } from './pricing.component';
import { ProductHeadlineModule } from '../../components/product-headline/product-headline.module';
import { PlanColumnModule } from '../../components/plan-column/plan-column.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ProductHeadlineModule,
    ReactiveFormsModule,
    PlanColumnModule
  ],
  declarations: [PricingComponent],
  exports: [
    PricingComponent
  ]
})
export class PricingModule {}
