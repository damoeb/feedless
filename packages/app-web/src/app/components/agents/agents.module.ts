import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AgentsComponent } from './agents.component';

@NgModule({
  declarations: [AgentsComponent],
  exports: [AgentsComponent],
  imports: [CommonModule],
})
export class AgentsModule {}
