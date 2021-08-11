import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SubscriptionsComponent } from './subscriptions.component';
import { IonicModule } from '@ionic/angular';
import { ChooseFeedUrlModule } from '../choose-feed-url/choose-feed-url.module';
import { BubbleModule } from '../bubble/bubble.module';
import { SubscriptionSettingsModule } from '../subscription-settings/subscription-settings.module';

@NgModule({
  declarations: [SubscriptionsComponent],
  exports: [SubscriptionsComponent],
  imports: [
    CommonModule,
    IonicModule,
    ChooseFeedUrlModule,
    SubscriptionSettingsModule,
    BubbleModule,
  ],
})
export class SubscriptionsModule {}
