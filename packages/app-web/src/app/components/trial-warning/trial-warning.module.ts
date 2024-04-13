import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrialWarningComponent } from './trial-warning.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [TrialWarningComponent],
  exports: [TrialWarningComponent],
  imports: [CommonModule, IonicModule, RouterLink],
})
export class TrialWarningModule {}
