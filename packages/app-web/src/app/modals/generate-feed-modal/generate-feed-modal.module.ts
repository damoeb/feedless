import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GenerateFeedModalComponent } from './generate-feed-modal.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';
import { RemoteFeedItemModule } from '../../components/remote-feed-item/remote-feed-item.module';

@NgModule({
  declarations: [GenerateFeedModalComponent],
  exports: [GenerateFeedModalComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule, RemoteFeedItemModule]
})
export class GenerateFeedModalModule {}
