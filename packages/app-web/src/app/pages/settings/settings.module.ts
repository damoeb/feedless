import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SettingsPageRoutingModule } from './settings-routing.module';

import { SettingsPage } from './settings.page';


import {
  IonCheckbox,
  IonContent,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonSelect,
  IonSelectOption,
  IonSpinner,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    SettingsPageRoutingModule,
    ReactiveFormsModule,
    IonContent,
    IonList,
    IonItem,
    IonSpinner,
    IonSelect,
    IonSelectOption,
    IonLabel,
    IonInput,
    IonCheckbox,
    SettingsPage,
],
})
export class SettingsPageModule {}
