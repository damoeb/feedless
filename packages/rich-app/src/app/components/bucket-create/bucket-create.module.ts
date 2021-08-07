import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketCreateComponent } from './bucket-create.component';
import { IonicModule } from '@ionic/angular';
import { BucketModule } from '../bucket/bucket.module';

@NgModule({
  declarations: [BucketCreateComponent],
  exports: [BucketCreateComponent],
  imports: [CommonModule, IonicModule, BucketModule],
})
export class BucketCreateModule {}
