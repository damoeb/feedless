import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransformWebsiteToFeedComponent } from './transform-website-to-feed.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ResponsiveColumnsModule } from '../responsive-columns/responsive-columns.module';
import { InteractiveWebsiteModule } from '../interactive-website/interactive-website.module';
import { CodeEditorModalModule } from '../../modals/code-editor-modal/code-editor-modal.module';
import { ConsoleButtonModule } from '../console-button/console-button.module';
import { BubbleModule } from '../bubble/bubble.module';
import { RemoteFeedPreviewModule } from '../remote-feed-preview/remote-feed-preview.module';
import {
  IonAccordionGroup,
  IonAccordion,
  IonItem,
  IonLabel,
  IonIcon,
  IonInput,
  IonButton,
  IonNote,
  IonCheckbox,
  IonList,
  IonSegmentButton,
  IonProgressBar,
  IonSpinner,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [TransformWebsiteToFeedComponent],
  exports: [TransformWebsiteToFeedComponent],
  imports: [
    CommonModule,
    CodeEditorModalModule,
    ReactiveFormsModule,
    ResponsiveColumnsModule,
    FormsModule,
    InteractiveWebsiteModule,
    ConsoleButtonModule,
    BubbleModule,
    RemoteFeedPreviewModule,
    IonAccordionGroup,
    IonAccordion,
    IonItem,
    IonLabel,
    IonIcon,
    IonInput,
    IonButton,
    IonNote,
    IonCheckbox,
    IonList,
    IonSegmentButton,
    IonProgressBar,
    IonSpinner,
  ],
})
export class TransformWebsiteToFeedModule {}
