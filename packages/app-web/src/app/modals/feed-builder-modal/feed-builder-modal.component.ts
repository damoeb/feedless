import { Component } from '@angular/core';
import { FeedWithRequest } from '../../components/feed-builder/feed-builder.component';
import { ModalController } from '@ionic/angular';
import { GqlSourceInput } from '../../../generated/graphql';
import { Repository } from '../../graphql/types';

export interface FeedBuilderModalComponentProps {
  scrapeRequest?: GqlSourceInput;
  modalTitle?: string;
  submitButtonText?: string;
}

@Component({
  selector: 'app-feed-builder-modal',
  templateUrl: './feed-builder-modal.component.html',
  styleUrls: ['./feed-builder-modal.component.scss'],
})
export class FeedBuilderModalComponent
  implements FeedBuilderModalComponentProps
{
  scrapeRequest: GqlSourceInput;
  modalTitle: string = 'Feed Builder';
  submitButtonText = 'Save Feed';

  constructor(private readonly modalCtrl: ModalController) {}

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
