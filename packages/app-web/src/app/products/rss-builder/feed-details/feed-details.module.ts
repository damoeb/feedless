import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { FeedDetailsPage } from './feed-details.page';
import 'img-comparison-slider';
import { FeedDetailsRoutingModule } from './feed-details-routing.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FeedDetailsRoutingModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [FeedDetailsPage]
})
export class FeedDetailsPageModule {
}
