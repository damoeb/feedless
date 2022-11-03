import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { FeedPageRoutingModule } from './feed-routing.module';

import { FeedPage } from './feed.page';
import { ArticleModule } from '../article/article.module';
import { PaginatedModule } from '../paginated/paginated.module';
import { NativeFeedComponent } from '../native-feed/native-feed.component';

@NgModule({
  imports: [CommonModule, FormsModule, IonicModule, FeedPageRoutingModule, ArticleModule, PaginatedModule],
  declarations: [FeedPage, NativeFeedComponent]
})
export class FeedPageModule {}
