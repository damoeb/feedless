import { Component, inject } from '@angular/core';
import {
  FeedBuilderComponent,
  FeedWithRequest,
} from '../../components/feed-builder/feed-builder.component';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { GqlSourceInput, RepositoryWithFrequency } from '@feedless/graphql-api';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';

export interface FeedBuilderModalComponentProps {
  source?: GqlSourceInput;
  modalTitle?: string;
  submitButtonText?: string;
}

@Component({
  selector: 'app-feed-builder-modal',
  templateUrl: './feed-builder-modal.component.html',
  styleUrls: ['./feed-builder-modal.component.scss'],
  standalone: true,
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
export class FeedBuilderModalComponent
  implements FeedBuilderModalComponentProps
{
  private readonly modalCtrl = inject(ModalController);

  source: GqlSourceInput;
  modalTitle = 'Feed Builder';
  submitButtonText = 'Save Feed';

  constructor() {
    addIcons({ closeOutline });
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  async handleFeed(feed: FeedWithRequest) {
    await this.modalCtrl.dismiss({ feed });
  }

  async handleRepository(repository: RepositoryWithFrequency) {
    await this.modalCtrl.dismiss({ repository });
  }
}
