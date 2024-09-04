import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { FeedBuilderPageRoutingModule } from './feed-builder-routing.module';
import { FeedBuilderPage } from './feed-builder.page';
import { FeedBuilderModule } from '../../components/feed-builder/feed-builder.module';
import { RepositoryModalModule } from '../../modals/repository-modal/repository-modal.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FeedBuilderPageRoutingModule,
    RepositoryModalModule,
    FeedBuilderModule,
  ],
  declarations: [FeedBuilderPage],
})
export class FeedBuilderPageModule {}
