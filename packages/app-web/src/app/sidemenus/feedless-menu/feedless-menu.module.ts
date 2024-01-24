import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedlessMenuComponent } from './feedless-menu.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [FeedlessMenuComponent],
  exports: [FeedlessMenuComponent],
  imports: [CommonModule, IonicModule]
})
export class FeedlessMenuModule {
}
