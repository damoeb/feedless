import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotebooksComponent } from './notebooks.component';
import { FormsModule } from '@angular/forms';
import { ProductHeaderModule } from '../product-header/product-header.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { RouterLink } from '@angular/router';
import {
  IonList,
  IonListHeader,
  IonLabel,
  IonItem,
  IonIcon,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [NotebooksComponent],
  exports: [NotebooksComponent],
  imports: [
    CommonModule,
    FormsModule,
    ProductHeaderModule,
    SearchbarModule,
    RouterLink,
    IonList,
    IonListHeader,
    IonLabel,
    IonItem,
    IonIcon,
  ],
})
export class NotebooksModule {}
