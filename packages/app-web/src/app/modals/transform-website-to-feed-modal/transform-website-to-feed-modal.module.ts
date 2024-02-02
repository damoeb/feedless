import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransformWebsiteToFeedModalComponent } from './transform-website-to-feed-modal.component';
import { IonicModule } from '@ionic/angular';
import { TransformWebsiteToFeedModule } from '../../components/transform-website-to-feed/transform-website-to-feed.module';

@NgModule({
  declarations: [TransformWebsiteToFeedModalComponent],
  exports: [TransformWebsiteToFeedModalComponent],
  imports: [CommonModule, IonicModule, TransformWebsiteToFeedModule],
})
export class TransformWebsiteToFeedModalModule {}
