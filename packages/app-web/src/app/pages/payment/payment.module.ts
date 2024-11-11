import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PaymentPageRoutingModule } from './payment-routing.module';

import { PaymentPage } from './payment.page';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { IonContent, IonSpinner } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    PaymentPageRoutingModule,
    ReactiveFormsModule,
    FeedlessHeaderModule,
    IonContent,
    IonSpinner,
  ],
  declarations: [PaymentPage],
})
export class PaymentPageModule {}
