import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrackerEditModalComponent } from './tracker-edit-modal.component';
import 'img-comparison-slider';
import { BubbleModule } from '../../../components/bubble/bubble.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FeedBuilderModalModule } from '../../../modals/feed-builder-modal/feed-builder-modal.module';
import { RemoteFeedPreviewModule } from '../../../components/remote-feed-preview/remote-feed-preview.module';
import {
  IonHeader,
  IonToolbar,
  IonButtons,
  IonButton,
  IonIcon,
  IonTitle,
  IonLabel,
  IonContent,
  IonList,
  IonListHeader,
  IonRadioGroup,
  IonItem,
  IonRadio,
  IonNote,
  IonText,
  IonInput,
  IonSelect,
  IonSelectOption,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    BubbleModule,
    ReactiveFormsModule,
    FeedBuilderModalModule,
    RemoteFeedPreviewModule,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonTitle,
    IonLabel,
    IonContent,
    IonList,
    IonListHeader,
    IonRadioGroup,
    IonItem,
    IonRadio,
    IonNote,
    IonText,
    IonInput,
    IonSelect,
    IonSelectOption,
  ],
  declarations: [TrackerEditModalComponent],
})
export class TrackerEditModalModule {}
