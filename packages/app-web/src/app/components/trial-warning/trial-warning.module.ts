import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrialWarningComponent } from './trial-warning.component';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { IonToolbar, IonText, IonButton } from '@ionic/angular/standalone';

@NgModule({
  declarations: [TrialWarningComponent],
  exports: [TrialWarningComponent],
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    IonToolbar,
    IonText,
    IonButton,
  ],
})
export class TrialWarningModule {}
