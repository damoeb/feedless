import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CheckoutPageRoutingModule } from './checkout-routing.module';

import { CheckoutPage } from './checkout.page';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import {
  IonButton,
  IonCheckbox,
  IonCol,
  IonContent,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonListHeader,
  IonNote,
  IonRow,
  IonSelect,
  IonSelectOption,
  IonSpinner,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    CheckoutPageRoutingModule,
    ReactiveFormsModule,
    FeedlessHeaderModule,
    IonContent,
    IonSpinner,
    IonList,
    IonItem,
    IonRow,
    IonCol,
    IonInput,
    IonButton,
    IonListHeader,
    IonLabel,
    IonNote,
    IonSelect,
    IonSelectOption,
    IonCheckbox,
  ],
  declarations: [CheckoutPage],
})
export class CheckoutPageModule {}
