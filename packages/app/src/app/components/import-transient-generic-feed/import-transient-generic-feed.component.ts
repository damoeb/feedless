import { Component, OnInit } from '@angular/core';
import {
  FeedDiscoveryResult,
  TransientGenericFeed,
} from '../../services/feed.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ModalController } from '@ionic/angular';
import { ImporterService } from '../../services/importer.service';
import { ModalDismissal, ModalSuccess } from '../../app.module';

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
  transientGenericFeed: TransientGenericFeed;
  feedDiscovery: FeedDiscoveryResult;
  bucketId: string;

  formGroup: FormGroup<{
    websiteUrl: FormControl<string>;
    description: FormControl<string | null>;
    title: FormControl<string>;
    prerender: FormControl<boolean>;
    autoRelease: FormControl<boolean>;
  }>;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly importerService: ImporterService
  ) {}

  async ngOnInit() {
    const document = this.feedDiscovery.document;
    this.formGroup = new FormGroup({
      title: new FormControl(document.title, Validators.required),
      description: new FormControl(document.description),
      websiteUrl: new FormControl(this.feedDiscovery.websiteUrl, Validators.required),
      prerender: new FormControl(false, Validators.required),
      autoRelease: new FormControl(true, Validators.required),
    });
  }

  closeModal() {
    const response: ModalDismissal = {
      cancel: true,
    };
    return this.modalCtrl.dismiss(response);
  }

  async importAndClose() {
    if (this.formGroup.invalid) {
      console.warn(this.formGroup);
    } else {
      const values = this.formGroup.value;
      const { parserOptions, fetchOptions } = this.feedDiscovery.genericFeeds;
      await this.importerService.createImporter({
        autoRelease: values.autoRelease,
        where: {
          id: this.bucketId,
        },
        feed: {
          create: {
            genericFeed: {
              title: values.title,
              description: values.description,
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
                refineOptions: {},
                selectors: this.transientGenericFeed.selectors,
              },
              websiteUrl: values.websiteUrl,
              harvestItems: false,
              harvestSiteWithPrerender: values.prerender,
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

  showTransientGenericFeed(genericFeed: TransientGenericFeed) {
    this.transientGenericFeed = genericFeed;
  }
}
