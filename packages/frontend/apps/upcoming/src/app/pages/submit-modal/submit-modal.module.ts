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
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonRadio,
  IonRadioGroup,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';
import { IconComponent } from '@feedless/components';

@NgModule({
  declarations: [SubmitModalComponent],
  exports: [SubmitModalComponent],
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IconComponent,
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
