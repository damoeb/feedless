import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlanColumnComponent } from './plan-column.component';
import { RouterLink } from '@angular/router';
import { IonRow, IonCol, IonIcon } from '@ionic/angular/standalone';

@NgModule({
  declarations: [PlanColumnComponent],
  exports: [PlanColumnComponent],
  imports: [CommonModule, RouterLink, IonRow, IonCol, IonIcon],
})
export class PlanColumnModule {}
