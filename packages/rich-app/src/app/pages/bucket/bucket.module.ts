import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketPageRoutingModule } from './bucket-routing.module';

import { BucketPage } from './bucket.page';
import { BucketSettingsModule } from '../../components/bucket-settings/bucket-settings.module';
import { FeedItemModule } from '../../components/feed-item/feed-item.module';
import { ToolbarModule } from '../../components/toolbar/toolbar.module';

@NgModule({
  imports: [
    CommonModule,
    BucketSettingsModule,
    FormsModule,
    IonicModule,
    BucketPageRoutingModule,
    FeedItemModule,
    ToolbarModule,
  ],
  declarations: [BucketPage],
})
export class BucketPageModule {}
