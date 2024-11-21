import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SubmitModalComponent } from './submit-modal.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  IonButton,
  IonButtons,
  IonCheckbox,
  IonCol,
  IonContent,
  IonFooter,
  IonHeader,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonRadio,
  IonRadioGroup,
  IonRow,
  IonSearchbar,
  IonTitle,
  IonToolbar,
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
    IonCheckbox,
  ],
})
export class SubmitModalModule {}
