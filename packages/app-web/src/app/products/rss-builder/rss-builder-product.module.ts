import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RssBuilderPageRoutingModule } from './rss-builder-product-routing.module';

import { RssBuilderProductPage } from './rss-builder-product.page';








import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonMenuButton,
  IonRouterOutlet,
  IonToolbar,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    RssBuilderPageRoutingModule,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    IonButton,
    IonIcon,
    IonContent,
    IonRouterOutlet,
    RssBuilderProductPage,
],
})
export class RssBuilderProductModule {}
