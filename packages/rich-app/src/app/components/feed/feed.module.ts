import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedComponent } from './feed.component';
import { IonicModule } from '@ionic/angular';
import { ArticleModule } from '../article/article.module';

@NgModule({
  declarations: [FeedComponent],
  exports: [FeedComponent],
  imports: [CommonModule, IonicModule, ArticleModule],
})
export class FeedModule {}
