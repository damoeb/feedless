import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DirectoryPage } from './directory.page';
import 'img-comparison-slider';
import { DirectoryRoutingModule } from './directory-routing.module';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { HistogramModule } from '../../components/histogram/histogram.module';
import { ImportButtonModule } from '../../components/import-button/import-button.module';
import { PaginationModule } from '../../components/pagination/pagination.module';
import { ProductHeaderModule } from '../../components/product-header/product-header.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { RemoveIfProdModule } from '../../directives/remove-if-prod/remove-if-prod.module';
import {
  IonButton,
  IonChip,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonProgressBar,
  IonRow,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    DirectoryRoutingModule,
    BubbleModule,
    HistogramModule,
    ImportButtonModule,
    PaginationModule,
    ProductHeaderModule,
    SearchbarModule,
    ReactiveFormsModule,
    FeedlessHeaderModule,
    RemoveIfProdModule,
    IonHeader,
    IonProgressBar,
    IonContent,
    IonRow,
    IonItem,
    IonList,
    IonLabel,
    IonChip,
    IonButton,
    IonIcon,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [DirectoryPage],
})
export class DirectoryPageModule {}
