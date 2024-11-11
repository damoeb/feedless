import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmailLoginComponent } from './email-login.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  IonCard,
  IonCardContent,
  IonList,
  IonItem,
  IonInput,
  IonLabel,
  IonIcon,
  IonSpinner,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [EmailLoginComponent],
  exports: [EmailLoginComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    IonCard,
    IonCardContent,
    IonList,
    IonItem,
    IonInput,
    IonLabel,
    IonIcon,
    IonSpinner,
  ],
})
export class EmailLoginModule {}
