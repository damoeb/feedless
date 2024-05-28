import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedDetailsComponent } from './feed-details.component';
import { IonicModule } from '@ionic/angular';
import { BubbleModule } from '../bubble/bubble.module';
import { ReaderModule } from '../reader/reader.module';
import { FeedBuilderModalModule } from '../../modals/feed-builder-modal/feed-builder-modal.module';
import { GenerateFeedModalModule } from '../../modals/generate-feed-modal/generate-feed-modal.module';
import { PaginationModule } from '../pagination/pagination.module';
import { ReactiveFormsModule } from '@angular/forms';
import { PlayerModule } from '../player/player.module';
import { ImageDiffModule } from '../image-diff/image-diff.module';
import { TextDiffModule } from '../text-diff/text-diff.module';
import { HistogramModule } from '../histogram/histogram.module';

@NgModule({
  declarations: [FeedDetailsComponent],
  exports: [FeedDetailsComponent],
  imports: [
    CommonModule,
    IonicModule,
    BubbleModule,
    ReaderModule,
    FeedBuilderModalModule,
    GenerateFeedModalModule,
    PaginationModule,
    ReactiveFormsModule,
    PlayerModule,
    ImageDiffModule,
    TextDiffModule,
    HistogramModule
  ]
})
export class FeedDetailsModule {}
