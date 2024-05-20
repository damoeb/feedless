import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlansComponent } from './plans.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { PlanColumnModule } from '../plan-column/plan-column.module';

@NgModule({
  declarations: [PlansComponent],
  exports: [PlansComponent],
  imports: [CommonModule, IonicModule, RouterLink, PlanColumnModule]
})
export class PlansModule {}
