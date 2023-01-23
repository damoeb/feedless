import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PreviewFeedModalComponent } from './preview-feed-modal.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [PreviewFeedModalComponent],
  exports: [PreviewFeedModalComponent],
  imports: [CommonModule, IonicModule, RouterLink],
})
export class PreviewFeedModalModule {}
