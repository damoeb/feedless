import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import {
  GqlExtendContentOptions,
  GqlFeedlessPlugins,
  GqlNativeFeed,
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
import { NativeOrGenericFeed } from '../../modals/transform-website-to-feed-modal/transform-website-to-feed-modal.component';
import { ModalService } from '../../services/modal.service';
import { FeedService } from '../../services/feed.service';
import { getScrapeRequest } from '../../modals/generate-feed-modal/generate-feed-modal.component';

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
export class TransformWebsiteToFeedComponent implements OnInit {
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
      linkXPath: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(1)],
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
  nativeFeeds: GqlNativeFeed[] = [];
  currentNativeFeed: GqlNativeFeed;
  currentGenericFeed: GqlTransientGenericFeed;
  embedWebsiteData: Embeddable;
  isNonSelected = true;
  busy = false;
  showSelectors = false;
  private selectedFeed: NativeOrGenericFeed;
  private scaleScore: ScaleLinear<number, number, never>;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly feedService: FeedService,
    private readonly modalService: ModalService,
  ) {}

  async ngOnInit() {
    try {
      const elementWithFeeds = this.scrapeResponse.elements.find((element) =>
        element.selector.fields.some(
          (field) => field.name === GqlFeedlessPlugins.OrgFeedlessFeeds,
        ),
      );
      if (elementWithFeeds) {
        const feeds = JSON.parse(
          elementWithFeeds.selector.fields.find(
            (field) => field.name === GqlFeedlessPlugins.OrgFeedlessFeeds,
          ).value.one.data,
        ) as GqlScrapedFeeds;
        this.genericFeeds = feeds.genericFeeds;
        this.nativeFeeds = feeds.nativeFeeds;
        const scores = feeds.genericFeeds.map((gf) => gf.score);
        const maxScore = max(scores);
        const minScore = min(scores);
        this.scaleScore = scaleLinear()
          .domain([minScore, maxScore])
          .range([0, 100]);

        this.embedWebsiteData = {
          data: this.scrapeResponse.debug.html,
          mimeType: this.scrapeResponse.debug.contentType,
          url: this.scrapeRequest.page.url,
          viewport: this.scrapeRequest.page.prerender?.viewport,
        };
      } else {
        const elementWithFeed = this.scrapeResponse.elements.find((element) =>
          element.selector.fields.some(
            (field) => field.name === GqlFeedlessPlugins.OrgFeedlessFeed,
          ),
        );
        if (elementWithFeed) {
          const feed = JSON.parse(
            elementWithFeed.selector.fields.find(
              (field) => field.name === GqlFeedlessPlugins.OrgFeedlessFeed,
            ).value.one.data,
          ) as GqlNativeFeed;
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

  async pickNativeFeed(feed: GqlNativeFeed) {
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
      linkXPath: selectors.linkXPath,
      extendContext: selectors.extendContext,
    });
    this.emitSelectedFeed();

    this.changeRef.detectChanges();
  }

  getRelativeScore(genericFeed: GqlTransientGenericFeed): number {
    return this.scaleScore ? this.scaleScore(genericFeed.score) : 0;
  }

  getExtendContextOptions(): LabelledSelectOption[] {
    return Object.values(GqlExtendContentOptions).map((option) => ({
      label: option,
      value: option,
    }));
  }

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
    this.selectedFeedChange.emit(this.selectedFeed);
  }

  async previewGenericFeed() {
    await this.modalService.openRemoteFeedModal({
      feedProvider: () =>
        this.feedService.previewFeed({
          requests: [
            getScrapeRequest(
              this.selectedFeed,
              this.scrapeRequest as GqlScrapeRequest,
            ),
          ],
          filters: [],
          tags: [],
        }),
    });
  }
}
