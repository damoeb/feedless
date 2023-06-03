import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PuppeteerEvaluateModalComponent } from './puppeteer-evaluate-modal.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [PuppeteerEvaluateModalComponent],
  exports: [PuppeteerEvaluateModalComponent],
  imports: [CommonModule, IonicModule, FormsModule, ReactiveFormsModule],
})
export class PuppeteerEvaluateModalModule {}
