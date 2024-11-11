import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RemoteFeedPreviewComponent } from './remote-feed-preview.component';
import { RemoteFeedItemModule } from '../../components/remote-feed-item/remote-feed-item.module';
import { IonList, IonItem, IonLabel } from '@ionic/angular/standalone';

@NgModule({
  declarations: [RemoteFeedPreviewComponent],
  exports: [RemoteFeedPreviewComponent],
  imports: [CommonModule, RemoteFeedItemModule, IonList, IonItem, IonLabel],
})
export class RemoteFeedPreviewModule {}
