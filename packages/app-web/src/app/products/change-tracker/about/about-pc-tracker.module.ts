import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AboutPcTrackerRoutingModule } from './about-pc-tracker-routing.module';

import { AboutPcTrackerPage } from './about-pc-tracker.page';


import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    AboutPcTrackerRoutingModule,
    IonContent,
    AboutPcTrackerPage,
],
})
export class AboutPcTrackerModule {}
