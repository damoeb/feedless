import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FeedComponent} from './feed.component';
import {IonicModule} from '@ionic/angular';



@NgModule({
  declarations: [FeedComponent],
  exports: [FeedComponent],
  imports: [
    CommonModule,
    IonicModule
  ]
})
export class FeedModule { }
