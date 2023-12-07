import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { GqlExtendContentOptions, GqlScrapeEmitType, GqlScrapeRequestInput } from '../../../generated/graphql';
import { ScrapeResponse, Selectors, TransientGenericFeed, TransientOrExistingNativeFeed } from '../../graphql/types';
import { Embeddable } from '../embedded-website/embedded-website.component';
import { ScaleLinear } from 'd3-scale';
import { cloneDeep, omit } from 'lodash-es';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard/wizard.module';
import { LabelledSelectOption } from '../wizard/wizard-generic-feeds/wizard-generic-feeds.component';
import { ModalController } from '@ionic/angular';
import { isDefined } from '../../modals/feed-builder-modal/scrape-builder';

export interface NativeOrGenericFeed {
  genericFeed?: TransientGenericFeed
  nativeFeed?: TransientOrExistingNativeFeed
}

export interface TransformWebsiteToFeedComponentProps {
  scrapeRequest: GqlScrapeRequestInput;
  scrapeResponse: ScrapeResponse;
  feed: NativeOrGenericFeed
}

@Component({
  selector: 'app-transform-website-to-feed',
  templateUrl: './transform-website-to-feed.component.html',
  styleUrls: ['./transform-website-to-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransformWebsiteToFeedComponent implements OnInit, TransformWebsiteToFeedComponentProps {

  @Input()
  scrapeRequest: GqlScrapeRequestInput;

  @Input()
  scrapeResponse: ScrapeResponse;

  @Input()
  feed: NativeOrGenericFeed;

  formGroup: FormGroup<TypedFormControls<Selectors>> = new FormGroup<TypedFormControls<Selectors>>(
    {
      contextXPath: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.minLength(1)]}),
      dateXPath: new FormControl('', []),
      linkXPath: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.minLength(1)]}),
      dateIsStartOfEvent: new FormControl(false, {nonNullable: true, validators: [Validators.required]}),
      extendContext: new FormControl(GqlExtendContentOptions.None, []),
      paginationXPath: new FormControl('', []),
    },
    { updateOn: 'change' },
  );

  genericFeeds: TransientGenericFeed[];
  nativeFeeds: TransientOrExistingNativeFeed[];

  private selectedFeed: NativeOrGenericFeed;

  constructor(private readonly changeRef: ChangeDetectorRef,
              private readonly modalCtrl: ModalController) { }

  currentNativeFeed: TransientOrExistingNativeFeed;
  currentGenericFeed: TransientGenericFeed;
  embedWebsiteData: Embeddable;
  isNonSelected = true;
  busy = false;
  private scaleScore: ScaleLinear<number, number, never>;
  showSelectors = false;

  async ngOnInit() {
    const feeds = this.scrapeResponse.elements[0].selector.transformers.find(t => isDefined(t.internal.feeds))
      .internal.feeds;
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
      this.selectedFeed = {
        nativeFeed: this.currentNativeFeed
      }
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;
    this.changeRef.detectChanges();
  }

  async pickGenericFeed(genericFeed: TransientGenericFeed) {
    await this.resetSelection();
    this.showSelectors = true;
    if (this.currentGenericFeed?.hash !== genericFeed.hash) {
      this.currentGenericFeed = cloneDeep(genericFeed);
      this.selectedFeed = {
        genericFeed: omit(this.currentGenericFeed, 'samples')
      }
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

  dismissModal() {
    return this.modalCtrl.dismiss()
  }

  applyChanges() {
    return this.modalCtrl.dismiss(this.selectedFeed)
  }

  isValid(): boolean {
    if (this.selectedFeed) {
      if (this.selectedFeed.genericFeed) {
        return this.formGroup.valid;
      }
      return true;
    }
    return false;
  }
}
