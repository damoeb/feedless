import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedComponent } from './feed.component';
import { IonicModule } from '@ionic/angular';
import { FeedItemModule } from '../feed-item/feed-item.module';

@NgModule({
  declarations: [FeedComponent],
  exports: [FeedComponent],
  imports: [CommonModule, IonicModule, FeedItemModule],
})
export class FeedModule {}
