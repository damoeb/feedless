import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { FeedBuilderComponent } from './feed-builder.component';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { TransformWebsiteToFeedModule } from '../transform-website-to-feed/transform-website-to-feed.module';
import { InteractiveWebsiteModalModule } from '../../modals/interactive-website-modal/interactive-website-modal.module';
import { TagsModalModule } from '../../modals/tags-modal/tags-modal.module';
import { SearchAddressModalModule } from '../../modals/search-address-modal/search-address-modal.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    TransformWebsiteToFeedModule,
    TagsModalModule,
    InteractiveWebsiteModalModule,
    SearchbarModule,
    SearchAddressModalModule,
  ],
  declarations: [FeedBuilderComponent],
  exports: [FeedBuilderComponent],
})
export class FeedBuilderModule {}
