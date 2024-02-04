import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { FeedBuilderComponent } from './feed-builder.component';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { TransformWebsiteToFeedModule } from '../transform-website-to-feed/transform-website-to-feed.module';
import { FeedBuilderActionsModalModule } from '../feed-builder-actions-modal/feed-builder-actions-modal.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    TransformWebsiteToFeedModule,
    FeedBuilderActionsModalModule,
    SearchbarModule
  ],
  declarations: [FeedBuilderComponent],
  exports: [
    FeedBuilderComponent
  ]
})
export class FeedBuilderModule {}
