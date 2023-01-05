import { ChangeDetectionStrategy, Component } from '@angular/core';
import {
  FeedDiscoveryResult,
  FeedService,
  TransientGenericFeed,
  TransientNativeFeed,
} from '../../services/feed.service';
import { Router } from '@angular/router';
import { omit } from 'lodash';
import {
  TransientGenericFeedAndDiscovery,
  TransientNativeFeedAndDiscovery
} from '../../components/feed-discovery-wizard/feed-discovery-wizard.component';

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
    feedDiscovery,
  ]: TransientGenericFeedAndDiscovery) {
    const genericFeed = await this.feedService.createGenericFeed({
      harvestSiteWithPrerender: false,
      harvestSite: false,
      title: feedDiscovery.title,
      description: feedDiscovery.description,
      websiteUrl: feedDiscovery.url,
      feedRule: JSON.stringify(omit(transientGenericFeed, 'samples')),
    });
    await this.router.navigateByUrl(`/feeds/${genericFeed.nativeFeedId}`);
  }

  async saveNative([feed, discovery]: TransientNativeFeedAndDiscovery) {
    const nativeFeed = await this.feedService.createNativeFeed({
      websiteUrl: feed.url,
      feedUrl: feed.url,
      title: feed.title,
      description: feed.description || discovery.description,
      harvestSiteWithPrerender: false,
      harvestSite: false,
    });
    await this.router.navigateByUrl(`/feeds/${nativeFeed.id}`);
  }
}
