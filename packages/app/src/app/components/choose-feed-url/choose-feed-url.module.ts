import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChooseFeedUrlComponent } from './choose-feed-url.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [ChooseFeedUrlComponent],
  exports: [ChooseFeedUrlComponent],
  imports: [CommonModule, IonicModule, FormsModule],
})
export class ChooseFeedUrlModule {}
