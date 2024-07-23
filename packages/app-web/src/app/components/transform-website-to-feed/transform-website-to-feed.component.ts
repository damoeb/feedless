import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import {
  GqlExtendContentOptions,
  GqlFeedlessPlugins,
  GqlNativeFeed,
  GqlRemoteNativeFeed,
  GqlScrapedFeeds,
  GqlScrapeRequest,
  GqlScrapeRequestInput,
  GqlTransientGenericFeed,
} from '../../../generated/graphql';
import { ScrapeResponse, Selectors } from '../../graphql/types';
import { Embeddable } from '../embedded-website/embedded-website.component';
import { scaleLinear, ScaleLinear } from 'd3-scale';
import { cloneDeep, max, min, omit } from 'lodash-es';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ModalService } from '../../services/modal.service';
import { FeedService } from '../../services/feed.service';
import { getScrapeRequest } from '../../modals/generate-feed-modal/generate-feed-modal.component';
import { NativeOrGenericFeed } from '../feed-builder/feed-builder.component';
import { Subscription } from 'rxjs';
import { getFirstFetchUrlLiteral } from '../../utils';

export type TypedFormControls<TControl> = {
  [K in keyof TControl]: FormControl<TControl[K]>;
};

export interface LabelledSelectOption {
  value: string;
  label: string;
}

export type ComponentStatus = 'valid' | 'invalid';

