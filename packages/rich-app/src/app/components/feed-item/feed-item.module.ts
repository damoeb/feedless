import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FeedItemComponent} from "./feed-item.component";



@NgModule({
  declarations: [FeedItemComponent],
  exports: [FeedItemComponent],
  imports: [
    CommonModule
  ]
})
export class FeedItemModule { }
