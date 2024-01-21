import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { VisualDiffPageRoutingModule } from './visual-diff-routing.module';

import { VisualDiffPage } from './visual-diff.page';
import { ScrapeSourceModule } from '../../components/scrape-source/scrape-source.module';
import { EmbeddedImageModule } from '../../components/embedded-image/embedded-image.module';
import { SubscriptionCreatePage } from './subscription-create/subscription-create.page';
import { SubscriptionsPage } from './subscriptions/subscriptions.page';
import { SubscriptionDetailsPage } from './subscription-details/subscription-details.page';
import 'img-comparison-slider';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    VisualDiffPageRoutingModule,
    ReactiveFormsModule,
    ScrapeSourceModule,
    EmbeddedImageModule,
    FormsModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [VisualDiffPage, SubscriptionCreatePage, SubscriptionsPage, SubscriptionDetailsPage],
})
export class VisualDiffPageModule {}
