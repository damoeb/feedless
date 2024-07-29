import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransformWebsiteToFeedComponent } from './transform-website-to-feed.component';
import { IonicModule } from '@ionic/angular';
import { EmbeddedMarkupModule } from '../embedded-markup/embedded-markup.module';
import { NativeFeedModule } from '../native-feed/native-feed.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ResponsiveColumnsModule } from '../responsive-columns/responsive-columns.module';
import { RemoteFeedModalModule } from '../../modals/remote-feed-modal/remote-feed-modal.module';
import { CodeEditorModalModule } from '../../modals/code-editor-modal/code-editor-modal.module';

@NgModule({
  declarations: [TransformWebsiteToFeedComponent],
  exports: [TransformWebsiteToFeedComponent],
  imports: [
    CommonModule,
    IonicModule,
    EmbeddedMarkupModule,
    NativeFeedModule,
    RemoteFeedModalModule,
    ReactiveFormsModule,
    ResponsiveColumnsModule,
    FormsModule,
    CodeEditorModalModule,
  ],
})
export class TransformWebsiteToFeedModule {}
