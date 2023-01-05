import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketPageRoutingModule } from './article-routing.module';

import { ArticlePage } from './article.page';
import { ArticleModule } from '../../components/article/article.module';
import { PlayerModule } from '../../components/player/player.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BucketPageRoutingModule,
    ArticleModule,
    PlayerModule,
  ],
  declarations: [ArticlePage],
})
export class ArticlePageModule {}
