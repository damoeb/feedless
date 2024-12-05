import { NgModule } from '@angular/core';
import { TrackerEditModalComponent } from './tracker-edit-modal.component';
import 'img-comparison-slider';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonListHeader,
  IonNote,
  IonRadio,
  IonRadioGroup,
  IonSelect,
  IonSelectOption,
  IonText,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';
import { RemoteFeedPreviewComponent } from '../../../components/remote-feed-preview/remote-feed-preview.component';
import { FeedBuilderModalModule } from '../../../modals/feed-builder-modal/feed-builder-modal.module';

@NgModule({
  imports: [
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonTitle,
    IonLabel,
    IonContent,
    RemoteFeedPreviewComponent,
    IonList,
    IonListHeader,
    IonRadioGroup,
    FormsModule,
    ReactiveFormsModule,
    IonItem,
    IonRadio,
    IonNote,
    IonText,
    IonInput,
    IonSelect,
    IonSelectOption,
    FeedBuilderModalModule,
  ],
  declarations: [TrackerEditModalComponent],
  exports: [TrackerEditModalComponent],
})
export class TrackerEditModalModule {}
