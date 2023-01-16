import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { Bucket } from '../../services/bucket.service';
import { BasicNativeFeed, FeedService } from '../../services/feed.service';
import { ModalController, ToastController } from '@ionic/angular';
import { ImporterService } from '../../services/importer.service';
import {
  ImportTransientNativeFeedComponent,
  ImportTransientNativeFeedComponentProps,
} from '../import-transient-native-feed/import-transient-native-feed.component';
import { ModalDismissal } from '../../app.module';
import { Pagination } from '../../services/pagination.service';
import {
  DiscoveryModalComponent,
  DiscoveryModalComponentProps,
  DiscoveryModalSuccess,
} from '../discovery-modal/discovery-modal.component';
import {
  ImportTransientGenericFeedComponent,
  ImportTransientGenericFeedComponentProps,
} from '../import-transient-generic-feed/import-transient-generic-feed.component';
import {
  TransientGenericFeedAndDiscovery,
  TransientNativeFeedAndDiscovery,
} from '../feed-discovery-wizard/feed-discovery-wizard.component';
import {
  ImportExistingNativeFeedComponent,
  ImportExistingNativeFeedComponentProps,
} from '../import-existing-native-feed/import-existing-native-feed.component';

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
  existingFeeds: Array<BasicNativeFeed>;
  private pagination: Pagination;

  constructor(
    private readonly feedService: FeedService,
    private readonly importerService: ImporterService,
    private readonly toastCtrl: ToastController,
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    await this.searchFeeds('');
  }

  async searchFeeds(query: string) {
    this.loading = true;
    this.canInspectPage = this.isUrl(query);
    const response = await this.feedService.searchNativeFeeds({
      query,
    });
    this.existingFeeds = response.nativeFeeds;
    this.pagination = response.pagination;
    this.loading = false;
    this.changeRef.detectChanges();
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

  async importExistingNativeFeed(feed: BasicNativeFeed) {
    const componentProps: ImportExistingNativeFeedComponentProps = {
      bucketId: this.bucket.id,
      nativeFeed: feed,
    };
    const modal = await this.modalCtrl.create({
      component: ImportExistingNativeFeedComponent,
      componentProps,
      backdropDismiss: false,
    });
    await modal.present();
    const result = await modal.onDidDismiss<ModalDismissal>();

    if (!result.data.cancel) {
      await this.closeModal();
    }
  }

  async showDiscoveryModal() {
    const componentProps: DiscoveryModalComponentProps = {
      url: this.query,
    };
    const modal = await this.modalCtrl.create({
      component: DiscoveryModalComponent,
      componentProps,
      backdropDismiss: false,
    });
    await modal.present();
    const result = await modal.onDidDismiss<ModalDismissal>();

    if (result.data.cancel) {
    } else {
      const payload = result.data as DiscoveryModalSuccess;
      if (payload.data.nativeFeed) {
        await this.importTransientNativeFeed(payload.data.nativeFeed);
      } else {
        await this.importTransientGenericFeed(
          payload.data.genericFeedAndDiscovery
        );
      }
    }
  }

  private async importTransientNativeFeed([
    nativeFeed,
    feedDiscovery,
  ]: TransientNativeFeedAndDiscovery) {
    const componentProps: ImportTransientNativeFeedComponentProps = {
      bucketId: this.bucket.id,
      transientNativeFeed: nativeFeed,
      feedDiscovery,
    };
    const modal = await this.modalCtrl.create({
      component: ImportTransientNativeFeedComponent,
      componentProps,
      backdropDismiss: false,
    });
    await modal.present();
    const result = await modal.onDidDismiss<ModalDismissal>();

    if (!result.data.cancel) {
      await this.closeModal();
    }
  }

  private async importTransientGenericFeed([
    genericFeed,
    feedDiscovery,
  ]: TransientGenericFeedAndDiscovery) {
    const componentProps: ImportTransientGenericFeedComponentProps = {
      bucketId: this.bucket.id,
      transientGenericFeed: genericFeed,
      feedDiscovery,
    };
    const modal = await this.modalCtrl.create({
      component: ImportTransientGenericFeedComponent,
      componentProps,
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
