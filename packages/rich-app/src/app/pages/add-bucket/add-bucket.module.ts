import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { AddBucketPageRoutingModule } from './add-bucket-routing.module';

import { AddBucketPage } from './add-bucket.page';
import {BucketModule} from '../../components/bucket/bucket.module';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        IonicModule,
        AddBucketPageRoutingModule,
        BucketModule
    ],
  declarations: [AddBucketPage]
})
export class AddBucketPageModule {}
