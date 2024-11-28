import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotebookDetailsPage } from './notebook-details.page';
import { NotebookDetailsRoutingModule } from './notebook-details-routing.module';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';




import {
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardTitle,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonMenu,
  IonPopover,
  IonProgressBar,
  IonSearchbar,
  IonSplitPane,
  IonText,
  IonToolbar,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    NotebookDetailsRoutingModule,
    ReactiveFormsModule,
    FormsModule,
    IonSplitPane,
    IonMenu,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    IonSearchbar,
    IonList,
    IonItem,
    IonLabel,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardContent,
    IonText,
    IonPopover,
    IonProgressBar,
    NotebookDetailsPage,
],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class NotebookDetailsPageModule {}
