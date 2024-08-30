import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransformWebsiteToFeedComponent } from './transform-website-to-feed.component';
import { IonicModule } from '@ionic/angular';
import { NativeFeedModule } from '../native-feed/native-feed.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ResponsiveColumnsModule } from '../responsive-columns/responsive-columns.module';
import { RemoteFeedModalModule } from '../../modals/remote-feed-modal/remote-feed-modal.module';
import { InteractiveWebsiteModule } from '../interactive-website/interactive-website.module';
import { CodeEditorModalModule } from '../../modals/code-editor-modal/code-editor-modal.module';
import { RemoteFeedPreviewModule } from '../remote-feed-preview/remote-feed-preview.module';

@NgModule({
  declarations: [TransformWebsiteToFeedComponent],
  exports: [TransformWebsiteToFeedComponent],
  imports: [
    CommonModule,
    IonicModule,
    NativeFeedModule,
    RemoteFeedModalModule,
    CodeEditorModalModule,
    ReactiveFormsModule,
    ResponsiveColumnsModule,
    FormsModule,
    InteractiveWebsiteModule,
    RemoteFeedPreviewModule
  ]
})
export class TransformWebsiteToFeedModule {}
