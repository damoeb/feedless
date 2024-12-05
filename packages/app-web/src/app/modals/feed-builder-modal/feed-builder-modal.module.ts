import { NgModule } from '@angular/core';
import { FeedBuilderModalComponent } from './feed-builder-modal.component';
import { FeedBuilderComponent } from '../../components/feed-builder/feed-builder.component';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [FeedBuilderModalComponent],
  exports: [FeedBuilderModalComponent],
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    FeedBuilderComponent,
  ],
})
export class FeedBuilderModalModule {}
