import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { GqlScrapeRequestInput } from '../../../generated/graphql';
import { ScrapeResponse, TransientGenericFeed, TransientOrExistingNativeFeed } from '../../graphql/types';
import { Embeddable } from '../embedded-website/embedded-website.component';
import { ScaleLinear } from 'd3-scale';
import { cloneDeep } from 'lodash-es';

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

  genericFeeds: TransientGenericFeed[];
  nativeFeeds: TransientOrExistingNativeFeed[];

  constructor(private readonly changeRef: ChangeDetectorRef) { }

  currentNativeFeed: TransientOrExistingNativeFeed;
  currentGenericFeed: TransientGenericFeed;
  embedWebsiteData: Embeddable;
  isNonSelected = true;
  busy = false;
  private scaleScore: ScaleLinear<number, number, never>;

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
    if (this.currentGenericFeed?.hash !== genericFeed.hash) {
      this.currentGenericFeed = cloneDeep(genericFeed);
      this.feedChanged.emit({
        genericFeed: this.currentGenericFeed
      })
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;
    this.changeRef.detectChanges();
  }

  getRelativeScore(genericFeed: TransientGenericFeed): number {
    return this.scaleScore ? this.scaleScore(genericFeed.score) : 0;
  }

  private async resetSelection() {
    this.currentGenericFeed = null;
    this.currentNativeFeed = null;
  }

}
