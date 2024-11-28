import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageChangeTrackerPageRoutingModule } from './change-tracker-product-routing.module';

import { ChangeTrackerProductPage } from './change-tracker-product.page';





import {
  IonHeader,
  IonToolbar,
  IonButtons,
  IonMenuButton,
  IonButton,
  IonIcon,
  IonContent,
  IonRouterOutlet,
  IonFooter,
  IonChip,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    PageChangeTrackerPageRoutingModule,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    IonButton,
    IonIcon,
    IonContent,
    IonRouterOutlet,
    IonFooter,
    IonChip,
    ChangeTrackerProductPage,
],
})
export class ChangeTrackerProductModule {}
