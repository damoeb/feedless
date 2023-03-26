import { FeedDiscoveryResult, FeedService } from '../../services/feed.service';
import { ChangeDetectorRef } from '@angular/core';
import { WizardContext, WizardStepId } from './wizard/wizard.component';
import { webToFeedParams } from '../api-params';
import { GqlExtendContentOptions } from '../../../generated/graphql';
import { ServerSettingsService } from '../../services/server-settings.service';
import { debounce, DebouncedFunc } from 'lodash-es';

export class WizardHandler {
  private discovery: FeedDiscoveryResult;
  private debouncedDetectChanges: DebouncedFunc<() => void>;

  constructor(
    private context: WizardContext,
    private readonly feedService: FeedService,
    private readonly serverSettingsService: ServerSettingsService,
    private readonly changeRef: ChangeDetectorRef
  ) {
    this.debouncedDetectChanges = debounce(() => {
      console.log('feedUrl', this.context.feedUrl);
      this.changeRef.detectChanges();
    }, 500);
  }

  getContext(): WizardContext {
    return this.context;
  }

  async updateContext(update: Partial<WizardContext>) {
    this.context = {
      ...this.context,
      ...update,
    };

    await this.hooks(update);

    this.debouncedDetectChanges();
  }

  async init() {
    await this.hooks(this.context);
  }

  hasMimeType(mime: string): boolean {
    const discovery = this.getDiscovery();
    return (
      discovery &&
      !discovery.failed &&
      discovery.document.mimeType.startsWith(mime)
    );
  }

  getDiscovery(): FeedDiscoveryResult {
    return this.discovery;
  }

  hasEmptyHistory(): boolean {
    return this.context.history.length === 0;
  }

  getCurrentStepId(): WizardStepId {
    return this.context.currentStepId;
  }

  private async fetchDiscovery() {
    const fetchOptions = this.context.fetchOptions;
    this.discovery = await this.feedService.discoverFeeds({
      fetchOptions: {
        websiteUrl: fetchOptions.websiteUrl,
        prerender: fetchOptions.prerender,
        prerenderScript: fetchOptions.prerenderScript,
        prerenderWaitUntil: fetchOptions.prerenderWaitUntil,
        prerenderWithoutMedia: false,
      },
      parserOptions: {
        strictMode: false,
      },
    });
  }

  private async updateGenericFeedUrl() {
    const selectors =
      this.context.feed?.create?.genericFeed?.specification?.selectors;
    if (!selectors) {
      return;
    }
    const str = (value: boolean | number): string => `${value}`;

    const searchParams = new URLSearchParams();
    searchParams.set(webToFeedParams.version, '0.1');
    searchParams.set(webToFeedParams.url, this.discovery.websiteUrl);
    searchParams.set(webToFeedParams.contextPath, selectors.contextXPath);
    searchParams.set(webToFeedParams.paginationPath, selectors.paginationXPath);
    searchParams.set(webToFeedParams.datePath, selectors.dateXPath);
    searchParams.set(webToFeedParams.linkPath, selectors.linkXPath);
    searchParams.set(
      webToFeedParams.eventFeed,
      str(selectors.dateIsStartOfEvent)
    );
    searchParams.set(
      webToFeedParams.extendContent,
      this.toExtendContextParam(selectors.extendContext)
    );
    searchParams.set(
      webToFeedParams.prerender,
      str(this.context.fetchOptions.prerender)
    );
    searchParams.set(webToFeedParams.strictMode, str(false));
    searchParams.set(
      webToFeedParams.prerenderWaitUntil,
      this.context.fetchOptions.prerenderWaitUntil
    );

    this.context.feedUrl =
      this.serverSettingsService.getApiUrls().webToFeed +
      '?' +
      searchParams.toString();
  }
  private async updateNativeFeedUrl() {
    if (this.context.feed?.create?.nativeFeed?.feedUrl) {
      this.context.feedUrl = this.context.feed.create.nativeFeed.feedUrl;
    }
    if (this.context.feed?.connect?.id) {
      this.context.feedUrl = this.context.feed.create.nativeFeed.feedUrl;
    }
  }

  private async updateFeedUrl() {
    await this.updateNativeFeedUrl();
    await this.updateGenericFeedUrl();
  }

  private toExtendContextParam(extendContext: GqlExtendContentOptions): string {
    switch (extendContext) {
      case GqlExtendContentOptions.PreviousAndNext:
        return 'pn';
      default:
        return extendContext.toString()[0].toLowerCase();
    }
  }

  private async hooks(context: Partial<WizardContext>) {
    if (context.fetchOptions?.websiteUrl) {
      await this.fetchDiscovery();
    }
    if (context.feed) {
      await this.updateFeedUrl();
    }
  }
}