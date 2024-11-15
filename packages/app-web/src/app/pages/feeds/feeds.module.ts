import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { FeedsPage } from './feeds.page';
import 'img-comparison-slider';
import { FeedsRoutingModule } from './feeds-routing.module';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { HistogramModule } from '../../components/histogram/histogram.module';
import { ImportButtonModule } from '../../components/import-button/import-button.module';
import { PaginationModule } from '../../components/pagination/pagination.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { TableModule } from '../../components/table/table.module';
import { RemoveIfProdModule } from '../../directives/remove-if-prod/remove-if-prod.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FeedsRoutingModule,
    BubbleModule,
    HistogramModule,
    ImportButtonModule,
    PaginationModule,
    FeedlessHeaderModule,
    TableModule,
    RemoveIfProdModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [FeedsPage],
})
export class FeedsPageModule {}
