import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GenerateFeedModalComponent } from './generate-feed-modal.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [GenerateFeedModalComponent],
  exports: [GenerateFeedModalComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class GenerateFeedModalModule {}
