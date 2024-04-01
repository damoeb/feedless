import { Component } from '@angular/core';
import { FeedWithRequest } from '../../components/feed-builder/feed-builder.component';
import { ModalController } from '@ionic/angular';
import { GqlScrapeRequestInput } from '../../../generated/graphql';

export interface FeedBuilderModalComponentProps {
  scrapeRequest?: GqlScrapeRequestInput;
}

@Component({
  selector: 'app-feed-builder-modal',
  templateUrl: './feed-builder-modal.component.html',
  styleUrls: ['./feed-builder-modal.component.scss'],
})
export class FeedBuilderModalComponent
  implements FeedBuilderModalComponentProps
{
  scrapeRequest: GqlScrapeRequestInput;

  constructor(private readonly modalCtrl: ModalController) {}

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  async handleFeed(feed: FeedWithRequest) {
    await this.modalCtrl.dismiss(feed);
  }
}
