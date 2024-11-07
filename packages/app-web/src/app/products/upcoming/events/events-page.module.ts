import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { EventsPage } from './events.page';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EventsRoutingModule } from './events-routing.module';
import { UpcomingHeaderModule } from '../upcoming-header/upcoming-header.module';
import { UpcomingFooterModule } from '../upcoming-footer/upcoming-footer.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    ReactiveFormsModule,
    RouterLink,
    EventsRoutingModule,
    UpcomingHeaderModule,
    UpcomingFooterModule,
  ],
  declarations: [EventsPage],
  exports: [EventsPage],
})
export class EventsPageModule {}
