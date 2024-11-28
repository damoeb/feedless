import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoginPageRoutingModule } from './login-routing.module';

import { LoginPage } from './login.page';


import {
  IonButton,
  IonCard,
  IonCardContent,
  IonContent,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonSpinner,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    LoginPageRoutingModule,
    IonContent,
    IonSpinner,
    IonCardContent,
    IonList,
    IonItem,
    IonInput,
    IonLabel,
    IonCard,
    IonButton,
    IonIcon,
    LoginPage,
],
})
export class EmailLoginPageModule {}
