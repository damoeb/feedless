import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { WizardContext } from '../wizard/wizard.component';
import {
  Selectors,
  TransientGenericFeed,
  TransientNativeFeed,
} from '../../../services/feed.service';
import { cloneDeep, max, min } from 'lodash';
import { GqlExtendContentOptions } from '../../../../generated/graphql';
import { FormGroup } from '@angular/forms';
import { TypedFormControls } from '../wizard.module';
import { EmbedWebsite } from '../../embedded-website/embedded-website.component';
import { ScaleLinear, scaleLinear } from 'd3-scale';
import { WizardHandler } from '../wizard-handler';

@Component({
  selector: 'app-wizard-feeds',
  templateUrl: './wizard-feeds.component.html',
  styleUrls: ['./wizard-feeds.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardFeedsComponent implements OnInit, OnChanges {
  @Input()
  handler: WizardHandler;

  currentNativeFeed: TransientNativeFeed;
  currentSelectors: Selectors | null = {
    contextXPath: '',
    paginationXPath: '',
    dateIsStartOfEvent: false,
    dateXPath: '',
    extendContext: GqlExtendContentOptions.None,
    linkXPath: '',
  };
  currentGenericFeed: TransientGenericFeed;
  formGroup: FormGroup<
    TypedFormControls<
      Pick<
        WizardContext,
        'url' | 'prerender' | 'prerenderWaitUntil' | 'prerenderScript'
      >
    >
  >;
  embedWebsiteData: EmbedWebsite;
  isNonSelected = true;
  private scaleScore: ScaleLinear<number, number, never>;

  constructor(private readonly changeRef: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    const discovery = this.handler.getDiscovery();
    if (
      changes.context?.currentValue &&
      this.embedWebsiteData?.url !== discovery.websiteUrl
    ) {
      this.embedWebsiteData = {
        htmlBody: discovery.document.htmlBody,
        mimeType: discovery.document.mimeType,
        url: discovery.websiteUrl,
      };
    }
    this.init();
  }

  ngOnInit() {
    this.init();
  }

  async pickNativeFeed(nativeFeed: TransientNativeFeed) {
    await this.resetSelection();
    if (this.currentNativeFeed !== nativeFeed) {
      this.currentNativeFeed = nativeFeed;
      await this.handler.updateContext({ feedUrl: nativeFeed.url });
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;
    this.changeRef.detectChanges();
  }

  async pickGenericFeed(genericFeed: TransientGenericFeed) {
    await this.resetSelection();
    if (this.currentGenericFeed?.hash !== genericFeed.hash) {
      this.currentGenericFeed = cloneDeep(genericFeed);
      this.currentSelectors = cloneDeep(this.currentGenericFeed.selectors);
      await this.handler.updateContext({
        genericFeed: this.currentGenericFeed,
      });
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;
    this.changeRef.detectChanges();
  }

  getFeedUrl(): string {
    if (this.currentNativeFeed) {
      return this.currentNativeFeed.url;
    } else {
      return this.currentGenericFeed.feedUrl;
    }
  }

  isWebsite(): boolean {
    return this.handler.hasMimeType('text/html');
  }

  getRelativeScore(genericFeed: TransientGenericFeed): number {
    return this.scaleScore(genericFeed.score);
  }

  private init() {
    const scores = this.handler
      .getDiscovery()
      .genericFeeds.feeds.map((gf) => gf.score);
    const maxScore = max(scores);
    const minScore = min(scores);
    this.scaleScore = scaleLinear()
      .domain([minScore, maxScore])
      .range([0, 100]);
    this.changeRef.detectChanges();
  }

  private async resetSelection() {
    this.currentGenericFeed = null;
    this.currentSelectors = null;
    this.currentNativeFeed = null;
    await this.handler.updateContext({ feedUrl: '', genericFeed: undefined });
  }
}
