import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AgentsComponent } from './agents.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [AgentsComponent],
  exports: [AgentsComponent],
  imports: [CommonModule, IonicModule]
})
export class AgentsModule {
}
