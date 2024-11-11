import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RemoteFeedItemComponent } from './remote-feed-item.component';
import { RouterLink } from '@angular/router';
import { BubbleModule } from '../bubble/bubble.module';
import { PlayerModule } from '../player/player.module';
import { IonItem, IonLabel, IonChip } from '@ionic/angular/standalone';

@NgModule({
  declarations: [RemoteFeedItemComponent],
  exports: [RemoteFeedItemComponent],
  imports: [
    CommonModule,
    RouterLink,
    BubbleModule,
    PlayerModule,
    IonItem,
    IonLabel,
    IonChip,
  ],
})
export class RemoteFeedItemModule {}
