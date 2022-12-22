import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketPageRoutingModule } from './bucket-routing.module';

import { BucketPage } from './bucket.page';
import { ArticleModule } from '../../components/article/article.module';
import { CheckableItemModule } from '../../components/checkable-item/checkable-item.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BucketPageRoutingModule,
    ArticleModule,
    CheckableItemModule
  ],
  declarations: [BucketPage],
})
export class BucketPageModule {}
