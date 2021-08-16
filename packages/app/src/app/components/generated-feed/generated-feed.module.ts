import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GeneratedFeedComponent } from './generated-feed.component';
import { IonicModule } from '@ionic/angular';
import { ArticleModule } from '../article/article.module';

@NgModule({
  declarations: [GeneratedFeedComponent],
  exports: [GeneratedFeedComponent],
  imports: [CommonModule, IonicModule, ArticleModule],
})
export class GeneratedFeedModule {}
