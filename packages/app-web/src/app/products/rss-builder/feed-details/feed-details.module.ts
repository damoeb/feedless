import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { FeedDetailsPage } from './feed-details.page';
import 'img-comparison-slider';
import { FeedDetailsRoutingModule } from './feed-details-routing.module';
import { FeedBuilderModalModule } from '../../../modals/feed-builder-modal/feed-builder-modal.module';
import { BubbleModule } from '../../../components/bubble/bubble.module';
import { LoginButtonModule } from '../../../components/login-button/login-button.module';
import { HistogramModule } from '../../../components/histogram/histogram.module';

@NgModule({
  imports: [CommonModule, IonicModule, FeedDetailsRoutingModule, FeedBuilderModalModule, BubbleModule, LoginButtonModule, HistogramModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [FeedDetailsPage],
})
export class FeedDetailsPageModule {}
