import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventPage } from './event.page';
import { EventRoutingModule } from './event-routing.module';
import {
  IonBadge,
  IonButton,
  IonButtons,
  IonContent,
  IonFooter,
  IonIcon,
  IonNote,
  IonSpinner,
  IonToolbar,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    EventRoutingModule,
    IonContent,
    IonSpinner,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonNote,
    IonBadge,
    IonFooter,
    EventPage,
  ],
})
export class EventPageModule {}
