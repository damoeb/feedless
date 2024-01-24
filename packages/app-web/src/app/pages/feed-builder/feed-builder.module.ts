import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { FeedBuilderPageRoutingModule } from './feed-builder-routing.module';
import { FeedBuilderPage } from './feed-builder.page';
import { TransformWebsiteToFeedModule } from '../../components/transform-website-to-feed/transform-website-to-feed.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { FeedBuilderActionsModalModule } from '../../components/feed-builder-actions-modal/feed-builder-actions-modal.module';
import { GenerateFeedModalModule } from '../../modals/generate-feed-modal/generate-feed-modal.module';


@NgModule({
  imports: [CommonModule,
    IonicModule,
    FeedBuilderPageRoutingModule,
    TransformWebsiteToFeedModule,
    FeedBuilderActionsModalModule,
    GenerateFeedModalModule,
    SearchbarModule],
  declarations: [FeedBuilderPage]
})
export class FeedBuilderPageModule {
}
