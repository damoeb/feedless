import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { FeedDetailsPage } from './feed-details.page';
import 'img-comparison-slider';
import { FeedDetailsRoutingModule } from './feed-details-routing.module';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { HistogramModule } from '../../components/histogram/histogram.module';
import { ReaderModule } from '../../components/reader/reader.module';
import { FormsModule } from '@angular/forms';
import { TagsModalModule } from '../../modals/tags-modal/tags-modal.module';
import { FeedDetailsModule } from '../../components/feed-details/feed-details.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FeedDetailsRoutingModule,
    BubbleModule,
    LoginButtonModule,
    HistogramModule,
    TagsModalModule,
    ReaderModule,
    FormsModule,
    FeedDetailsModule,
    FeedlessHeaderModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [FeedDetailsPage],
})
export class FeedDetailsPageModule {}
