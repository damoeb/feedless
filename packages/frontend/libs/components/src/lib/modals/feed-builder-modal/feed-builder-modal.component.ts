import { Component, inject, PLATFORM_ID } from '@angular/core';
import {
  FeedBuilderComponent,
  FeedWithRequest,
} from '../../components/feed-builder/feed-builder.component';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { GqlSourceInput, RepositoryWithFrequency } from '@feedless/graphql-api';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../../components/icon/icon.component';

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
    IconComponent,
    IonContent,
    FeedBuilderComponent,
  ],
})
export class FeedBuilderModalComponent
  implements FeedBuilderModalComponentProps
{
  private readonly modalCtrl = inject(ModalController);
  private readonly platformId = inject(PLATFORM_ID);

  source: GqlSourceInput;
  modalTitle = 'Feed Builder';
  submitButtonText = 'Save Feed';

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ closeOutline });
    }
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
