import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedlessMenuComponent } from './feedless-menu.component';
import { IonicModule } from '@ionic/angular';
import { RouterModule } from '@angular/router';
import { BubbleModule } from '../../../components/bubble/bubble.module';

@NgModule({
  declarations: [FeedlessMenuComponent],
  exports: [FeedlessMenuComponent],
  imports: [
    CommonModule,
    IonicModule,
    RouterModule,
    BubbleModule
  ]
})
export class FeedlessMenuModule {}
