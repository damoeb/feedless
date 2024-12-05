import { Component, inject } from '@angular/core';
import { FeedWithRequest } from '../../components/feed-builder/feed-builder.component';
import { ModalController } from '@ionic/angular/standalone';
import { GqlSourceInput } from '../../../generated/graphql';
import { RepositoryWithFrequency } from '../../graphql/types';
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
  standalone: false,
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

  async handleRepository(repository: RepositoryWithFrequency) {
    await this.modalCtrl.dismiss({ repository });
  }
}
