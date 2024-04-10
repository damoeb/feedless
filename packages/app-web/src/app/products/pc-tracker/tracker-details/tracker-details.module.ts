import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { TrackerDetailsPage } from './tracker-details.page';
import 'img-comparison-slider';
import { TrackerDetailsRoutingModule } from './tracker-details-routing.module';
import { BubbleModule } from '../../../components/bubble/bubble.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    TrackerDetailsRoutingModule,
    BubbleModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [TrackerDetailsPage],
})
export class TrackerDetailsPageModule {}
