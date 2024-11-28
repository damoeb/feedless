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
import { GqlSourceInput } from '../../../generated/graphql';
import { Repository } from '../../graphql/types';
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
  standalone: true,
})
export class FeedBuilderModalComponent
  implements FeedBuilderModalComponentProps
{
  private readonly modalCtrl = inject(ModalController);

  source: GqlSourceInput;
  modalTitle: string = 'Feed Builder';
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

  async handleRepository(repository: Repository) {
    await this.modalCtrl.dismiss({ repository });
  }
}
