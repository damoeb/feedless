import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WorkflowBuilderComponent } from './workflow-builder.component';
import { IonicModule } from '@ionic/angular';
import { EmbeddedMarkupModule } from '../embedded-markup/embedded-markup.module';
import { NativeFeedModule } from '../native-feed/native-feed.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ResponsiveColumnsModule } from '../responsive-columns/responsive-columns.module';
import { RemoteFeedModalModule } from '../../modals/remote-feed-modal/remote-feed-modal.module';
import { BubbleModule } from '../bubble/bubble.module';

@NgModule({
  declarations: [WorkflowBuilderComponent],
  exports: [WorkflowBuilderComponent],
  imports: [
    CommonModule,
    IonicModule,
    EmbeddedMarkupModule,
    NativeFeedModule,
    RemoteFeedModalModule,
    ReactiveFormsModule,
    ResponsiveColumnsModule,
    FormsModule,
    BubbleModule,
  ],
})
export class WorkflowBuilderModule {}
