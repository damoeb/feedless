import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { PaymentConfirmationPageRoutingModule } from './payment-summary-routing.module';

import { PaymentSummaryPage } from './payment-summary.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    PaymentConfirmationPageRoutingModule,
    ReactiveFormsModule,
  ],
  declarations: [PaymentSummaryPage],
})
export class PaymentSummaryPageModule {}
