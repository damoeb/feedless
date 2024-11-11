import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AgentsComponent } from './agents.component';
import { IonList, IonItem, IonLabel, IonChip } from '@ionic/angular/standalone';

@NgModule({
  declarations: [AgentsComponent],
  exports: [AgentsComponent],
  imports: [CommonModule, IonList, IonItem, IonLabel, IonChip],
})
export class AgentsModule {}
