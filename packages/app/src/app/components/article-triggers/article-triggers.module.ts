import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { FilterToolbarModule } from '../filter-toolbar/filter-toolbar.module';
import { RouterLink } from '@angular/router';
import { ArticleTriggersComponent } from './article-triggers.component';

@NgModule({
  declarations: [ArticleTriggersComponent],
  exports: [ArticleTriggersComponent],
  imports: [CommonModule, IonicModule, FilterToolbarModule, RouterLink],
})
export class ArticleTriggersModule {}
