import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedsPage } from './feeds.page';
import 'img-comparison-slider';
import { FeedsRoutingModule } from './feeds-routing.module';


import { ImportButtonModule } from '../../components/import-button/import-button.module';




import {
  IonBreadcrumb,
  IonBreadcrumbs,
  IonButton,
  IonButtons,
  IonChip,
  IonCol,
  IonContent,
  IonItem,
  IonLabel,
  IonList,
  IonProgressBar,
  IonRow,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FeedsRoutingModule,
    ImportButtonModule,
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    IonRow,
    IonCol,
    IonButtons,
    IonButton,
    IonProgressBar,
    IonList,
    IonItem,
    IonLabel,
    IonChip,
    FeedsPage,
],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class FeedsPageModule {}
