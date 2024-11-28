import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DirectoryPage } from './directory.page';
import 'img-comparison-slider';
import { DirectoryRoutingModule } from './directory-routing.module';


import { ImportButtonModule } from '../../components/import-button/import-button.module';



import { ReactiveFormsModule } from '@angular/forms';


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
    ImportButtonModule,
    ReactiveFormsModule,
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
    DirectoryPage,
],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class DirectoryPageModule {}
