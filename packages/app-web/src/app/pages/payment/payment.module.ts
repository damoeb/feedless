import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PaymentPageRoutingModule } from './payment-routing.module';

import { PaymentPage } from './payment.page';

import { IonContent, IonSpinner } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    PaymentPageRoutingModule,
    ReactiveFormsModule,
    IonContent,
    IonSpinner,
    PaymentPage,
],
})
export class PaymentPageModule {}
