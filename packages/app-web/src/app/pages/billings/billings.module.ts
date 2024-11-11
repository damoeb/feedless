import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BillingsPage } from './billings.page';
import 'img-comparison-slider';
import { BillingsRoutingModule } from './billings-routing.module';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { HistogramModule } from '../../components/histogram/histogram.module';
import { ImportButtonModule } from '../../components/import-button/import-button.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import {
  IonContent,
  IonRow,
  IonCol,
  IonList,
  IonItem,
  IonLabel,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    BillingsRoutingModule,
    BubbleModule,
    HistogramModule,
    ImportButtonModule,
    FeedlessHeaderModule,
    IonContent,
    IonRow,
    IonCol,
    IonList,
    IonItem,
    IonLabel,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [BillingsPage],
})
export class BillingsPageModule {}
