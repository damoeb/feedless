import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { Bucket } from '../../services/bucket.service';
import {
  FeedDiscoveryResult,
  FeedService,
  PagedNativeFeeds,
  TransientNativeFeed,
} from '../../services/feed.service';
import { ModalController, ToastController } from '@ionic/angular';
import { ImporterService } from '../../services/importer.service';
import {
  PreviewTransientNativeFeedComponent,
  PreviewTransientNativeFeedComponentProps,
} from '../preview-transient-native-feed/preview-transient-native-feed.component';
import { ModalDismissal } from '../../app.module';
import {
  PreviewTransientGenericFeedComponent,
  PreviewTransientGenericFeedComponentProps,
} from '../preview-transient-generic-feed/preview-transient-generic-feed.component';

export interface ImporterCreatePageProps {
  bucket: Bucket;
}

@Component({
  selector: 'app-importer-create',
  templateUrl: './importer-create.page.html',
  styleUrls: ['./importer-create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImporterCreatePage implements OnInit, ImporterCreatePageProps {
  bucket: Bucket;
  loading = false;
  query: string;
  canInspectPage: boolean;
  feedDiscovery: FeedDiscoveryResult;
  existingFeeds: PagedNativeFeeds;

  constructor(
    private readonly feedService: FeedService,
    private readonly importerService: ImporterService,
    private readonly toastCtrl: ToastController,
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef
  ) {}

  ngOnInit() {}

  async searchFeeds(query: string) {
    if (query?.trim().length > 3) {
      this.loading = true;
      this.feedDiscovery = null;
      this.canInspectPage = this.isUrl(query);
      this.existingFeeds = await this.feedService.searchNativeFeeds({
        query,
      });
      if (this.canInspectPage) {
        this.feedDiscovery = await this.inspectPage(query);
      }
      this.loading = false;
      this.changeRef.detectChanges();
    }
  }

  async inspectPage(url: string) {
    return this.feedService.discoverFeeds(url);
  }

  cancelModal() {
    const response: ModalDismissal = {
      cancel: true,
    };
    return this.modalCtrl.dismiss(response);
  }

  async closeModal() {
    const toast = await this.toastCtrl.create({
      message: 'Created',
      duration: 3000,
      color: 'success',
    });

    await toast.present();
    const response: ModalDismissal = {
      cancel: false,
    };
    await this.modalCtrl.dismiss(response);
  }

  async importTransientNativeFeed(feed: TransientNativeFeed) {
    const componentProps: PreviewTransientNativeFeedComponentProps = {
      bucketId: this.bucket.id,
      transientNativeFeed: feed,
    };
    const modal = await this.modalCtrl.create({
      component: PreviewTransientNativeFeedComponent,
      componentProps,
      backdropDismiss: false,
    });
    await modal.present();
    const result = await modal.onDidDismiss<ModalDismissal>();

    if (!result.data.cancel) {
      await this.closeModal();
    }
  }

  async importTransientGenericFeed() {
    const componentProps: PreviewTransientGenericFeedComponentProps = {
      bucketId: this.bucket.id,
      feedDiscovery: this.feedDiscovery,
    };
    const modal = await this.modalCtrl.create({
      component: PreviewTransientGenericFeedComponent,
      componentProps,
      backdropDismiss: false,
      cssClass: 'fullscreen',
    });
    await modal.present();
    const result = await modal.onDidDismiss<ModalDismissal>();

    if (!result.data.cancel) {
      await this.closeModal();
    }
  }

  private isUrl(value: string): boolean {
    try {
      const potentialUrl = value.toLowerCase();
      if (
        potentialUrl.startsWith('http://') ||
        potentialUrl.startsWith('https://')
      ) {
        new URL(value);
      } else {
        new URL(`https://${value}`);
      }
      return true;
    } catch (e) {
      return false;
    }
  }
}
