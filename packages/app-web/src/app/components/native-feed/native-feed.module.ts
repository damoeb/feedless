import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NativeFeedComponent } from './native-feed.component';
import { RouterLink } from '@angular/router';
import { BubbleModule } from '../bubble/bubble.module';
import { RemoteFeedItemModule } from '../remote-feed-item/remote-feed-item.module';
import {
  IonItem,
  IonLabel,
  IonSpinner,
  IonList,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [NativeFeedComponent],
  exports: [NativeFeedComponent],
  imports: [
    CommonModule,
    RouterLink,
    BubbleModule,
    RemoteFeedItemModule,
    IonItem,
    IonLabel,
    IonSpinner,
    IonList,
  ],
})
export class NativeFeedModule {}
