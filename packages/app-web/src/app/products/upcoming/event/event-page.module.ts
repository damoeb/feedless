import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { EventPageComponent } from './event-page.component';
import { EventPageRoutingModule } from './event-page-routing.module';
import { UpcomingHeaderModule } from '../upcoming-header/upcoming-header.module';

@NgModule({
  imports: [CommonModule, IonicModule, EventPageRoutingModule, UpcomingHeaderModule],
  declarations: [EventPageComponent],
  exports: [EventPageComponent],
})
export class EventPageModule {}
