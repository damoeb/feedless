import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { FeedService, GenericFeed } from '../../services/feed.service';
import { ActivatedRoute, Router } from '@angular/router';
import {
  TransientGenericFeedAndDiscovery,
  TransientNativeFeedAndDiscovery,
} from '../../components/feed-discovery-wizard/feed-discovery-wizard.component';
import { GqlArticleRecoveryType } from '../../../generated/graphql';

@Component({
  selector: 'app-generic-feed',
  templateUrl: './generic-feed.page.html',
  styleUrls: ['./generic-feed.page.scss'],
  // changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GenericFeedPage implements OnInit {
  genericFeed: GenericFeed;

  constructor(
    private readonly feedService: FeedService,
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((params) => {
      if (params.id) {
        this.initGenericFeed(params.id);
      }
    });
  }

  async updateGeneric([
    transientGenericFeed,
    discovery,
  ]: TransientGenericFeedAndDiscovery) {
    const { fetchOptions, parserOptions } = discovery.genericFeeds;
    const selectors = transientGenericFeed.selectors;
    const genericFeed = await this.feedService.updateGenericFeed({
      where: {
        id: this.genericFeed.id,
      },
      data: {
        harvestSiteWithPrerender: false,
        title: discovery.document.title,
        description: discovery.document.description,
        websiteUrl: discovery.websiteUrl,
        specification: {
          // feedUrl: transientGenericFeed.feedUrl,
          refineOptions: {
            filter: '',
            recovery: GqlArticleRecoveryType.None,
          },
          fetchOptions: {
            websiteUrl: discovery.websiteUrl,
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
            dateXPath: selectors.dateXPath,
            extendContext: selectors.extendContext,
            dateIsStartOfEvent: selectors.dateIsStartOfEvent
          },
        },
      },
    });
    await this.router.navigateByUrl(`/feeds/${genericFeed.nativeFeedId}`);
  }

  private async initGenericFeed(id: string): Promise<void> {
    this.genericFeed = await this.feedService.getGenericFeedById(id);
  }
}
