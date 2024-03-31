import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedBuilderModalComponent } from './feed-builder-modal.component';
import { IonicModule } from '@ionic/angular';
import { FeedBuilderModule } from '../../components/feed-builder/feed-builder.module';

@NgModule({
  declarations: [FeedBuilderModalComponent],
  exports: [FeedBuilderModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    FeedBuilderModule
  ]
})
export class FeedBuilderModalModule {}
