import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RemoteFeedComponent } from './remote-feed.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [RemoteFeedComponent],
  exports: [RemoteFeedComponent],
  imports: [CommonModule, IonicModule, RouterLink],
})
export class RemoteFeedModule {}
