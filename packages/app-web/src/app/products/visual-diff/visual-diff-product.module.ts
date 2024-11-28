import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VisualDiffProductRoutingModule } from './visual-diff-product-routing.module';

import { VisualDiffProductPage } from './visual-diff-product.page';






import {
  IonHeader,
  IonToolbar,
  IonButtons,
  IonMenuButton,
  IonButton,
  IonContent,
  IonRouterOutlet,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    VisualDiffProductRoutingModule,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    IonButton,
    IonContent,
    IonRouterOutlet,
    VisualDiffProductPage,
],
})
export class VisualDiffProductModule {}
