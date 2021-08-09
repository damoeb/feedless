import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketSettingsComponent } from './bucket-settings.component';
import { IonicModule } from '@ionic/angular';
import { SubscriptionSettingsModule } from '../subscription-settings/subscription-settings.module';
import { BubbleModule } from '../bubble/bubble.module';
import { FormsModule } from '@angular/forms';
import { ChooseFeedUrlModule } from '../choose-feed-url/choose-feed-url.module';
import { FiltersModule } from '../filters/filters.module';
import { OutputThrottleModule } from '../output-throttle/output-throttle.module';

@NgModule({
  declarations: [BucketSettingsComponent],
  exports: [BucketSettingsComponent],
  imports: [
    CommonModule,
    IonicModule,
    SubscriptionSettingsModule,
    BubbleModule,
    FormsModule,
    ChooseFeedUrlModule,
    FiltersModule,
    OutputThrottleModule,
  ],
})
export class BucketSettingsModule {}
