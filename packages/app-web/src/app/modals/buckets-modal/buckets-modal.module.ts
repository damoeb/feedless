import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketEditModule } from '../../components/bucket-edit/bucket-edit.module';
import { IonicModule } from '@ionic/angular';
import { BucketsModalComponent } from './buckets-modal.component';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [BucketsModalComponent],
  exports: [BucketsModalComponent],
  imports: [CommonModule, BucketEditModule, IonicModule, ReactiveFormsModule]
})
export class BucketsModalModule {}
