import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BillingsPage } from './billings.page';
import 'img-comparison-slider';
import { BillingsRoutingModule } from './billings-routing.module';


import { ImportButtonModule } from '../../components/import-button/import-button.module';

import {
  IonCol,
  IonContent,
  IonItem,
  IonLabel,
  IonList,
  IonRow,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    BillingsRoutingModule,
    ImportButtonModule,
    IonContent,
    IonRow,
    IonCol,
    IonList,
    IonItem,
    IonLabel,
    BillingsPage,
],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class BillingsPageModule {}
