import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleRefComponent } from './article-ref.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { BubbleModule } from '../bubble/bubble.module';
import { EnclosureModule } from '../enclosure/enclosure.module';

@NgModule({
  declarations: [ArticleRefComponent],
  exports: [ArticleRefComponent],
  imports: [
    CommonModule,
    IonicModule,
    RouterLink,
    BubbleModule,
    EnclosureModule,
  ],
})
export class ArticleRefModule {}
