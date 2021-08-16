import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketSettingsComponent } from './bucket-settings.component';
import { IonicModule } from '@ionic/angular';
import { BubbleModule } from '../bubble/bubble.module';
import { FormsModule } from '@angular/forms';
import { FiltersModule } from '../filters/filters.module';
import { OutputThrottleModule } from '../output-throttle/output-throttle.module';
import { SubscriptionsModule } from '../subscriptions/subscriptions.module';
import { ConfirmButtonModule } from '../confirm-button/confirm-button.module';

@NgModule({
  declarations: [BucketSettingsComponent],
  exports: [BucketSettingsComponent],
  imports: [
    CommonModule,
    IonicModule,
    BubbleModule,
    FormsModule,
    FiltersModule,
    SubscriptionsModule,
    OutputThrottleModule,
    ConfirmButtonModule,
  ],
})
export class BucketSettingsModule {}
