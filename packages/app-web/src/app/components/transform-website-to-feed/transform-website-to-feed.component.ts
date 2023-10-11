import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { GqlExtendContentOptions, GqlScrapeRequestInput } from '../../../generated/graphql';
import { ScrapeResponse, Selectors, TransientGenericFeed, TransientOrExistingNativeFeed } from '../../graphql/types';
import { Embeddable } from '../embedded-website/embedded-website.component';
import { ScaleLinear } from 'd3-scale';
import { cloneDeep, omit } from 'lodash-es';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard/wizard.module';
import { LabelledSelectOption } from '../wizard/wizard-generic-feeds/wizard-generic-feeds.component';

export interface NativeOrGenericFeed {
  genericFeed?: TransientGenericFeed
  nativeFeed?: TransientOrExistingNativeFeed
}

@Component({
  selector: 'app-transform-website-to-feed',
  templateUrl: './transform-website-to-feed.component.html',
  styleUrls: ['./transform-website-to-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransformWebsiteToFeedComponent  implements OnInit {

  @Input()
  scrapeRequest: GqlScrapeRequestInput;

  @Input()
  scrapeResponse: ScrapeResponse;

  @Input()
  feed: NativeOrGenericFeed;

  @Output()
  feedChanged: EventEmitter<NativeOrGenericFeed> = new EventEmitter<NativeOrGenericFeed>();

  formGroup: FormGroup<TypedFormControls<Selectors>> = new FormGroup<TypedFormControls<Selectors>>(
    {
      contextXPath: new FormControl('', [Validators.required]),
      dateXPath: new FormControl('', []),
      linkXPath: new FormControl('', [Validators.required]),
      dateIsStartOfEvent: new FormControl(false, [Validators.required]),
      extendContext: new FormControl(GqlExtendContentOptions.None, []),
      paginationXPath: new FormControl('', []),
    },
    { updateOn: 'change' },
  );

  genericFeeds: TransientGenericFeed[];
  nativeFeeds: TransientOrExistingNativeFeed[];

  constructor(private readonly changeRef: ChangeDetectorRef) { }

  currentNativeFeed: TransientOrExistingNativeFeed;
  currentGenericFeed: TransientGenericFeed;
  embedWebsiteData: Embeddable;
  isNonSelected = true;
  busy = false;
  private scaleScore: ScaleLinear<number, number, never>;
  showSelectors = false;

  async ngOnInit() {
    const feeds = this.scrapeResponse.elements[0].data[0].feeds;
    this.genericFeeds = feeds.genericFeeds;
    this.nativeFeeds = feeds.nativeFeeds;
    this.embedWebsiteData = {
      data: this.scrapeResponse.debug.html,
      mimeType: this.scrapeResponse.debug.contentType,
      url: this.scrapeRequest.page.url,
      viewport: this.scrapeRequest.page.prerender?.viewport
    }
    if (this.feed) {
      if (this.feed.nativeFeed) {
        await this.pickNativeFeed(this.feed.nativeFeed);
      } else if (this.feed.genericFeed) {
        await this.pickGenericFeed(this.feed.genericFeed);
      } else {
        throw new Error('not supported')
      }
    }
  }

  async pickNativeFeed(feed: TransientOrExistingNativeFeed) {
    await this.resetSelection();
    if (this.currentNativeFeed !== feed) {
      this.currentNativeFeed = feed;
      // await assignNativeFeedToContext(feed, this.handler);
      this.feedChanged.emit({
        nativeFeed: this.currentNativeFeed
      })
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;
    this.changeRef.detectChanges();
  }

  async pickGenericFeed(genericFeed: TransientGenericFeed) {
    await this.resetSelection();
    this.showSelectors = true;
    if (this.currentGenericFeed?.hash !== genericFeed.hash) {
      this.currentGenericFeed = cloneDeep(genericFeed);
      this.feedChanged.emit({
        genericFeed: omit(this.currentGenericFeed, 'samples')
      })
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;

    const selectors = genericFeed.selectors;
    this.formGroup.setValue({
      contextXPath: selectors.contextXPath,
      dateIsStartOfEvent: selectors.dateIsStartOfEvent,
      dateXPath: selectors.dateXPath,
      linkXPath: selectors.linkXPath,
      extendContext: selectors.extendContext,
      paginationXPath: selectors.paginationXPath,
    });

    this.changeRef.detectChanges();
  }

  getRelativeScore(genericFeed: TransientGenericFeed): number {
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

}
