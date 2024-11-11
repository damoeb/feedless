import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PaymentConfirmationPageRoutingModule } from './payment-summary-routing.module';

import { PaymentSummaryPage } from './payment-summary.page';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    PaymentConfirmationPageRoutingModule,
    ReactiveFormsModule,
    FeedlessHeaderModule,
    IonContent,
  ],
  declarations: [PaymentSummaryPage],
})
export class PaymentSummaryPageModule {}
