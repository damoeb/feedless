import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
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
  GqlFetchOptionsInput,
  GqlVisibility,
} from '../../../../generated/graphql';
import { EmbedWebsite } from '../../embedded-website/embedded-website.component';
import { ScaleLinear, scaleLinear } from 'd3-scale';
import { WizardContextChange, WizardHandler } from '../wizard-handler';
import {
  TransientGenericFeed,
  TransientNativeFeed,
  TransientOrExistingNativeFeed,
} from '../../../graphql/types';
import { ProfileService } from '../../../services/profile.service';
import { assignNativeFeedToContext } from '../wizard-source/wizard-source.component';

@Component({
  selector: 'app-wizard-feeds',
  templateUrl: './wizard-feeds.component.html',
  styleUrls: ['./wizard-feeds.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardFeedsComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  currentNativeFeed: TransientOrExistingNativeFeed;
  currentGenericFeed: TransientGenericFeed;
  embedWebsiteData: EmbedWebsite;
  isNonSelected = true;
  busy = false;
  private scaleScore: ScaleLinear<number, number, never>;
  private currentFetchOptions: GqlFetchOptionsInput = undefined;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly profileService: ProfileService
  ) {}

  ngOnInit() {
    this.handler.onContextChange().subscribe((change) => {
      this.handleChange(change);
    });
  }

  async pickNativeFeed(feed: TransientOrExistingNativeFeed) {
    await this.resetSelection();
    if (this.currentNativeFeed !== feed) {
      this.currentNativeFeed = feed;
      await assignNativeFeedToContext(feed, this.handler);
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
        isCurrentStepValid: true,
        feed: {
          create: {
            genericFeed: {
              title: document.title,
              description: document.description,
              // visibility: GqlVisibility.IsProtected,
              specification: {
                selectors: omit(
                  this.currentGenericFeed.selectors,
                  '__typename'
                ),
                fetchOptions: this.handler.getContext().fetchOptions,
                refineOptions: {
                  filter: '',
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
