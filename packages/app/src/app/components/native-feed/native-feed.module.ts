import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NativeFeedComponent } from './native-feed.component';
import { IonicModule } from '@ionic/angular';
import { ArticleModule } from '../article/article.module';
import { RouterLink } from '@angular/router';
import { FilterToolbarModule } from '../filter-toolbar/filter-toolbar.module';

@NgModule({
  declarations: [NativeFeedComponent],
  exports: [NativeFeedComponent],
  imports: [CommonModule, IonicModule, ArticleModule, RouterLink, FilterToolbarModule]
})
export class NativeFeedModule {}
