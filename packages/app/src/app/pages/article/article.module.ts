import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ArticlePageRoutingModule } from './article-routing.module';

import { ArticlePage } from './article.page';
import { ArticleRefModule } from '../../components/article-ref/article-ref.module';
import { PlayerModule } from '../../components/player/player.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ArticlePageRoutingModule,
    ArticleRefModule,
    PlayerModule,
  ],
  declarations: [ArticlePage],
})
export class ArticlePageModule {}
