import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { EventsPageComponent } from './events-page.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EventsPageRoutingModule } from './events-page-routing.module';
import { UpcomingHeaderModule } from '../upcoming-header/upcoming-header.module';

@NgModule({
  imports: [CommonModule, IonicModule, ReactiveFormsModule, RouterLink, EventsPageRoutingModule, UpcomingHeaderModule],
  declarations: [EventsPageComponent],
  exports: [EventsPageComponent],
})
export class EventsPageModule {}
