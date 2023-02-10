import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NativeFeedComponent } from './native-feed.component';
import { IonicModule } from '@ionic/angular';
import { ArticleRefModule } from '../article-ref/article-ref.module';
import { RouterLink } from '@angular/router';
import { FilterToolbarModule } from '../filter-toolbar/filter-toolbar.module';

@NgModule({
  declarations: [NativeFeedComponent],
  exports: [NativeFeedComponent],
  imports: [CommonModule, IonicModule, ArticleRefModule, RouterLink, FilterToolbarModule]
})
export class NativeFeedModule {}
