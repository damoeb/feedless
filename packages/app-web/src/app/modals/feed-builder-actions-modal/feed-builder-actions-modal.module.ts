import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';
import { FeedBuilderActionsModalComponent } from './feed-builder-actions-modal.component';
import { EmbeddedImageModule } from '../../components/embedded-image/embedded-image.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ReactiveFormsModule,
    EmbeddedImageModule,
  ],
  declarations: [FeedBuilderActionsModalComponent],
  exports: [FeedBuilderActionsModalComponent],
})
export class FeedBuilderActionsModalModule {}
