import { Component, OnInit } from '@angular/core';
import { FeedDiscoveryResult, TransientGenericFeed } from '../../services/feed.service';
import { ModalController } from '@ionic/angular';
import { ModalDismissal } from '../../app.module';
import { BucketCreatePage } from '../bucket-create/bucket-create.page';
import {
  ImportTransientGenericFeedComponent,
  ImportTransientGenericFeedComponentProps
} from '../import-transient-generic-feed/import-transient-generic-feed.component';

export interface PreviewTransientGenericFeedComponentProps {
  bucketId: string
  feedDiscovery: FeedDiscoveryResult
}

@Component({
  selector: 'app-preview-transient-generic-feed',
  templateUrl: './preview-transient-generic-feed.component.html',
  styleUrls: ['./preview-transient-generic-feed.component.scss'],
})
export class PreviewTransientGenericFeedComponent implements OnInit, PreviewTransientGenericFeedComponentProps {

  feedDiscovery: FeedDiscoveryResult;
  bucketId: string;

  private transientGenericFeed: TransientGenericFeed;

  constructor(private readonly modalCtrl: ModalController) { }

  async ngOnInit() {
  }

  closeModal(cancel: boolean = true) {
    return this.modalCtrl.dismiss({
        cancel
      }
    )
  }

  showTransientGenericFeed(genericFeed: TransientGenericFeed) {
    this.transientGenericFeed = genericFeed;
  }

  async continue() {
    if (this.transientGenericFeed) {
      const componentProps: ImportTransientGenericFeedComponentProps = {
        bucketId: this.bucketId,
        feedDiscovery: this.feedDiscovery,
        transientGenericFeed: this.transientGenericFeed
      };
      const modal = await this.modalCtrl.create({
        component: ImportTransientGenericFeedComponent,
        componentProps
      });
      await modal.present();
      const { data } = await modal.onDidDismiss<ModalDismissal>();
      if (!data.cancel) {
        await this.closeModal(false);
      }
    }  }
}
