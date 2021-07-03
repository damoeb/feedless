import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketSettingsComponent } from './bucket-settings.component';
import { IonicModule } from '@ionic/angular';
import { AddSubscriptionModule } from '../add-subscription/add-subscription.module';
import { NotificationBubbleModule } from '../notification-bubble/notification-bubble.module';

@NgModule({
  declarations: [BucketSettingsComponent],
  exports: [BucketSettingsComponent],
  imports: [
    CommonModule,
    IonicModule,
    AddSubscriptionModule,
    NotificationBubbleModule,
  ],
})
export class BucketSettingsModule {}
