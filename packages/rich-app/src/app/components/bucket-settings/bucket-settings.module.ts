import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketSettingsComponent } from './bucket-settings.component';
import { IonicModule } from '@ionic/angular';
import { SubscriptionSettingsModule } from '../subscription-settings/subscription-settings.module';
import { BubbleModule } from '../bubble/bubble.module';
import { FormsModule } from '@angular/forms';
import { ChooseFeedUrlModule } from '../choose-feed-url/choose-feed-url.module';

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
  ],
})
export class BucketSettingsModule {}
