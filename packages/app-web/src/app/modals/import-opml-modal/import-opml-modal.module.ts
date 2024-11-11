import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ImportOpmlModalComponent } from './import-opml-modal.component';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonIcon,
  IonContent,
  IonList,
  IonItem,
  IonCheckbox,
  IonLabel,
  IonFooter,
  IonNote,
  ModalController,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [ImportOpmlModalComponent],
  exports: [ImportOpmlModalComponent],
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    IonList,
    IonItem,
    IonCheckbox,
    IonLabel,
    IonFooter,
    IonNote,
  ],
  providers: [ModalController],
})
export class ImportOpmlModalModule {}
