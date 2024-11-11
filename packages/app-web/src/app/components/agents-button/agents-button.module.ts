import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AgentsButtonComponent } from './agents-button.component';
import { BubbleModule } from '../bubble/bubble.module';
import { RouterLink } from '@angular/router';
import { IonButton, IonLabel, IonChip } from '@ionic/angular/standalone';

@NgModule({
  declarations: [AgentsButtonComponent],
  exports: [AgentsButtonComponent],
  imports: [
    CommonModule,
    BubbleModule,
    RouterLink,
    IonButton,
    IonLabel,
    IonChip,
  ],
})
export class AgentsButtonModule {}
