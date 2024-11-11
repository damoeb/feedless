import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginButtonComponent } from './login-button.component';
import { RouterLink } from '@angular/router';
import {
  IonButton,
  IonIcon,
  IonPopover,
  IonList,
  IonItem,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [LoginButtonComponent],
  exports: [LoginButtonComponent],
  imports: [
    CommonModule,
    RouterLink,
    IonButton,
    IonIcon,
    IonPopover,
    IonList,
    IonItem,
  ],
})
export class LoginButtonModule {}
