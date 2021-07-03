import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationBubbleComponent } from './notification-bubble.component';

@NgModule({
  declarations: [NotificationBubbleComponent],
  exports: [NotificationBubbleComponent],
  imports: [CommonModule],
})
export class NotificationBubbleModule {}
