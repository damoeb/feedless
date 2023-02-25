import { Component, OnInit, ViewChild } from '@angular/core';
import { FeedDiscoveryResult, TransientGenericFeed } from '../../services/feed.service';
import { ModalController, ToastController } from '@ionic/angular';
import { ImporterService } from '../../services/importer.service';
import { ModalDismissal, ModalSuccess } from '../../app.module';
import { FeedMetadata, FeedMetadataFormComponent } from '../feed-metadata-form/feed-metadata-form.component';
import { ImporterMetadataFormComponent } from '../importer-metadata-form/importer-metadata-form.component';
import { GqlArticleRecoveryType } from '../../../generated/graphql';

export interface ImportTransientGenericFeedComponentProps {
  transientGenericFeed: TransientGenericFeed;
  feedDiscovery: FeedDiscoveryResult;
  bucketId: string;
}

@Component({
  selector: 'app-preview-transient-generic-feed',
  templateUrl: './import-transient-generic-feed.component.html',
  styleUrls: ['./import-transient-generic-feed.component.scss'],
})
export class ImportTransientGenericFeedComponent
  implements OnInit, ImportTransientGenericFeedComponentProps
{
  @ViewChild('feedMetadataForm')
  feedMetadataFormComponent: FeedMetadataFormComponent;

  @ViewChild('importerMetadataForm')
  importerMetadataFormComponent: ImporterMetadataFormComponent;

  transientGenericFeed: TransientGenericFeed;
  feedDiscovery: FeedDiscoveryResult;
  bucketId: string;

  feedMetadata: FeedMetadata;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly importerService: ImporterService,
    private readonly toastCtrl: ToastController
  ) {}

  async ngOnInit() {
    const discovery = this.feedDiscovery;
    const feed = this.transientGenericFeed;
    this.feedMetadata = {
      title: discovery.document.title,
      description: discovery.document.description,
      websiteUrl: discovery.websiteUrl,
      harvestItems: false,
      autoRelease: false,
      prerender: false,
      language: discovery.document.language,
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
      const { title, description, prerender, websiteUrl, autoRelease } =
        feedForm.value;
      const { parserOptions, fetchOptions } = this.feedDiscovery.genericFeeds;
      await this.importerService.createImporter({
        autoRelease: importerForm.value.autoImport,
        where: {
          id: this.bucketId,
        },
        feed: {
          create: {
            genericFeed: {
              autoRelease,
              title,
              description,
              specification: {
                parserOptions: {
                  strictMode: parserOptions.strictMode,
                },
                fetchOptions: {
                  prerenderWithoutMedia: fetchOptions.prerenderWithoutMedia,
                  prerenderWaitUntil: fetchOptions.prerenderWaitUntil,
                  prerenderScript: fetchOptions.prerenderScript || '',
                  websiteUrl: fetchOptions.websiteUrl,
                  prerender: fetchOptions.prerender,
                },
                refineOptions: {
                  recovery: GqlArticleRecoveryType.None
                },
                selectors: this.transientGenericFeed.selectors,
              },
              websiteUrl,
              harvestItems: false,
              harvestSiteWithPrerender: prerender,
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
