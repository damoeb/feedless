import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedDetailsComponent } from './feed-details.component';





import { ReactiveFormsModule } from '@angular/forms';






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
  IonFooter,
} from '@ionic/angular/standalone';


@NgModule({
  exports: [FeedDetailsComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
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
    IonFooter,
    IonPopover,
    IonCol,
    IonSegment,
    IonSegmentButton,
    IonCheckbox,
    FeedDetailsComponent,
],
  providers: [ModalController],
})
export class FeedDetailsModule {}
