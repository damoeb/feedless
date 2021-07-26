import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleComponent } from './article.component';
import { IonicModule } from '@ionic/angular';
import { RouterModule } from '@angular/router';
import { BubbleModule } from '../bubble/bubble.module';

@NgModule({
  declarations: [ArticleComponent],
  exports: [ArticleComponent],
  imports: [CommonModule, IonicModule, RouterModule, BubbleModule],
})
export class ArticleModule {}
