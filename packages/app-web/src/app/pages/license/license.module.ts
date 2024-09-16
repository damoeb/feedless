import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { LicensePageRoutingModule } from './license-routing.module';

import { LicensePage } from './license.page';
import { PlanColumnModule } from '../../components/plan-column/plan-column.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    LicensePageRoutingModule,
    PlanColumnModule,
    ReactiveFormsModule,
    FeedlessHeaderModule,
  ],
  declarations: [LicensePage],
})
export class LicensePageModule {}
