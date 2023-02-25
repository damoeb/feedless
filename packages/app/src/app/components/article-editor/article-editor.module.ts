import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleEditorComponent } from './article-editor.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { BubbleModule } from '../bubble/bubble.module';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [ArticleEditorComponent],
  exports: [ArticleEditorComponent],
  imports: [CommonModule, IonicModule, RouterLink, BubbleModule, FormsModule],
})
export class ArticleEditorModule {}
