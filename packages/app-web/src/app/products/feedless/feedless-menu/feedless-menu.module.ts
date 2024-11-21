import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedlessMenuComponent } from './feedless-menu.component';
import { RouterModule } from '@angular/router';
import { BubbleModule } from '../../../components/bubble/bubble.module';
import {
  IonChip,
  IonItem,
  IonList,
  IonMenuToggle,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [FeedlessMenuComponent],
  exports: [FeedlessMenuComponent],
  imports: [
    CommonModule,
    RouterModule,
    BubbleModule,
    IonList,
    IonMenuToggle,
    IonItem,
    IonChip,
  ],
})
export class FeedlessMenuModule {}
