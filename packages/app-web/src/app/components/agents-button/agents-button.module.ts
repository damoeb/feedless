import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AgentsButtonComponent } from './agents-button.component';
import { IonicModule } from '@ionic/angular';
import { BubbleModule } from '../bubble/bubble.module';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [AgentsButtonComponent],
  exports: [AgentsButtonComponent],
  imports: [CommonModule, IonicModule, BubbleModule, RouterLink]
})
export class AgentsButtonModule {}
