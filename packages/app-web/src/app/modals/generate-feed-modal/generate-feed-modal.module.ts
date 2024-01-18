import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GenerateFeedModalComponent } from './generate-feed-modal.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [GenerateFeedModalComponent],
  exports: [GenerateFeedModalComponent],
  imports: [
    CommonModule,
    IonicModule,
  ],
})
export class GenerateFeedModalModule {}
