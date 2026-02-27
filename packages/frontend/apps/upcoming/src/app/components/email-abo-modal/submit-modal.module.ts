import { NgModule } from '@angular/core';
import { EmailAboModalComponent } from './email-abo-modal.component';
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
// eslint-disable-next-line @nx/enforce-module-boundaries
import { IconComponent } from '@feedless/components';

@NgModule({
  declarations: [EmailAboModalComponent],
  exports: [EmailAboModalComponent],
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
