import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PreviewRemoteFeedComponent } from './preview-remote-feed.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [PreviewRemoteFeedComponent],
  exports: [PreviewRemoteFeedComponent],
  imports: [CommonModule, IonicModule, RouterLink],
})
export class PreviewRemoteFeedModule {}
