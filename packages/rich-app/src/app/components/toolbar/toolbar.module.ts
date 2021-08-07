import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToolbarComponent } from './toolbar.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';
import { BubbleModule } from '../bubble/bubble.module';
import { ChooseFeedUrlModule } from '../choose-feed-url/choose-feed-url.module';
import { ChooseBucketModule } from '../choose-bucket/choose-bucket.module';

@NgModule({
  declarations: [ToolbarComponent],
  exports: [ToolbarComponent],
  imports: [
    CommonModule,
    IonicModule,
    FormsModule,
    BubbleModule,
    ChooseFeedUrlModule,
    ChooseBucketModule,
  ],
})
export class ToolbarModule {}
