import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RemoteFeedItemComponent } from './remote-feed-item.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { BubbleModule } from '../bubble/bubble.module';
import { PlayerModule } from '../player/player.module';

@NgModule({
  declarations: [RemoteFeedItemComponent],
  exports: [RemoteFeedItemComponent],
  imports: [CommonModule, IonicModule, RouterLink, BubbleModule, PlayerModule]
})
export class RemoteFeedItemModule {}
