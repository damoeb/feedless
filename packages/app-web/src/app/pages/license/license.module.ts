import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { LicensePageRoutingModule } from './license-routing.module';

import { LicensePage } from './license.page';
import { PlanColumnModule } from '../../components/plan-column/plan-column.module';
import { DiscountTagModule } from '../../components/discount-tag/discount-tag.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    LicensePageRoutingModule,
    PlanColumnModule,
    DiscountTagModule,
    ReactiveFormsModule
  ],
  declarations: [LicensePage],
})
export class LicensePageModule {}
