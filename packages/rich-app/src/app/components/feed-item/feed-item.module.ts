import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedItemComponent } from './feed-item.component';
import { IonicModule } from '@ionic/angular';
import { RouterModule } from '@angular/router';
import { NotificationBubbleModule } from '../notification-bubble/notification-bubble.module';

@NgModule({
  declarations: [FeedItemComponent],
  exports: [FeedItemComponent],
  imports: [CommonModule, IonicModule, RouterModule, NotificationBubbleModule],
})
export class FeedItemModule {}
