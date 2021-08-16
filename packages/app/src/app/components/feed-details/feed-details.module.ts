import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedDetailsComponent } from './feed-details.component';
import { IonicModule } from '@ionic/angular';
import { BubbleModule } from '../bubble/bubble.module';

@NgModule({
  declarations: [FeedDetailsComponent],
  exports: [FeedDetailsComponent],
  imports: [CommonModule, IonicModule, BubbleModule],
})
export class FeedDetailsModule {}
