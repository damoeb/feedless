import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoginPageRoutingModule } from './login-routing.module';

import { LoginPage } from './login.page';
import { EmailLoginModule } from '../../components/email-login/email-login.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import {
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
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    LoginPageRoutingModule,
    EmailLoginModule,
    FeedlessHeaderModule,
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
  ],
  declarations: [LoginPage],
})
export class EmailLoginPageModule {}
