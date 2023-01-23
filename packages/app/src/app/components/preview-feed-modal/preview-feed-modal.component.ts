import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { ModalCancel } from '../../app.module';
import { FeedService, RemoteFeedItem } from '../../services/feed.service';
import { FieldWrapper, Scalars } from '../../../generated/graphql';

export interface PreviewFeedModalComponentProps {
  feedUrl: string;
}

export type PreviewFeedModalDismissal = ModalCancel;

@Component({
  selector: 'app-preview-feed-modal',
  templateUrl: './preview-feed-modal.component.html',
  styleUrls: ['./preview-feed-modal.component.scss'],
})
export class PreviewFeedModalComponent
  implements PreviewFeedModalComponentProps, OnInit
{
  feedUrl: string;
  loading: boolean;
  feedItems: Array<RemoteFeedItem>;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly feedService: FeedService
  ) {}

  async closeModal() {
    const response: PreviewFeedModalDismissal = {
      cancel: true,
    };

    await this.modalCtrl.dismiss(response);
  }

  async ngOnInit(): Promise<void> {
    this.loading = true;
    this.feedItems = await this.feedService.remoteFeedContent(this.feedUrl);
    this.loading = false;
  }

  toDate(publishedAt: FieldWrapper<Scalars['Long']>): Date {
    return new Date(publishedAt);
  }
}
