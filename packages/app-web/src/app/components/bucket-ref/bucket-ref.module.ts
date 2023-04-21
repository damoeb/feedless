import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketRefComponent } from './bucket-ref.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { BubbleModule } from '../bubble/bubble.module';
import { EnclosureModule } from '../enclosure/enclosure.module';
import { HistogramModule } from '../histogram/histogram.module';

@NgModule({
  declarations: [BucketRefComponent],
  exports: [BucketRefComponent],
  imports: [
    CommonModule,
    IonicModule,
    RouterLink,
    BubbleModule,
    EnclosureModule,
    HistogramModule,
  ],
})
export class BucketRefModule {}
