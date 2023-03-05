import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketPageRoutingModule } from './bucket-routing.module';

import { BucketPage } from './bucket.page';
import { FilterToolbarModule } from '../../../components/filter-toolbar/filter-toolbar.module';
import { ArticleRefModule } from '../../../components/article-ref/article-ref.module';
import { PageHeaderModule } from '../../../components/page-header/page-header.module';
import { ArticlesModule } from '../../../components/articles/articles.module';
import { ImportersModule } from '../../../components/importers/importers.module';
import { SubscribeModalModule } from '../../../modals/subscribe-modal/subscribe-modal.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BucketPageRoutingModule,
    ArticleRefModule,
    FilterToolbarModule,
    PageHeaderModule,
    ArticlesModule,
    ImportersModule,
    SubscribeModalModule,
  ],
  declarations: [BucketPage],
})
export class BucketPageModule {}
