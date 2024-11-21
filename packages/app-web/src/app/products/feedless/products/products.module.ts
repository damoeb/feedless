import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductsPageRoutingModule } from './products-routing.module';

import { ProductsPage } from './products.page';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';
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
    ProductHeadlineModule,
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    IonToolbar,
    IonButton,
    IonIcon,
  ],
  declarations: [ProductsPage],
})
export class ProductsPageModule {}
