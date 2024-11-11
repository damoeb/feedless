import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrackerDetailsPage } from './tracker-details.page';
import 'img-comparison-slider';
import { TrackerDetailsRoutingModule } from './tracker-details-routing.module';
import { BubbleModule } from '../../../components/bubble/bubble.module';
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
    BubbleModule,
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    IonSpinner,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [TrackerDetailsPage],
})
export class TrackerDetailsPageModule {}
