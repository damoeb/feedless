import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { Embeddable } from '../embedded-website/embedded-website.component';
import {
  GqlFeedlessPlugins,
  GqlRetentionInput,
  GqlScrapeRequestInput,
  GqlVisibility,
} from '../../../generated/graphql';
import { ModalController, ToastController } from '@ionic/angular';
import { ScrapeService } from '../../services/scrape.service';
import { ActivatedRoute } from '@angular/router';
import { ScrapeResponse } from '../../graphql/types';
import { NativeOrGenericFeed } from '../../modals/transform-website-to-feed-modal/transform-website-to-feed-modal.component';
import { ProductConfig, ProductService } from '../../services/product.service';
import {
  FeedBuilderActionsModalComponent,
  FeedBuilderData,
} from '../../modals/feed-builder-actions-modal/feed-builder-actions-modal.component';
import { fixUrl, isValidUrl } from '../../app.module';
import { ApolloAbortControllerService } from '../../services/apollo-abort-controller.service';
import { ModalService } from '../../services/modal.service';
import { Agent } from '../../services/agent.service';

/**
 * IDEEN
 *     create feed from website
 *     merge 2 feeds and deduplicate using url/id
 *     use feed and filter title not includes 'Ad'
 *     track pixel page changes of [url], but ship latest text and latest image
 *     track text page changes of [url], but ship diff to first for 2 weeks
 *     track price of product on [url] by extracting field, but shipping product fragment as pixel and markup
 *     use existing feed -> readability, inline images and untrack urls
 *     generate feed, fix title by removing prefix, trim after length 20
 *     inbox: select feeds, filter last 24h, order by quality, pick best 12
 *     digest: select feed, send best 10 end of week as digest via mail
 *     create feed activate tracking
 *     create just the feed sink
 */

export enum FeedBuilderModalComponentExitRole {
  dismiss = 'dismiss',
  login = 'login',
}

type SinkTargetType = 'email' | 'webhook';

type ScheduledPolicy = {
  cronString: string;
};

type PluginRef = {
  id: string;
  data: object;
};

type FetchPolicy = {
  plugins?: PluginRef[];
} & ScheduledPolicy;

type FieldFilterType = 'include' | 'exclude';

type FieldFilterOperator = 'matches' | 'contains' | 'endsWith' | 'startsWith';
type FieldFilter = {
  type: FieldFilterType;
  field: string;
  negate: boolean;
  operator: FieldFilterOperator;
  value: string;
};

export type SegmentedOutput = {
  filter?: string;
  orderBy?: string;
  orderAsc?: boolean;
  size?: number;
  digest?: boolean;
  scheduled?: ScheduledPolicy;
};

type SinkTargetWrapper = {
  type: SinkTargetType;
  oneOf: SinkTarget;
};

type EmailSink = {
  address: string;
};
type WebhookSink = {
  url: string;
};
type SinkTarget = {
  email?: EmailSink;
  webhook?: WebhookSink;
};

export type Sink = {
  isSegmented: boolean;
  segmented?: SegmentedOutput;
  targets: SinkTargetWrapper[];
  hasRetention: boolean;
  retention: GqlRetentionInput;
  visibility: GqlVisibility;
  title: string;
  description: string;
};

export type Source = {
  // output?: ScrapeField | ScrapeField[]
  request: GqlScrapeRequestInput;
  response?: ScrapeResponse;
};

export type FeedBuilder = {
  sources: Source[];
  agent?: Agent;
  fetch: ScheduledPolicy;
  filters: FieldFilter[];
  sink: Sink;
};

export type FeedWithRequest = {
  scrapeRequest: GqlScrapeRequestInput;
  feed: NativeOrGenericFeed;
};

