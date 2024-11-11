import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedBuilderPageRoutingModule } from './feed-builder-routing.module';
import { FeedBuilderPage } from './feed-builder.page';
import { FeedBuilderModule } from '../../components/feed-builder/feed-builder.module';
import { RepositoryModalModule } from '../../modals/repository-modal/repository-modal.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FeedBuilderPageRoutingModule,
    RepositoryModalModule,
    FeedBuilderModule,
    FeedlessHeaderModule,
    IonContent,
  ],
  declarations: [FeedBuilderPage],
})
export class FeedBuilderPageModule {}
