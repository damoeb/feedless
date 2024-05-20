import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlanColumnComponent } from './plan-column.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [PlanColumnComponent],
  exports: [PlanColumnComponent],
  imports: [CommonModule, IonicModule, RouterLink],
})
export class PlanColumnModule {}
