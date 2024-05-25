import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { SettingsPageRoutingModule } from './settings-routing.module';

import { SettingsPage } from './settings.page';
import { PlanColumnModule } from '../../components/plan-column/plan-column.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    SettingsPageRoutingModule,
    PlanColumnModule,
    ReactiveFormsModule,
  ],
  declarations: [SettingsPage],
})
export class SettingsPageModule {}