@Component({
  selector: 'app-feed-builder',
  templateUrl: './feed-builder.component.html',
  styleUrls: ['./feed-builder.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedBuilderComponent implements OnInit, OnDestroy {
  url: string;
  scrapeResponse: ScrapeResponse;
  embedWebsite: Embeddable;
  loading = false;

  @Input()
  submitButtonText = 'Finalize Feed';

  @Input()
  scrapeRequest: GqlScrapeRequestInput;
  hasFeed: boolean;
  selectedFeed: NativeOrGenericFeed;
  productConfig: ProductConfig;
  private subscriptions: Subscription[] = [];
  errorMessage: string;

  @Input()
  hideSearchBar = false;

  @Output()
  selectedFeedChanged = new EventEmitter<FeedWithRequest>();
  protected tags: string[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    private readonly apolloAbortController: ApolloAbortControllerService,
    private readonly scrapeService: ScrapeService,
    private readonly modalService: ModalService,
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    if (this.scrapeRequest) {
      this.url = this.scrapeRequest.page.url;

      await this.scrapeUrl();
    }
    this.subscriptions.push(
      this.productService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
      this.activatedRoute.queryParams.subscribe(async (params) => {
        if (params.url?.length > 0) {
          await this.handleQuery(params.url);
        }
      }),
    );
  }

  async scrapeUrl() {
    if (!this.url) {
      return;
    }
    if (!isValidUrl(this.url)) {
      this.url = fixUrl(this.url);
    }

    try {
      console.log(`scrape ${this.url}`);
      this.errorMessage = null;
      this.loading = true;
      this.scrapeResponse = null;
      this.changeRef.detectChanges();

      this.scrapeRequest = {
        tags: [],
        page: {
          url: this.url,
        },
        emit: [
          {
            selectorBased: {
              xpath: {
                value: '/',
              },
              expose: {
                transformers: [
                  {
                    pluginId: GqlFeedlessPlugins.OrgFeedlessFeeds,
                    params: {},
                  },
                ],
              },
            },
          },
        ],
      };

      this.handleResponse(await this.scrapeService.scrape(this.scrapeRequest));
    } catch (e) {
      this.errorMessage = e.message;
    }

    this.loading = false;
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async finalizeFeed() {
    if (!this.hasFeed) {
      const toast = await this.toastCtrl.create({
        message: 'Pick a feed',
        color: 'danger',
        duration: 2000,
        position: 'bottom',
        cssClass: 'tiny-toast',
      });
      await toast.present();
      return;
    }

    this.scrapeRequest.tags = this.tags;
    this.selectedFeedChanged.emit({
      scrapeRequest: this.scrapeRequest,
      feed: this.selectedFeed,
    });
  }

  async showActionsModal() {
    const modal = await this.modalCtrl.create({
      component: FeedBuilderActionsModalComponent,
      cssClass: 'fullscreen-modal',
      componentProps: {
        url: this.url,
      },
    });

    await modal.present();
    const result = await modal.onDidDismiss<FeedBuilderData>();
    if (result.data) {
      this.scrapeRequest = null;
      this.scrapeResponse = null;
      this.embedWebsite = null;
      this.changeRef.detectChanges();

      this.scrapeRequest = result.data.request;
      this.handleResponse(result.data.response);
      this.changeRef.detectChanges();
    }
  }

  handleQuery(url: string) {
    this.url = url;
    return this.scrapeUrl();
  }

  handleCancel() {
    console.log('handleCancel');
    this.apolloAbortController.abort('user canceled');
  }

  private handleResponse(scrapeResponse: ScrapeResponse) {
    this.scrapeResponse = scrapeResponse;

    const { contentType } = scrapeResponse.debug;
    if (contentType.startsWith('text/html')) {
      this.embedWebsite = {
        mimeType: 'text/html',
        data: this.scrapeResponse.elements[0].selector.html.data,
        url: this.url,
      };
    } else {
      console.warn(`Unsupported contentType ${contentType}`);
    }
  }

  async showTagsModal() {
    this.tags = await this.modalService.openTagModal({
      tags: this.tags || [],
    });
    this.changeRef.detectChanges();
  }

  getTagsString() {
    if (this.tags) {
      return tagsToString(this.tags);
    } else {
      return '';
    }
  }
}

export function tagsToString(tags: string[]): string {
  if (tags) {
    return tags.map((tag) => `#${tag}`).join(' ');
  }
}
