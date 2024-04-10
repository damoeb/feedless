import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RemoteFeedPreviewComponent } from './remote-feed-preview.component';
import { IonicModule } from '@ionic/angular';
import { RemoteFeedItemModule } from '../../components/remote-feed-item/remote-feed-item.module';

@NgModule({
  declarations: [RemoteFeedPreviewComponent],
  exports: [RemoteFeedPreviewComponent],
  imports: [
    CommonModule,
    IonicModule,
    RemoteFeedItemModule,
  ],
})
export class RemoteFeedPreviewModule {}
