import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketPageRoutingModule } from './bucket-routing.module';

import { BucketPage } from './bucket.page';
import { ArticleRefModule } from '../../components/article-ref/article-ref.module';
import { FilterToolbarModule } from '../../components/filter-toolbar/filter-toolbar.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BucketPageRoutingModule,
    ArticleRefModule,
    FilterToolbarModule,
  ],
  declarations: [BucketPage],
})
export class BucketPageModule {}
