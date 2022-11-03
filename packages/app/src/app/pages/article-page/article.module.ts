import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketPageRoutingModule } from './article-routing.module';

import { ArticlePage } from './article.page';
import { ArticleModule } from '../article/article.module';
import { PaginatedModule } from '../paginated/paginated.module';

@NgModule({
  imports: [CommonModule, FormsModule, IonicModule, BucketPageRoutingModule, ArticleModule, PaginatedModule],
  declarations: [ArticlePage],
})
export class ArticlePageModule {}
