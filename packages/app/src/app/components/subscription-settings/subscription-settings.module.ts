import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SubscriptionSettingsComponent } from './subscription-settings.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';
import { NativeFeedModule } from '../native-feed/native-feed.module';
import { GeneratedFeedModule } from '../generated-feed/generated-feed.module';
import { BubbleModule } from '../bubble/bubble.module';
import { FeedDetailsModule } from '../feed-details/feed-details.module';
import { ConfirmButtonModule } from '../confirm-button/confirm-button.module';

@NgModule({
  declarations: [SubscriptionSettingsComponent],
  exports: [SubscriptionSettingsComponent],
  imports: [
    CommonModule,
    IonicModule,
    FormsModule,
    NativeFeedModule,
    GeneratedFeedModule,
    BubbleModule,
    FeedDetailsModule,
    ConfirmButtonModule,
  ],
})
export class SubscriptionSettingsModule {}
