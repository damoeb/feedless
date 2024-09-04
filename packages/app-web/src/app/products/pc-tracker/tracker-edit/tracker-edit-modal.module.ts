import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { TrackerEditModalComponent } from './tracker-edit-modal.component';
import 'img-comparison-slider';
import { BubbleModule } from '../../../components/bubble/bubble.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FeedBuilderModalModule } from '../../../modals/feed-builder-modal/feed-builder-modal.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    BubbleModule,
    ReactiveFormsModule,
    FeedBuilderModalModule,
  ],
  declarations: [TrackerEditModalComponent],
})
export class TrackerEditModalModule {}
