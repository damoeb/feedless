import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { EventPage } from './event.page';
import { EventRoutingModule } from './event-routing.module';
import { UpcomingHeaderModule } from '../upcoming-header/upcoming-header.module';
import { UpcomingFooterModule } from '../upcoming-footer/upcoming-footer.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    EventRoutingModule,
    UpcomingHeaderModule,
    UpcomingFooterModule,
  ],
  declarations: [EventPage],
  exports: [EventPage],
})
export class EventPageModule {}
