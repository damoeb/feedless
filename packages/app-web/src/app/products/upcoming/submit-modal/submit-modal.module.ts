import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SubmitModalComponent } from './submit-modal.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
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
  IonLabel,
  IonRow,
  IonCol,
  IonInput,
  IonNote,
  IonRadio,
  IonRadioGroup,
  IonSearchbar,
  IonFooter,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [SubmitModalComponent],
  exports: [SubmitModalComponent],
  imports: [
    CommonModule,
    FormsModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonRow,
    IonCol,
    IonIcon,
    IonContent,
    IonList,
    IonItem,
    IonLabel,
    IonInput,
    ReactiveFormsModule,
    IonNote,
    IonRadio,
    IonRadioGroup,
    IonSearchbar,
    IonFooter,
  ],
})
export class SubmitModalModule {}
