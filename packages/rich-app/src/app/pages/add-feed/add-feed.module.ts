import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { AddFeedPageRoutingModule } from './add-feed-routing.module';

import { AddFeedPage } from './add-feed.page';

@NgModule({
  imports: [CommonModule, FormsModule, IonicModule, AddFeedPageRoutingModule],
  declarations: [AddFeedPage],
})
export class AddFeedPageModule {}
