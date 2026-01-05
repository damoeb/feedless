import { NgModule } from '@angular/core';
import { SubmitModalComponent } from './submit-modal.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  IonButton,
  IonButtons,
  IonCheckbox,
  IonContent,
  IonFooter,
  IonHeader,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonRadio,
  IonRadioGroup,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [SubmitModalComponent],
  exports: [SubmitModalComponent],
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    IonItem,
    IonInput,
    FormsModule,
    ReactiveFormsModule,
    IonRadioGroup,
    IonLabel,
    IonRadio,
    IonFooter,
    IonList,
    IonCheckbox,
  ],
})
export class SubmitModalModule {}
