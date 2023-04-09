import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import {
  TransientGenericFeed,
  TransientNativeFeed,
} from '../../../services/feed.service';
import {
  clone,
  cloneDeep,
  isEqual,
  isUndefined,
  max,
  min,
  omit,
} from 'lodash-es';
import {
  GqlArticleRecoveryType,
  GqlFetchOptionsInput,
  GqlVisibility,
} from '../../../../generated/graphql';
import { EmbedWebsite } from '../../embedded-website/embedded-website.component';
import { ScaleLinear, scaleLinear } from 'd3-scale';
import { WizardContextChange, WizardHandler } from '../wizard-handler';

@Component({
  selector: 'app-wizard-feeds',
  templateUrl: './wizard-feeds.component.html',
  styleUrls: ['./wizard-feeds.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardFeedsComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  currentNativeFeed: TransientNativeFeed;
  currentGenericFeed: TransientGenericFeed;
  embedWebsiteData: EmbedWebsite;
  isNonSelected = true;
  busy = false;
  private scaleScore: ScaleLinear<number, number, never>;
  private currentFetchOptions: GqlFetchOptionsInput = undefined;

  constructor(private readonly changeRef: ChangeDetectorRef) {}

  ngOnInit() {
    this.handler.onContextChange().subscribe((change) => {
      this.handleChange(change);
    });
  }

  async pickNativeFeed(nativeFeed: TransientNativeFeed) {
    await this.resetSelection();
    if (this.currentNativeFeed !== nativeFeed) {
      this.currentNativeFeed = nativeFeed;
      await this.handler.updateContext({
        feed: {
          create: {
            nativeFeed: {
              feedUrl: nativeFeed.url,
              title: nativeFeed.title,
              description: nativeFeed.description,
              autoRelease: true,
              harvestItems: false,
              harvestSiteWithPrerender: false,
              visibility: GqlVisibility.IsPublic,
            },
          },
        },
      });
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;
    this.changeRef.detectChanges();
  }

  async pickGenericFeed(genericFeed: TransientGenericFeed) {
    await this.resetSelection();
    if (this.currentGenericFeed?.hash !== genericFeed.hash) {
      this.currentGenericFeed = cloneDeep(genericFeed);
      const discovery = this.handler.getDiscovery();
      const document = discovery.document;
      await this.handler.updateContext({
        feed: {
          create: {
            genericFeed: {
              title: document.title,
              description: document.description,
              websiteUrl: discovery.websiteUrl,
              // autoRelease: true,
              harvestItems: false,
              harvestSiteWithPrerender: false,
              // visibility: GqlVisibility.IsProtected,
              specification: {
                selectors: omit(
                  this.currentGenericFeed.selectors,
                  '__typename'
                ),
                fetchOptions: this.handler.getContext().fetchOptions,
                parserOptions: {
                  strictMode: false,
                },
                refineOptions: {
                  recovery: GqlArticleRecoveryType.None,
                },
              },
            },
          },
        },
      });
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;
    this.changeRef.detectChanges();
  }

  isWebsite(): boolean {
    return this.handler.hasMimeType('text/html');
  }

  getRelativeScore(genericFeed: TransientGenericFeed): number {
    return this.scaleScore ? this.scaleScore(genericFeed.score) : 0;
  }

  private handleChange(change: WizardContextChange) {
    if (!isUndefined(change.busy)) {
      this.busy = change.busy;
      this.changeRef.detectChanges();
    }
    const discovery = this.handler.getDiscovery();
    if (
      discovery &&
      !isEqual(this.currentFetchOptions, discovery.fetchOptions)
    ) {
      try {
        this.currentFetchOptions = clone(discovery.fetchOptions);
        this.embedWebsiteData = {
          htmlBody: discovery.document.htmlBody,
          mimeType: discovery.document.mimeType,
          url: discovery.websiteUrl,
        };
        const scores = discovery.genericFeeds.feeds.map((gf) => gf.score);
        const maxScore = max(scores);
        const minScore = min(scores);
        this.scaleScore = scaleLinear()
          .domain([minScore, maxScore])
          .range([0, 100]);
        this.changeRef.detectChanges();
      } catch (e) {
        console.error(e);
      }
    }
  }

  private async resetSelection() {
    this.currentGenericFeed = null;
    this.currentNativeFeed = null;
    await this.handler.updateContext({ feed: null });
  }
}
