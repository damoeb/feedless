import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedBuilderModalComponent } from './feed-builder-modal.component';
import { FeedBuilderModule } from '../../components/feed-builder/feed-builder.module';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonIcon,
  IonContent,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [FeedBuilderModalComponent],
  exports: [FeedBuilderModalComponent],
  imports: [
    CommonModule,
    FeedBuilderModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
  ],
})
export class FeedBuilderModalModule {}
