import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { FeedsPage } from './feeds.page';
import 'img-comparison-slider';
import { FeedsRoutingModule } from './feeds-routing.module';
import { BubbleModule } from '../../../components/bubble/bubble.module';
import { HistogramModule } from '../../../components/histogram/histogram.module';
import { OpmlPickerModule } from '../../../components/opml-picker/opml-picker.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FeedsRoutingModule,
    BubbleModule,
    HistogramModule,
    OpmlPickerModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [FeedsPage],
})
export class FeedsPageModule {}
