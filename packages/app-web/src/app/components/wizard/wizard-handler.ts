import { FeedService } from '../../services/feed.service';
import { WizardContext, WizardStepId } from './wizard/wizard.component';
import { webToFeedParams, webToPageChangeParams } from '../api-params';
import { GqlExtendContentOptions } from '../../../generated/graphql';
import { ServerSettingsService } from '../../services/server-settings.service';
import {
  DebouncedFunc,
  flatMap,
  get,
  isBoolean,
  isNumber,
  isString,
  isUndefined,
  set,
} from 'lodash-es';
import { BehaviorSubject, Observable, ReplaySubject } from 'rxjs';
import { debounce } from 'lodash-es';
import { isUrl } from '../../pages/getting-started/getting-started.page';
import { FeedDiscoveryResult } from '../../graphql/types';

export type WizardContextChange = Partial<WizardContext>;

export type DeepPartial<T> = T extends object
  ? {
      [P in keyof T]?: DeepPartial<T[P]>;
    }
  : T;

export class WizardHandler {
  private discovery: FeedDiscoveryResult;
  private readonly contextChange =
    new BehaviorSubject<WizardContextChange | null>(null);
  private readonly fetchDiscoveryDebounced: DebouncedFunc<any>;

  constructor(
    private context: WizardContext,
    private readonly feedService: FeedService,
    private readonly serverSettingsService: ServerSettingsService
  ) {
    this.fetchDiscoveryDebounced = debounce(
      this.fetchDiscovery.bind(this),
      200
    );
  }

  onContextChange(): Observable<WizardContextChange | null> {
    return this.contextChange.asObservable();
  }

  destroy() {
    this.contextChange.complete();
    this.contextChange.unsubscribe();
  }

  getContext(): WizardContext {
    return this.context;
  }

  async updateContextPartial(
    update: DeepPartial<WizardContext>
  ): Promise<void> {
    const recursive = (path: string): { path: string; value: any }[] => {
      const obj = get(update, path) || update;
      return flatMap(Object.keys(obj), (attr) => {
        if (
          isString(obj[attr]) ||
          isBoolean(obj[attr]) ||
          isNumber(obj[attr])
        ) {
          return {
            path: path + '.' + attr,
            value: obj[attr],
          };
        } else {
          return recursive(path ? path + '.' + attr : attr);
        }
      });
    };
    recursive('').forEach((path) => {
      console.log(path.path, '->', path.value);
      set(this.context, path.path, path.value);
    });
  }

  async updateContext(update: Partial<WizardContext>): Promise<void> {
    // console.log('updateContext', update);
    this.context = {
      ...this.context,
      ...update,
    };

    await this.hooks(update);

    this.contextChange.next(update);
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
    return this.context.stepId;
  }

  private async fetchDiscovery() {
    const fetchOptions = this.context.fetchOptions;
    if (fetchOptions.websiteUrl?.length > 10) {
      this.setBusyFlag(true);
      this.discovery = await this.feedService.discoverFeeds({
        fetchOptions: {
          websiteUrl: fetchOptions.websiteUrl,
          prerender: fetchOptions.prerender,
          prerenderScript: fetchOptions.prerenderScript,
          prerenderWaitUntil: fetchOptions.prerenderWaitUntil,
        },
      });
      this.setBusyFlag(false);
    }
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

    const feedUrl =
      this.serverSettingsService.getApiUrls().webToFeed +
      '?' +
      searchParams.toString();

    this.setFeedUrl(feedUrl);
  }

  private async updatePageChangeFeedUrl() {
    const fragmentWatchFeed = this.context.feed?.create?.fragmentWatchFeed;
    if (!fragmentWatchFeed) {
      return;
    }
    const str = (value: boolean | number): string => `${value}`;

    const searchParams = new URLSearchParams();
    searchParams.set(webToPageChangeParams.version, '0.1');
    searchParams.set(webToPageChangeParams.url, this.discovery.websiteUrl);
    searchParams.set(
      webToPageChangeParams.prerender,
      str(fragmentWatchFeed.fetchOptions.prerender)
    );
    searchParams.set(
      webToPageChangeParams.prerenderScript,
      fragmentWatchFeed.fetchOptions.prerenderScript
    );
    searchParams.set(
      webToPageChangeParams.prerenderWaitUntil,
      fragmentWatchFeed.fetchOptions.prerenderWaitUntil
    );
    searchParams.set(webToPageChangeParams.type, fragmentWatchFeed.compareBy);
    searchParams.set(webToPageChangeParams.format, 'atom');
    searchParams.set(
      webToPageChangeParams.xpath,
      fragmentWatchFeed.fragmentXpath
    );

    const feedUrl =
      this.serverSettingsService.getApiUrls().webToPageChange +
      '?' +
      searchParams.toString();

    this.setFeedUrl(feedUrl);
  }

  private async updateNativeFeedUrl() {
    if (this.context.feed?.create?.nativeFeed?.feedUrl) {
      this.setFeedUrl(this.context.feed.create.nativeFeed.feedUrl);
    }
    if (this.context.feed?.connect?.id) {
      this.setBusyFlag(true);
      const nativeFeed = await this.feedService.getNativeFeed({
        where: {
          id: this.context.feed.connect.id,
        },
      });
      this.setBusyFlag(false);
      this.setFeedUrl(nativeFeed.feedUrl);
    }
  }

  private async updateFeedUrl() {
    await this.updateNativeFeedUrl();
    await this.updateGenericFeedUrl();
    await this.updatePageChangeFeedUrl();
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
    if (
      isUrl(context.fetchOptions?.websiteUrl) &&
      (!isUndefined(context.fetchOptions?.prerender) ||
        !isUndefined(context.fetchOptions?.prerenderScript) ||
        !isUndefined(context.fetchOptions?.prerenderWaitUntil))
    ) {
      await this.fetchDiscoveryDebounced();
    }
    if (context.feed) {
      await this.updateFeedUrl();
    }
  }

  private setBusyFlag(busy: boolean) {
    this.context.busy = busy;
    this.contextChange.next({
      busy,
    });
  }

  private setFeedUrl(feedUrl: string) {
    console.log('feedUrl', feedUrl);
    this.context.feedUrl = feedUrl;
    this.contextChange.next({
      feedUrl,
    });
  }
}
