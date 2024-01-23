import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';
import { FeedBuilderPage } from './feed-builder.page';
import { TransformWebsiteToFeedModule } from '../../../components/transform-website-to-feed/transform-website-to-feed.module';
import { FeedBuilderActionsModalModule } from '../feed-builder-actions-modal/feed-builder-actions-modal.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ReactiveFormsModule,
    TransformWebsiteToFeedModule,
    FeedBuilderActionsModalModule
  ],
  declarations: [FeedBuilderPage],
})
export class FeedBuilderPageModule {}
