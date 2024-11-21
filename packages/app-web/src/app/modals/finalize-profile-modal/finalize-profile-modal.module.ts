import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FinalizeProfileModalComponent } from './finalize-profile-modal.component';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import {
  IonButton,
  IonButtons,
  IonCheckbox,
  IonCol,
  IonContent,
  IonInput,
  IonLabel,
  IonList,
  IonNote,
  IonRow,
  IonSpinner,
  IonToolbar,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [FinalizeProfileModalComponent],
  exports: [FinalizeProfileModalComponent],
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    IonContent,
    IonList,
    IonRow,
    IonCol,
    IonLabel,
    IonInput,
    IonNote,
    IonCheckbox,
    IonToolbar,
    IonButtons,
    IonButton,
    IonSpinner,
  ],
})
export class FinalizeProfileModalModule {}
