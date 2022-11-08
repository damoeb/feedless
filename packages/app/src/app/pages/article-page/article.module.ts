import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketPageRoutingModule } from './article-routing.module';

import { ArticlePage } from './article.page';
import { ArticleModule } from '../../components/article/article.module';

@NgModule({
  imports: [CommonModule, FormsModule, IonicModule, BucketPageRoutingModule, ArticleModule],
  declarations: [ArticlePage],
})
export class ArticlePageModule {}
