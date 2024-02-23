import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RemoteFeedItemComponent } from './remote-feed-item.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { BubbleModule } from '../bubble/bubble.module';

@NgModule({
  declarations: [RemoteFeedItemComponent],
  exports: [RemoteFeedItemComponent],
  imports: [CommonModule, IonicModule, RouterLink, BubbleModule],
})
export class RemoteFeedItemModule {}
