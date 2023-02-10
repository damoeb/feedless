import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketsPageRoutingModule } from './buckets-routing.module';

import { BucketsPage } from './buckets.page';
import { BubbleModule } from '../../components/bubble/bubble.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BucketsPageRoutingModule,
    BubbleModule,
  ],
  declarations: [BucketsPage],
})
export class BucketsPageModule {}
