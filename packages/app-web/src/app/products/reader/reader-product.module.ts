import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReaderProductRoutingModule } from './reader-product-routing.module';

import { ReaderProductPage } from './reader-product.page';





import {
  IonButton,
  IonButtons,
  IonCol,
  IonContent,
  IonFooter,
  IonHeader,
  IonIcon,
  IonItem,
  IonItemDivider,
  IonLabel,
  IonList,
  IonMenuButton,
  IonPopover,
  IonRow,
  IonSegment,
  IonSegmentButton,
  IonSpinner,
  IonText,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReaderProductRoutingModule,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    IonButton,
    IonIcon,
    IonPopover,
    IonTitle,
    IonContent,
    IonList,
    IonItem,
    IonLabel,
    IonItemDivider,
    IonText,
    IonSpinner,
    IonSegment,
    IonSegmentButton,
    IonRow,
    IonCol,
    IonFooter,
    ReaderProductPage,
],
})
export class ReaderProductModule {}
