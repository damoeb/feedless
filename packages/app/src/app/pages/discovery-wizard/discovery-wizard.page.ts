import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FeedService } from '../../services/feed.service';
import { Router } from '@angular/router';
import {
  TransientGenericFeedAndDiscovery,
  TransientNativeFeedAndDiscovery,
} from '../../components/feed-discovery-wizard/feed-discovery-wizard.component';
import { GqlArticleRecoveryType } from '../../../generated/graphql';
import { omit } from 'lodash';

@Component({
  selector: 'app-wizard',
  templateUrl: './discovery-wizard.page.html',
  styleUrls: ['./discovery-wizard.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DiscoveryWizardPage {
  constructor(
    private readonly feedService: FeedService,
    private readonly router: Router
  ) {}

  async saveGeneric([
    transientGenericFeed,
    discovery,
  ]: TransientGenericFeedAndDiscovery) {
    const { fetchOptions, parserOptions } = discovery.genericFeeds;
    const selectors = transientGenericFeed.selectors;
    const genericFeed = await this.feedService.createGenericFeed({
      harvestSiteWithPrerender: false,
      title: discovery.document.title,
      description: discovery.document.description,
      websiteUrl: discovery.url,
      specification: {
        // feedUrl: transientGenericFeed.feedUrl,
        refineOptions: {
          filter: '',
          recovery: GqlArticleRecoveryType.None,
        },
        fetchOptions: {
          websiteUrl: discovery.url,
          prerender: fetchOptions.prerender,
          prerenderWaitUntil: fetchOptions.prerenderWaitUntil,
          prerenderScript: fetchOptions.prerenderScript,
          prerenderWithoutMedia: fetchOptions.prerenderWithoutMedia,
        },
        parserOptions: {
          strictMode: parserOptions.strictMode,
          eventFeed: parserOptions.eventFeed,
        },
        selectors: {
          contextXPath: selectors.contextXPath,
          dateXPath: selectors.dateXPath,
          extendContext: selectors.extendContext,
          linkXPath: selectors.linkXPath,
        },
      },
    });
    await this.router.navigateByUrl(`/feeds/${genericFeed.nativeFeedId}`);
  }

  async saveNative([feed, discovery]: TransientNativeFeedAndDiscovery) {
    const nativeFeed = await this.feedService.createNativeFeed({
      websiteUrl: feed.url,
      feedUrl: feed.url,
      title: feed.title,
      description: feed.description || discovery.document.description,
      harvestSiteWithPrerender: false,
    });
    await this.router.navigateByUrl(`/feeds/${nativeFeed.id}`);
  }
}
