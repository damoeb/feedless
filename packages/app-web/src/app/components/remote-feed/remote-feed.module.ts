import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RemoteFeedComponent } from './remote-feed.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { BubbleModule } from '../bubble/bubble.module';
import { RemoteFeedItemModule } from '../remote-feed-item/remote-feed-item.module';

@NgModule({
  declarations: [RemoteFeedComponent],
  exports: [RemoteFeedComponent],
  imports: [CommonModule, IonicModule, RouterLink, BubbleModule, RemoteFeedItemModule]
})
export class RemoteFeedModule {}
