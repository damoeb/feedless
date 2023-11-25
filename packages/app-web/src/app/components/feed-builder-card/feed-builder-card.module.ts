import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedBuilderCardComponent } from './feed-builder-card.component';
import { IonicModule } from '@ionic/angular';
import { SelectModule } from '../select/select.module';



@NgModule({
  declarations: [FeedBuilderCardComponent],
  exports: [FeedBuilderCardComponent],
  imports: [
    CommonModule,
    IonicModule,
    SelectModule
  ]
})
export class FeedBuilderCardModule { }
