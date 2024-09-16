import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { SettingsPageRoutingModule } from './settings-routing.module';

import { SettingsPage } from './settings.page';
import { PlanColumnModule } from '../../components/plan-column/plan-column.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    SettingsPageRoutingModule,
    PlanColumnModule,
    ReactiveFormsModule,
    FeedlessHeaderModule,
  ],
  declarations: [SettingsPage],
})
export class SettingsPageModule {}
