import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReaderProductRoutingModule } from './reader-product-routing.module';

import { ReaderProductPage } from './reader-product.page';
import { ReaderModule } from '../../components/reader/reader.module';
import { EmbeddedMarkupModule } from '../../components/embedded-markup/embedded-markup.module';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { ReaderMenuModule } from './reader-menu/reader-menu.module';
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
    ReaderMenuModule,
    ReaderProductRoutingModule,
    ReaderModule,
    EmbeddedMarkupModule,
    DarkModeButtonModule,
    SearchbarModule,
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
  ],
  declarations: [ReaderProductPage],
})
export class ReaderProductModule {}