@Component({
  selector: 'app-transform-website-to-feed',
  templateUrl: './transform-website-to-feed.component.html',
  styleUrls: ['./transform-website-to-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransformWebsiteToFeedComponent
  implements OnInit, OnChanges, OnDestroy
{
  @Input({ required: true })
  scrapeRequest: GqlScrapeRequestInput;

  @Input({ required: true })
  scrapeResponse: ScrapeResponse;

  @Input()
  feed: NativeOrGenericFeed;

  @Output()
  statusChange: EventEmitter<ComponentStatus> =
    new EventEmitter<ComponentStatus>();

  @Output()
  selectedFeedChange: EventEmitter<NativeOrGenericFeed> =
    new EventEmitter<NativeOrGenericFeed>();

  formGroup: FormGroup<TypedFormControls<Selectors>> = new FormGroup<
    TypedFormControls<Selectors>
  >(
    {
      contextXPath: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(1)],
      }),
      dateXPath: new FormControl('', []),
      paginationXPath: new FormControl('', []),
      linkXPath: new FormControl('', {
        nonNullable: true,
      }),
      dateIsStartOfEvent: new FormControl(false, {
        nonNullable: true,
        validators: [Validators.required],
      }),
      extendContext: new FormControl(GqlExtendContentOptions.None, []),
    },
    { updateOn: 'change' },
  );

  genericFeeds: GqlTransientGenericFeed[] = [];
  nativeFeeds: GqlRemoteNativeFeed[] = [];
  currentNativeFeed: GqlRemoteNativeFeed;
  currentGenericFeed: GqlTransientGenericFeed;
  embedWebsiteData: Embeddable;
  isNonSelected = true;
  busy = false;
  showSelectors = false;
  private selectedFeed: NativeOrGenericFeed;
  private scaleScore: ScaleLinear<number, number, never>;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly feedService: FeedService,
    private readonly modalService: ModalService,
  ) {}

  async ngOnInit() {
    try {
      this.subscriptions.push(
        this.formGroup.valueChanges.subscribe(() => {
          this.emitSelectedFeed();
        }),
      );
      const elementWithFeeds = this.scrapeResponse.outputs.find(
        (o) => o.execute?.pluginId === GqlFeedlessPlugins.OrgFeedlessFeeds,
      );
      if (elementWithFeeds) {
        const feeds = elementWithFeeds.execute.data
          .org_feedless_feeds as GqlScrapedFeeds;
        this.genericFeeds = feeds.genericFeeds;
        this.nativeFeeds = feeds.nativeFeeds;
        const scores = feeds.genericFeeds.map((gf) => gf.score);
        const maxScore = max(scores);
        const minScore = min(scores);
        this.scaleScore = scaleLinear()
          .domain([minScore, maxScore])
          .range([0, 100]);

        const fetchAction = this.scrapeResponse.outputs.find(
          (o) => o.fetch,
        ).fetch;

        this.embedWebsiteData = {
          data: fetchAction.data,
          mimeType: fetchAction.debug.contentType,
          url: getFirstFetchUrlLiteral(this.scrapeRequest.flow.sequence),
          // viewport: null,
        };
      } else {
        const elementWithFeed = this.scrapeResponse.outputs.find(
          (o) => o.execute?.pluginId === GqlFeedlessPlugins.OrgFeedlessFeed,
        );
        if (elementWithFeed) {
          const feed = elementWithFeed.execute.data
            .org_feedless_feed as GqlRemoteNativeFeed;
          this.nativeFeeds = [feed];
          await this.pickNativeFeed(feed);
        } else {
          throw new Error('not supported');
        }
      }
      if (this.feed) {
        if (this.feed.nativeFeed) {
          await this.pickNativeFeed(this.feed.nativeFeed);
        } else if (this.feed.genericFeed) {
          await this.pickGenericFeed(this.feed.genericFeed);
        } else {
          throw new Error('not supported');
        }
      }
      this.statusChange.emit(this.isValid() ? 'valid' : 'invalid');
    } catch (e) {
      console.error(e);
    }
  }

  async pickNativeFeed(feed: GqlRemoteNativeFeed) {
    await this.resetSelection();
    if (this.currentNativeFeed !== feed) {
      this.currentNativeFeed = feed;
      // await assignNativeFeedToContext(feed, this.handler);
      this.selectedFeed = {
        nativeFeed: this.currentNativeFeed,
      };
      this.emitSelectedFeed();
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;
    this.changeRef.detectChanges();
  }

  async pickGenericFeed(genericFeed: GqlTransientGenericFeed) {
    await this.resetSelection();
    this.showSelectors = true;
    if (this.currentGenericFeed?.hash !== genericFeed.hash) {
      this.currentGenericFeed = cloneDeep(genericFeed);
      this.selectedFeed = {
        genericFeed: omit(this.currentGenericFeed, 'samples') as any,
      };
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;

    const selectors = genericFeed.selectors;
    this.formGroup.setValue({
      contextXPath: selectors.contextXPath,
      dateIsStartOfEvent: selectors.dateIsStartOfEvent,
      dateXPath: selectors.dateXPath,
      paginationXPath: selectors.paginationXPath || '',
      linkXPath: selectors.linkXPath,
      extendContext: selectors.extendContext,
    });
    this.emitSelectedFeed();

    this.changeRef.detectChanges();
  }

  getRelativeScore(genericFeed: GqlTransientGenericFeed): number {
    return this.scaleScore ? this.scaleScore(genericFeed.score) : 0;
  }

  // getExtendContextOptions(): LabelledSelectOption[] {
  //   return Object.values(GqlExtendContentOptions).map((option) => ({
  //     label: option,
  //     value: option,
  //   }));
  // }

  private async resetSelection() {
    this.showSelectors = false;
    this.currentGenericFeed = null;
    this.currentNativeFeed = null;
  }

  private isValid(): boolean {
    if (this.selectedFeed) {
      if (this.selectedFeed.genericFeed) {
        return this.formGroup.valid;
      }
      return true;
    }
    return false;
  }

  private emitSelectedFeed() {
    this.statusChange.emit(this.isValid() ? 'valid' : 'invalid');
    this.selectedFeedChange.emit(this.getSelectedFeed());
  }

  async previewGenericFeed() {
    const request = getScrapeRequest(
      this.getSelectedFeed(),
      this.scrapeRequest as GqlScrapeRequest,
    );
    await this.modalService.openRemoteFeedModal({
      feedProvider: () =>
        this.feedService.previewFeed({
          requests: [request],
          filters: [],
          tags: [],
        }),
    });
  }

  async pickGenericFeedBySelectors(
    selectors: Partial<GqlTransientGenericFeed['selectors']>,
  ) {
    console.log('pickGenericFeedBySelectors', selectors, this.genericFeeds);
    const contextXPath = selectors.contextXPath;
    const linkXPath = selectors.linkXPath;
    if (contextXPath && linkXPath) {
      const matchingGenericFeed = this.genericFeeds.find(
        (genericFeed) =>
          genericFeed.selectors.contextXPath === contextXPath &&
          genericFeed.selectors.linkXPath === linkXPath,
      );
      if (matchingGenericFeed) {
        await this.pickGenericFeed(matchingGenericFeed);
      } else {
        console.warn('no matching feed found');
      }
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.scrapeResponse?.currentValue) {
      console.log('changed scrapeResponse');
    }
  }

  private getSelectedFeed(): NativeOrGenericFeed {
    if (this.selectedFeed.nativeFeed) {
      return this.selectedFeed;
    } else {
      return {
        genericFeed: {
          selectors: {
            linkXPath: this.formGroup.value.linkXPath,
            contextXPath: this.formGroup.value.contextXPath,
            dateIsStartOfEvent: this.formGroup.value.dateIsStartOfEvent,
            extendContext: this.formGroup.value.extendContext,
            dateXPath: this.formGroup.value.dateXPath,
            paginationXPath: this.formGroup.value.paginationXPath,
          },
          hash: '',
          score: 0,
          count: 0,
        },
      };
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
