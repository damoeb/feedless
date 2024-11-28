import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PricingPageRoutingModule } from './pricing-routing.module';

import { PricingPage } from './pricing.page';




import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    PricingPageRoutingModule,
    ReactiveFormsModule,
    IonContent,
    PricingPage,
],
})
export class PricingPageModule {}
