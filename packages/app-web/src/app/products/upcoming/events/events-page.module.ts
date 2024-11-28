import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventsPage } from './events.page';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EventsRoutingModule } from './events-routing.module';
import {
  IonBadge,
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonCardTitle,
  IonContent,
  IonFooter,
  IonIcon,
  IonInput,
  IonItem,
  IonNote,
  IonSpinner,
  IonToolbar,
} from '@ionic/angular/standalone';


@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    EventsRoutingModule,
    IonContent,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonSpinner,
    IonNote,
    IonFooter,
    IonCard,
    IonCardContent,
    IonCardTitle,
    IonInput,
    IonBadge,
    IonItem,
    EventsPage,
],
})
export class EventsPageModule {}
