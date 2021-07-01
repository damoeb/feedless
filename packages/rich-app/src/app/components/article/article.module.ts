import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleComponent } from './article.component';

@NgModule({
  declarations: [ArticleComponent],
  exports: [ArticleComponent],
  imports: [CommonModule],
})
export class ArticleModule {}
