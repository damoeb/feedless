import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GenerateFeedModalComponent } from './generate-feed-modal.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';
import { RemoteFeedItemModule } from '../../components/remote-feed-item/remote-feed-item.module';
import { RemoteFeedPreviewModule } from '../../components/remote-feed-preview/remote-feed-preview.module';

@NgModule({
  declarations: [GenerateFeedModalComponent],
  exports: [GenerateFeedModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    ReactiveFormsModule,
    RemoteFeedItemModule,
    RemoteFeedPreviewModule
  ]
})
export class GenerateFeedModalModule {}
