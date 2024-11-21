import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventPage } from './event.page';
import { EventRoutingModule } from './event-routing.module';
import { UpcomingHeaderModule } from '../upcoming-header/upcoming-header.module';
import { UpcomingFooterModule } from '../upcoming-footer/upcoming-footer.module';
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
    UpcomingHeaderModule,
    UpcomingFooterModule,
    IonContent,
    IonSpinner,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonNote,
    IonBadge,
    IonFooter,
  ],
  declarations: [EventPage],
  exports: [EventPage],
})
export class EventPageModule {}
