import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketPageRoutingModule } from './bucket-routing.module';

import { BucketPage } from './bucket.page';
import { FilterToolbarModule } from '../../../components/filter-toolbar/filter-toolbar.module';
import { PageHeaderModule } from '../../../components/page-header/page-header.module';
import { ArticlesModule } from '../../../components/articles/articles.module';
import { SubscribeModalModule } from '../../../modals/subscribe-modal/subscribe-modal.module';
import { BucketCreateModalModule } from '../../../modals/bucket-create-modal/bucket-create-modal.module';
import { ExternalLinkModule } from '../../../components/external-link/external-link.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BucketPageRoutingModule,
    FilterToolbarModule,
    PageHeaderModule,
    ArticlesModule,
    SubscribeModalModule,
    BucketCreateModalModule,
    ExternalLinkModule,
  ],
  declarations: [BucketPage],
})
export class BucketPageModule {}
