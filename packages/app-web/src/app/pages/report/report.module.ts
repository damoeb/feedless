import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReportPage } from './report.page';
import 'img-comparison-slider';
import { ReportRoutingModule } from './report-routing.module';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { HistogramModule } from '../../components/histogram/histogram.module';
import { ImportButtonModule } from '../../components/import-button/import-button.module';
import { PaginationModule } from '../../components/pagination/pagination.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { TableModule } from '../../components/table/table.module';
import { RemoveIfProdModule } from '../../directives/remove-if-prod/remove-if-prod.module';
import { IonCol, IonContent, IonRow } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    ReportRoutingModule,
    BubbleModule,
    HistogramModule,
    ImportButtonModule,
    PaginationModule,
    FeedlessHeaderModule,
    TableModule,
    RemoveIfProdModule,
    IonContent,
    IonRow,
    IonCol,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [ReportPage],
})
export class ReportPageModule {}
