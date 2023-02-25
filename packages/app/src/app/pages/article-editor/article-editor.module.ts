import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ArticleEditorPageRoutingModule } from './article-editor-routing.module';

import { ArticleEditorPage } from './article-editor.page';
import { ArticleRefModule } from '../../components/article-ref/article-ref.module';
import { PlayerModule } from '../../components/player/player.module';
import { ArticleEditorModule } from '../../components/article-editor/article-editor.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ArticleEditorPageRoutingModule,
    ArticleRefModule,
    PlayerModule,
    ArticleEditorModule,
  ],
  declarations: [ArticleEditorPage],
})
export class ArticleEditorPageModule {}
