import { Component, OnInit, ViewChild } from '@angular/core';
import { FeedDiscoveryResult, TransientNativeFeed } from '../../services/feed.service';
import { ModalController, ToastController } from '@ionic/angular';
import { ModalDismissal, ModalSuccess } from '../../app.module';
import { ImporterService } from '../../services/importer.service';
import { FeedMetadata, FeedMetadataFormComponent } from '../feed-metadata-form/feed-metadata-form.component';
import { ImporterMetadataFormComponent } from '../importer-metadata-form/importer-metadata-form.component';

export interface ImportTransientNativeFeedComponentProps {
  feedDiscovery: FeedDiscoveryResult;
  transientNativeFeed: TransientNativeFeed;
  bucketId: string;
}

@Component({
  selector: 'app-import-transient-native-feed',
  templateUrl: './import-transient-native-feed.component.html',
  styleUrls: ['./import-transient-native-feed.component.scss'],
})
export class ImportTransientNativeFeedComponent
  implements OnInit, ImportTransientNativeFeedComponentProps
{
  @ViewChild('feedMetadataForm')
  feedMetadataFormComponent: FeedMetadataFormComponent;

  @ViewChild('importerMetadataForm')
  importerMetadataFormComponent: ImporterMetadataFormComponent;

  transientNativeFeed: TransientNativeFeed;
  feedDiscovery: FeedDiscoveryResult;

  bucketId: string;
  feedMetadata: FeedMetadata;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly importerService: ImporterService,
    private readonly toastCtrl: ToastController
  ) {}

  ngOnInit(): void {
    const discovery = this.feedDiscovery;
    const feed = this.transientNativeFeed;
    this.feedMetadata = {
      autoRelease: true,
      title: feed.title || discovery.document.title,
      description: feed.description || discovery.document.description,
      websiteUrl: discovery.websiteUrl,
      harvestItems: false,
      prerender: false,
      language: discovery.document.language
    };
  }

  closeModal() {
    const response: ModalDismissal = {
      cancel: true,
    };
    return this.modalCtrl.dismiss(response);
  }

  async importAndClose() {
    const feedForm = this.feedMetadataFormComponent.formGroup;
    const importerForm = this.importerMetadataFormComponent.formGroup;
    if (feedForm.invalid || importerForm.invalid) {
      const toast = await this.toastCtrl.create({
        message: 'Form is incomplete',
        duration: 4000,
        color: 'danger',
      });
      await toast.present();
    } else {
      const { title, description, prerender, websiteUrl, autoRelease, harvestItems } = feedForm.value;
      await this.importerService.createImporter({
        autoRelease: importerForm.value.autoImport,
        where: {
          id: this.bucketId,
        },
        feed: {
          create: {
            nativeFeed: {
              autoRelease,
              feedUrl: this.transientNativeFeed.url,
              title,
              description,
              harvestItems,
              harvestSiteWithPrerender: prerender,
              websiteUrl,
            },
          },
        },
      });

      const response: ModalSuccess = {
        cancel: false,
      };
      return this.modalCtrl.dismiss(response);
    }
  }
}
