import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SettingsPageRoutingModule } from './settings-routing.module';

import { SettingsPage } from './settings.page';
import { PlanColumnModule } from '../../components/plan-column/plan-column.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import {
  IonContent,
  IonList,
  IonItem,
  IonSpinner,
  IonSelect,
  IonSelectOption,
  IonLabel,
  IonInput,
  IonCheckbox,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    SettingsPageRoutingModule,
    PlanColumnModule,
    ReactiveFormsModule,
    FeedlessHeaderModule,
    IonContent,
    IonList,
    IonItem,
    IonSpinner,
    IonSelect,
    IonSelectOption,
    IonLabel,
    IonInput,
    IonCheckbox,
  ],
  declarations: [SettingsPage],
})
export class SettingsPageModule {}
