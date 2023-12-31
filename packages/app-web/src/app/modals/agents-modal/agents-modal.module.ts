import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AgentsModalComponent } from './agents-modal.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';
import { AgentsModule } from '../../components/agents/agents.module';

@NgModule({
  declarations: [AgentsModalComponent],
  exports: [AgentsModalComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule, AgentsModule],
})
export class AgentsModalModule {}
