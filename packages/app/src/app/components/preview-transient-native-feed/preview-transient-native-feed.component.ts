import { Component, OnInit } from '@angular/core';
import {
  FeedService,
  RemoteFeedItem,
  TransientNativeFeed,
} from '../../services/feed.service';
import { ModalController } from '@ionic/angular';
import { ModalDismissal, ModalSuccess } from '../../app.module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ImporterService } from '../../services/importer.service';

export interface PreviewTransientNativeFeedComponentProps {
  transientNativeFeed: TransientNativeFeed;
  bucketId: string;
}

@Component({
  selector: 'app-preview-transient-native-feed',
  templateUrl: './preview-transient-native-feed.component.html',
  styleUrls: ['./preview-transient-native-feed.component.scss'],
})
export class PreviewTransientNativeFeedComponent
  implements OnInit, PreviewTransientNativeFeedComponentProps
{
  transientNativeFeed: TransientNativeFeed;
  bucketId: string;

  formGroup: FormGroup<{
    websiteUrl: FormControl<string>;
    description: FormControl<string | null>;
    title: FormControl<string>;
    harvest: FormControl<boolean>;
    prerender: FormControl<boolean>;
    autoRelease: FormControl<boolean>;
  }>;

  items: Array<RemoteFeedItem>;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly importerService: ImporterService,
    private readonly feedService: FeedService
  ) {}

  async ngOnInit() {
    this.formGroup = new FormGroup({
      title: new FormControl(
        this.transientNativeFeed.title,
        Validators.required
      ),
      description: new FormControl(this.transientNativeFeed.description),
      websiteUrl: new FormControl('', Validators.required),
      harvest: new FormControl(false, Validators.required),
      prerender: new FormControl(false, Validators.required),
      autoRelease: new FormControl(true, Validators.required),
    });
    this.items = await this.feedService.remoteFeedContent(
      this.transientNativeFeed.url
    );
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
            nativeFeed: {
              feedUrl: this.transientNativeFeed.url,
              title: values.title,
              description: values.description,
              harvestSite: values.harvest,
              harvestSiteWithPrerender: values.prerender,
              websiteUrl: values.websiteUrl,
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
