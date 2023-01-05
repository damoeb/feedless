import { Component, OnInit } from '@angular/core';
import {
  FeedDiscoveryResult,
  TransientGenericFeed,
} from '../../services/feed.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ModalController } from '@ionic/angular';
import { ImporterService } from '../../services/importer.service';
import { ModalDismissal, ModalSuccess } from '../../app.module';
import { omit } from 'lodash';

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
    harvest: FormControl<boolean>;
    prerender: FormControl<boolean>;
    autoRelease: FormControl<boolean>;
  }>;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly importerService: ImporterService
  ) {}

  async ngOnInit() {
    this.formGroup = new FormGroup({
      title: new FormControl(this.feedDiscovery.title, Validators.required),
      description: new FormControl(this.feedDiscovery.description),
      websiteUrl: new FormControl(this.feedDiscovery.url, Validators.required),
      harvest: new FormControl(false, Validators.required),
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
              feedRule: JSON.stringify(
                omit(this.transientGenericFeed, 'samples')
              ),
              harvestSite: values.harvest,
              websiteUrl: values.websiteUrl,
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
