import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { FeedsPageRoutingModule } from './feeds-routing.module';

import { FeedsPage } from './feeds.page';
import { ArticleRefModule } from '../../components/article-ref/article-ref.module';
import { NativeFeedModule } from '../../components/native-feed/native-feed.module';
import { FilterToolbarModule } from '../../components/filter-toolbar/filter-toolbar.module';
import { PageHeaderModule } from '../../components/page-header/page-header.module';
import { NativeFeedRefModule } from '../../components/native-feed-ref/native-feed-ref.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    FeedsPageRoutingModule,
    ArticleRefModule,
    NativeFeedModule,
    FilterToolbarModule,
    PageHeaderModule,
    NativeFeedRefModule,
  ],
  declarations: [FeedsPage],
})
export class FeedsPageModule {}
