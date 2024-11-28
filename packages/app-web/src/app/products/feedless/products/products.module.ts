import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductsPageRoutingModule } from './products-routing.module';

import { ProductsPage } from './products.page';

import {
  IonBreadcrumb,
  IonBreadcrumbs,
  IonButton,
  IonContent,
  IonIcon,
  IonToolbar,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ProductsPageRoutingModule,
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    IonToolbar,
    IonButton,
    IonIcon,
    ProductsPage,
],
})
export class ProductsPageModule {}
