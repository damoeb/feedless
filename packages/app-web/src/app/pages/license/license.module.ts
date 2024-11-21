import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { LicensePageRoutingModule } from './license-routing.module';

import { LicensePage } from './license.page';
import { PlanColumnModule } from '../../components/plan-column/plan-column.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import {
  IonButton,
  IonCol,
  IonContent,
  IonIcon,
  IonItem,
  IonList,
  IonProgressBar,
  IonRow,
  IonSpinner,
  IonTextarea,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    LicensePageRoutingModule,
    PlanColumnModule,
    ReactiveFormsModule,
    FeedlessHeaderModule,
    IonContent,
    IonList,
    IonItem,
    IonSpinner,
    IonIcon,
    IonProgressBar,
    IonRow,
    IonCol,
    IonButton,
    IonTextarea,
  ],
  declarations: [LicensePage],
})
export class LicensePageModule {}
