import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedDetailsComponent } from './feed-details.component';
import { BubbleModule } from '../bubble/bubble.module';
import { ReaderModule } from '../reader/reader.module';
import { FeedBuilderModalModule } from '../../modals/feed-builder-modal/feed-builder-modal.module';
import { RepositoryModalModule } from '../../modals/repository-modal/repository-modal.module';
import { PaginationModule } from '../pagination/pagination.module';
import { ReactiveFormsModule } from '@angular/forms';
import { PlayerModule } from '../player/player.module';
import { ImageDiffModule } from '../image-diff/image-diff.module';
import { TextDiffModule } from '../text-diff/text-diff.module';
import { HistogramModule } from '../histogram/histogram.module';
import { CodeEditorModalModule } from '../../modals/code-editor-modal/code-editor-modal.module';
import { RemoveIfProdModule } from '../../directives/remove-if-prod/remove-if-prod.module';
import { RouterLink } from '@angular/router';
import {
  IonSpinner,
  IonRow,
  IonChip,
  IonToolbar,
  IonButtons,
  IonModal,
  IonHeader,
  IonTitle,
  IonButton,
  IonIcon,
  IonContent,
  IonList,
  IonItem,
  IonLabel,
  IonNote,
  IonText,
  IonBadge,
  IonPopover,
  IonCol,
  IonSegment,
  IonSegmentButton,
  IonCheckbox,
  ModalController,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [FeedDetailsComponent],
  exports: [FeedDetailsComponent],
  imports: [
    CommonModule,
    BubbleModule,
    ReaderModule,
    FeedBuilderModalModule,
    RepositoryModalModule,
    PaginationModule,
    CodeEditorModalModule,
    ReactiveFormsModule,
    PlayerModule,
    ImageDiffModule,
    TextDiffModule,
    HistogramModule,
    RemoveIfProdModule,
    RouterLink,
    IonSpinner,
    IonRow,
    IonChip,
    IonToolbar,
    IonButtons,
    IonModal,
    IonHeader,
    IonTitle,
    IonButton,
    IonIcon,
    IonContent,
    IonList,
    IonItem,
    IonLabel,
    IonNote,
    IonText,
    IonBadge,
    IonPopover,
    IonCol,
    IonSegment,
    IonSegmentButton,
    IonCheckbox,
  ],
  providers: [ModalController],
})
export class FeedDetailsModule {}
