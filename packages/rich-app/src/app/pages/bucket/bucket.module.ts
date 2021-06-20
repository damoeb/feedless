import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketPageRoutingModule } from './bucket-routing.module';

import { BucketPage } from './bucket.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BucketPageRoutingModule
  ],
  declarations: [BucketPage]
})
export class BucketPageModule {}
