import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConnectAppPageRoutingModule } from './connect-app-routing.module';

import { ConnectAppPage } from './connect-app.page';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
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
    FeedlessHeaderModule,
    IonContent,
    IonSpinner,
    IonCard,
    IonCardContent,
    IonButtons,
    IonButton,
  ],
  declarations: [ConnectAppPage],
})
export class LinkAppPageModule {}
