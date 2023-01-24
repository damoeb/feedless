import { ChangeDetectionStrategy, Component, ViewChild } from '@angular/core';
import { FeedService, TransientGenericFeed } from '../../services/feed.service';
import { Router } from '@angular/router';
import {
  TransientGenericFeedAndDiscovery,
  TransientNativeFeedAndDiscovery,
} from '../../components/feed-discovery-wizard/feed-discovery-wizard.component';
import { GqlArticleRecoveryType } from '../../../generated/graphql';
import { omit } from 'lodash';
import { ToastController } from '@ionic/angular';
import { FeedMetadata, FeedMetadataFormComponent } from '../../components/feed-metadata-form/feed-metadata-form.component';

@Component({
  selector: 'app-wizard',
  templateUrl: './discovery-wizard.page.html',
  styleUrls: ['./discovery-wizard.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DiscoveryWizardPage {

  @ViewChild('metadataFormComponent')
  metadataFormComponent: FeedMetadataFormComponent;
  genericFeedData: TransientGenericFeedAndDiscovery;
  feedMetadata: FeedMetadata;

  constructor(
    private readonly feedService: FeedService,
    private readonly toastCtrl: ToastController,
    private readonly router: Router
  ) {}

  async saveGeneric() {
    const formGroup = this.metadataFormComponent.formGroup;
    if (formGroup.invalid) {
      const toast = await this.toastCtrl.create({
        message: 'Form is incomplete',
        duration: 4000,
        color: 'danger',
      });
      await toast.present();
    } else {
      const [
        transientGenericFeed,
        discovery,
      ] = this.genericFeedData;
      const { fetchOptions, parserOptions } = discovery.genericFeeds;
      const selectors = transientGenericFeed.selectors;
      const genericFeed = await this.feedService.createGenericFeed({
        harvestItems: formGroup.value.harvestItems,
        harvestSiteWithPrerender: false,
        title: formGroup.value.title,
        description: formGroup.value.description,
        websiteUrl: formGroup.value.websiteUrl,
        specification: {
          refineOptions: {
            filter: '',
            recovery: GqlArticleRecoveryType.None,
          },
          fetchOptions: {
            websiteUrl: formGroup.value.websiteUrl,
            prerender: fetchOptions.prerender,
            prerenderWaitUntil: fetchOptions.prerenderWaitUntil,
            prerenderScript: fetchOptions.prerenderScript || '',
            prerenderWithoutMedia: fetchOptions.prerenderWithoutMedia,
          },
          parserOptions: {
            strictMode: parserOptions.strictMode,
          },
          selectors: {
            contextXPath: selectors.contextXPath,
            linkXPath: selectors.linkXPath,
            extendContext: selectors.extendContext,
            dateXPath: selectors.dateXPath,
            dateIsStartOfEvent: selectors.dateIsStartOfEvent,
          },
        },
      });
      await this.router.navigateByUrl(`/feeds/${genericFeed.nativeFeedId}`);
    }
  }

  async saveNative([feed, discovery]: TransientNativeFeedAndDiscovery) {
    const response = await this.feedService.searchNativeFeeds({
      where: {
        feedUrl: feed.url
      },
      page: 0
    });

    if (!response.pagination.isEmpty) {
      const toast = await this.toastCtrl.create({
        message: 'Redirecting',
        duration: 1000,
        color: 'success',
      });
      await toast.present();

      await this.router.navigateByUrl(`/feeds/${response.nativeFeeds[0].id}`);
    } else {
      const nativeFeed = await this.feedService.createNativeFeed({
        websiteUrl: feed.url,
        feedUrl: feed.url,
        title: feed.title,
        description: feed.description || discovery.document.description,
        harvestItems: false,
        harvestSiteWithPrerender: false,
      });
      const toast = await this.toastCtrl.create({
        message: 'Created',
        duration: 3000,
        color: 'success',
      });
      await toast.present();
      await this.router.navigateByUrl(`/feeds/${nativeFeed.id}`);
    }
  }

  handleGeneric(event: TransientGenericFeedAndDiscovery) {
    this.genericFeedData = event;
    const discovery = event[1];
    this.feedMetadata = {
      title: discovery.document.title,
      description: discovery.document.description,
      websiteUrl: discovery.websiteUrl,
      harvestItems: false,
      prerender: discovery.genericFeeds.fetchOptions.prerender,
      language: discovery.document.language
    };
  }
}
