import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UntoldNotesPageRoutingModule } from './untold-notes-product-routing.module';

import { UntoldNotesProductPage } from './untold-notes-product.page';


import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { OfflineModule } from '../../offline.module';
import {
  IonHeader,
  IonToolbar,
  IonButtons,
  IonMenuButton,
  IonContent,
  IonRouterOutlet,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    UntoldNotesPageRoutingModule,
    ReactiveFormsModule,
    FormsModule,
    OfflineModule,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    IonContent,
    IonRouterOutlet,
    UntoldNotesProductPage,
],
})
export class UntoldNotesProductModule {}
