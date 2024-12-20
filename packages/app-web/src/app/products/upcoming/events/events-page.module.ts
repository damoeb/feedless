import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventsPage } from './events.page';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EventsRoutingModule } from './events-routing.module';
import { UpcomingHeaderModule } from '../upcoming-header/upcoming-header.module';
import { UpcomingFooterModule } from '../upcoming-footer/upcoming-footer.module';
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
import { RemoveIfProdModule } from '../../../directives/remove-if-prod/remove-if-prod.module';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    EventsRoutingModule,
    UpcomingHeaderModule,
    UpcomingFooterModule,
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
    RemoveIfProdModule,
    IonInput,
    IonBadge,
    IonItem,
  ],
  declarations: [EventsPage],
  exports: [EventsPage],
})
export class EventsPageModule {}
