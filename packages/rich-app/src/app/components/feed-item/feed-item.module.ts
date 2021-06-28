import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FeedItemComponent} from "./feed-item.component";
import {IonicModule} from "@ionic/angular";
import {RouterModule} from "@angular/router";



@NgModule({
  declarations: [FeedItemComponent],
  exports: [FeedItemComponent],
  imports: [
    CommonModule,
    IonicModule,
    RouterModule
  ]
})
export class FeedItemModule { }
