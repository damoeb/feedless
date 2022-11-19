import { Component, OnInit } from '@angular/core';
import { FeedDiscoveryResult, TransientGenericFeed } from '../../services/feed.service';
import { ModalController } from '@ionic/angular';
import { ModalDismissal } from '../../app.module';
import {
  ImportTransientGenericFeedComponent,
  ImportTransientGenericFeedComponentProps
} from '../import-transient-generic-feed/import-transient-generic-feed.component';
import { Article, BasicContent } from '../../services/article.service';
import { GqlArticleType, GqlReleaseStatus } from '../../../generated/graphql';

export interface PreviewTransientGenericFeedComponentProps {
  bucketId: string;
  feedDiscovery: FeedDiscoveryResult;
}

@Component({
  selector: 'app-preview-transient-generic-feed',
  templateUrl: './preview-transient-generic-feed.component.html',
  styleUrls: ['./preview-transient-generic-feed.component.scss'],
})
export class PreviewTransientGenericFeedComponent
  implements OnInit, PreviewTransientGenericFeedComponentProps
{
  feedDiscovery: FeedDiscoveryResult;
  bucketId: string;

  transientGenericFeed: TransientGenericFeed;

  constructor(private readonly modalCtrl: ModalController) {}

  async ngOnInit() {}

  closeModal(cancel: boolean = true) {
    return this.modalCtrl.dismiss({
      cancel,
    });
  }

  showTransientGenericFeed(genericFeed: TransientGenericFeed) {
    this.transientGenericFeed = genericFeed;
  }

  async continue() {
    if (this.transientGenericFeed) {
      const componentProps: ImportTransientGenericFeedComponentProps = {
        bucketId: this.bucketId,
        feedDiscovery: this.feedDiscovery,
        transientGenericFeed: this.transientGenericFeed,
      };
      const modal = await this.modalCtrl.create({
        component: ImportTransientGenericFeedComponent,
        componentProps,
      });
      await modal.present();
      const { data } = await modal.onDidDismiss<ModalDismissal>();
      if (!data.cancel) {
        await this.closeModal(false);
      }
    }
  }

  toArticle(content: BasicContent): Article {
    return {
      content,
      createdAt: content.createdAt,
      id: 'id',
      status: GqlReleaseStatus.Released,
      type: GqlArticleType.Feed,
      nativeFeedId: null,
      streamId: null
    };
  }
}
