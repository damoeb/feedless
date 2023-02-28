import { ChangeDetectionStrategy, Component, ViewChild } from '@angular/core';
import { FeedDiscoveryResult, FeedService } from '../../services/feed.service';
import { Router } from '@angular/router';
import {
  TransientGenericFeedAndDiscovery,
  TransientNativeFeedAndDiscovery,
} from '../../components/feed-discovery-wizard/feed-discovery-wizard.component';
import {
  AuthViaMail,
  GqlArticleRecoveryType,
  GqlAuthViaMailSubscription,
  GqlAuthViaMailSubscriptionVariables,
} from '../../../generated/graphql';
import { ToastController } from '@ionic/angular';
import {
  FeedMetadata,
  FeedMetadataFormComponent,
} from '../../components/feed-metadata-form/feed-metadata-form.component';
import { ApolloClient } from '@apollo/client/core';

@Component({
  selector: 'app-discovery-wizard',
  templateUrl: './discovery-wizard.page.html',
  styleUrls: ['./discovery-wizard.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DiscoveryWizardPage {
  @ViewChild('feedMetadataForm')
  feedMetadataFormComponent: FeedMetadataFormComponent;

  genericFeedData: TransientGenericFeedAndDiscovery;
  nativeFeedData: TransientNativeFeedAndDiscovery;
  feedMetadata: FeedMetadata;

  constructor(
    private readonly feedService: FeedService,
    private readonly toastCtrl: ToastController,
    private readonly router: Router
  ) {}

  handleGeneric(event: TransientGenericFeedAndDiscovery) {
    this.genericFeedData = event;
    this.initFeedData(event[1]);
  }

  handleNative(event: TransientNativeFeedAndDiscovery) {
    this.nativeFeedData = event;
    this.initFeedData(event[1]);
  }

  async isValid(): Promise<boolean> {
    const feedForm = this.feedMetadataFormComponent.formGroup;
    if (feedForm.invalid) {
      const toast = await this.toastCtrl.create({
        message: 'Form is incomplete',
        duration: 4000,
        color: 'danger',
      });
      await toast.present();
      return false;
    } else {
      return true;
    }
  }

  async saveGeneric() {
    if (await this.isValid()) {
      const [transientGenericFeed, discovery] = this.genericFeedData;
      const { fetchOptions, parserOptions } = discovery.genericFeeds;
      const selectors = transientGenericFeed.selectors;
      const {
        title,
        description,
        prerender,
        websiteUrl,
        autoRelease,
        harvestItems,
      } = this.feedMetadataFormComponent.formGroup.value;
      const genericFeed = await this.feedService.createGenericFeed({
        autoRelease,
        harvestItems,
        harvestSiteWithPrerender: false,
        title,
        description,
        websiteUrl,
        specification: {
          refineOptions: {
            filter: '',
            recovery: GqlArticleRecoveryType.None,
          },
          fetchOptions: {
            websiteUrl,
            prerender: prerender || fetchOptions.prerender,
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
            paginationXPath: selectors.paginationXPath,
            extendContext: selectors.extendContext,
            dateXPath: selectors.dateXPath,
            dateIsStartOfEvent: selectors.dateIsStartOfEvent,
          },
        },
      });
      await this.router.navigateByUrl(`/feeds/${genericFeed.nativeFeedId}`);
    }
  }
  async saveNative() {
    const [feed, discovery] = this.nativeFeedData;
    const response = await this.feedService.searchNativeFeeds({
      where: {
        feedUrl: feed.url,
      },
      page: 0,
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
        autoRelease: false,
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

  private initFeedData(discovery: FeedDiscoveryResult) {
    this.feedMetadata = {
      title: discovery.document.title,
      description: discovery.document.description,
      websiteUrl: discovery.websiteUrl,
      autoRelease: true,
      harvestItems: false,
      prerender: discovery.genericFeeds.fetchOptions.prerender,
      language: discovery.document.language,
    };
  }
}
