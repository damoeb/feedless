import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrackerDetailsPage } from './tracker-details.page';
import 'img-comparison-slider';
import { TrackerDetailsRoutingModule } from './tracker-details-routing.module';

import {
  IonContent,
  IonBreadcrumbs,
  IonBreadcrumb,
  IonSpinner,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    TrackerDetailsRoutingModule,
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    IonSpinner,
    TrackerDetailsPage,
],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class TrackerDetailsPageModule {}
