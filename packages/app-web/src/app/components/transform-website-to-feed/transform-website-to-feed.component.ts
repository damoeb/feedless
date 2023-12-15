import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import {
  GqlExtendContentOptions,
  GqlMarkupTransformer,
  GqlRemoteOrExistingNativeFeed,
  GqlScrapedFeeds,
  GqlScrapeRequestInput,
  GqlTransientGenericFeed
} from '../../../generated/graphql';
import { ScrapeResponse, Selectors } from '../../graphql/types';
import { Embeddable } from '../embedded-website/embedded-website.component';
import { ScaleLinear } from 'd3-scale';
import { cloneDeep, omit } from 'lodash-es';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormControls } from '../wizard/wizard.module';
import { LabelledSelectOption } from '../wizard/wizard-generic-feeds/wizard-generic-feeds.component';
import { ModalController } from '@ionic/angular';

export interface NativeOrGenericFeed {
  genericFeed?: GqlTransientGenericFeed
  nativeFeed?: GqlRemoteOrExistingNativeFeed
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
    },
    { updateOn: 'change' },
  );

  genericFeeds: GqlTransientGenericFeed[];
  nativeFeeds: GqlRemoteOrExistingNativeFeed[];

  private selectedFeed: NativeOrGenericFeed;

  constructor(private readonly changeRef: ChangeDetectorRef,
              private readonly modalCtrl: ModalController) { }

  currentNativeFeed: GqlRemoteOrExistingNativeFeed;
  currentGenericFeed: GqlTransientGenericFeed;
  embedWebsiteData: Embeddable;
  isNonSelected = true;
  busy = false;
  private scaleScore: ScaleLinear<number, number, never>;
  showSelectors = false;

  async ngOnInit() {
    const element = this.scrapeResponse.elements[0];
    const feeds = JSON.parse(element.selector.fields.find(field => field.transformer.internal === GqlMarkupTransformer.Feeds).value.one.data) as GqlScrapedFeeds;
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

  async pickNativeFeed(feed: GqlRemoteOrExistingNativeFeed) {
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

  async pickGenericFeed(genericFeed: GqlTransientGenericFeed) {
    await this.resetSelection();
    this.showSelectors = true;
    if (this.currentGenericFeed?.hash !== genericFeed.hash) {
      this.currentGenericFeed = cloneDeep(genericFeed);
      this.selectedFeed = {
        genericFeed: omit(this.currentGenericFeed, 'samples') as any
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
    });

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
