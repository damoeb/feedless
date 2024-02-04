import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { FeedBuilderPageRoutingModule } from './feed-builder-routing.module';
import { FeedBuilderPage } from './feed-builder.page';
import { FeedBuilderModule } from '../../components/feed-builder/feed-builder.module';
import { GenerateFeedModalModule } from '../../modals/generate-feed-modal/generate-feed-modal.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FeedBuilderPageRoutingModule,
    GenerateFeedModalModule,
    FeedBuilderModule,
  ],
  declarations: [FeedBuilderPage],
})
export class FeedBuilderPageModule {}
