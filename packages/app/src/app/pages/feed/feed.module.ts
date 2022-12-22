import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { FeedPageRoutingModule } from './feed-routing.module';

import { FeedPage } from './feed.page';
import { ArticleModule } from '../../components/article/article.module';
import { NativeFeedModule } from '../../components/native-feed/native-feed.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    FeedPageRoutingModule,
    ArticleModule,
    NativeFeedModule
  ],
  declarations: [FeedPage],
})
export class FeedPageModule {}
