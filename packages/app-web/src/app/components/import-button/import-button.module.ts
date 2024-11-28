import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportButtonComponent } from './import-button.component';

import { RouterLink } from '@angular/router';
import {
  IonButton,
  IonLabel,
  IonPopover,
  IonContent,
  IonList,
  IonItem,
  ModalController,
} from '@ionic/angular/standalone';



@NgModule({
  exports: [ImportButtonComponent],
  providers: [ModalController],
  imports: [
    CommonModule,
    RouterLink,
    IonButton,
    IonLabel,
    IonPopover,
    IonContent,
    IonList,
    IonItem,
    ImportButtonComponent,
],
})
export class ImportButtonModule {}
