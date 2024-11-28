import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConnectAppPageRoutingModule } from './connect-app-routing.module';

import { ConnectAppPage } from './connect-app.page';

import {
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonContent,
  IonSpinner,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ConnectAppPageRoutingModule,
    IonContent,
    IonSpinner,
    IonCard,
    IonCardContent,
    IonButtons,
    IonButton,
    ConnectAppPage,
],
})
export class LinkAppPageModule {}
