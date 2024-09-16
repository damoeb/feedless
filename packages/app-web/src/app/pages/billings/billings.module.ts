import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { BillingsPage } from './billings.page';
import 'img-comparison-slider';
import { BillingsRoutingModule } from './billings-routing.module';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { HistogramModule } from '../../components/histogram/histogram.module';
import { ImportButtonModule } from '../../components/import-button/import-button.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    BillingsRoutingModule,
    BubbleModule,
    HistogramModule,
    ImportButtonModule,
    FeedlessHeaderModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [BillingsPage],
})
export class BillingsPageModule {}
