import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { SubscriptionDetailsPage } from './subscription-details.page';
import 'img-comparison-slider';
import { SubscriptionDetailsRoutingModule } from './subscription-details-routing.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    SubscriptionDetailsRoutingModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [SubscriptionDetailsPage]
})
export class SubscriptionDetailsPageModule {
}
