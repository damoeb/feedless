import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RemoteFeedModalComponent } from './remote-feed-modal.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { RemoteFeedPreviewModule } from '../../components/remote-feed-preview/remote-feed-preview.module';

@NgModule({
  declarations: [RemoteFeedModalComponent],
  exports: [RemoteFeedModalComponent],
  imports: [CommonModule, IonicModule, FormsModule, SearchbarModule, RemoteFeedPreviewModule]
})
export class RemoteFeedModalModule {}
