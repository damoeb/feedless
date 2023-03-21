import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketEditModule } from '../../components/bucket-edit/bucket-edit.module';
import { BucketCreateModalComponent } from './bucket-create-modal.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [BucketCreateModalComponent],
  exports: [BucketCreateModalComponent],
  imports: [CommonModule, BucketEditModule, IonicModule],
})
export class BucketCreateModalModule {}
