import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { Location } from '@angular/common';
import { Subscription } from 'rxjs';
import {
  GqlExtendContentOptions,
  GqlFeedlessPlugins,
  GqlNativeFeed,
  GqlProductCategory,
  GqlRemoteNativeFeed,
  GqlScrapeRequestInput,
  GqlTransientGenericFeed,
} from '../../../generated/graphql';
import {
  AlertController,
  ModalController,
  ToastController,
} from '@ionic/angular';
import { ScrapeService } from '../../services/scrape.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Repository, ScrapeResponse } from '../../graphql/types';
import {
  AppConfigService,
  ProductConfig,
} from '../../services/app-config.service';
import {
  InteractiveWebsiteModalComponent,
  FeedBuilderData,
  InteractiveWebsiteModalComponentProps,
} from '../../modals/interactive-website-modal/interactive-website-modal.component';
import { fixUrl, isValidUrl } from '../../app.module';
import { ApolloAbortControllerService } from '../../services/apollo-abort-controller.service';
import { ModalService } from '../../services/modal.service';
import { TransformWebsiteToFeedComponent } from '../transform-website-to-feed/transform-website-to-feed.component';
import { OsmMatch } from '../../services/open-street-map.service';
import { getFirstFetchUrlLiteral } from '../../utils';
import { RepositoryService } from '../../services/repository.service';
import { Embeddable } from '../embedded-image/embedded-image.component';

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

export interface NativeOrGenericFeed {
  genericFeed?: GqlTransientGenericFeed;
  nativeFeed?: GqlRemoteNativeFeed;
}

export enum FeedBuilderModalComponentExitRole {
  dismiss = 'dismiss',
  login = 'login',
}

export type Source = {
  // output?: ScrapeField | ScrapeField[]
  request: GqlScrapeRequestInput;
  response?: ScrapeResponse;
};

export type FeedOrRepository = {
  feed: FeedWithRequest;
  repository: Repository;
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

  @ViewChild('webToFeedTransformer')
  webToFeedTransformerComponent: TransformWebsiteToFeedComponent;

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

  @Output()
  selectedRepositoryChanged = new EventEmitter<Repository>();

  protected tags: string[] = [];
  protected geoLocation: OsmMatch;
  protected repositories: Repository[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly appConfigService: AppConfigService,
    private readonly apolloAbortController: ApolloAbortControllerService,
    private readonly scrapeService: ScrapeService,
    private readonly modalService: ModalService,
    private readonly location: Location,
    private readonly router: Router,
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
    private readonly alertCtrl: AlertController,
    private readonly repositoryService: RepositoryService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    if (this.scrapeRequest) {
      this.url = getFirstFetchUrlLiteral(this.scrapeRequest.flow.sequence);

      await this.scrapeUrl();
    }
    this.subscriptions.push(
      this.appConfigService
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

    this.fetchFeeds();
  }

  remix(repository: Repository) {
    this.selectedRepositoryChanged.emit(repository);
  }

  private async fetchFeeds() {
    const page = 0;
    const repositories = await this.repositoryService.listRepositories({
      cursor: {
        page,
      },
      where: {
        product: {
          in: [GqlProductCategory.RssProxy],
        },
      },
    });
    this.repositories.push(...repositories);
    this.changeRef.detectChanges();
  }

  async scrapeUrl() {
    if (!this.url) {
      return;
    }
    if (!isValidUrl(this.url)) {
      this.url = fixUrl(this.url);
    }
    await this.patchUrl();
    await this.detectLegacyRssProxy();

    try {
      console.log(`scrape ${this.url}`);
      this.errorMessage = null;
      this.loading = true;
      this.scrapeResponse = null;
      this.changeRef.detectChanges();

      this.scrapeRequest = {
        title: `From ${this.url}`,
        tags: [],
        flow: {
          sequence: [
            {
              fetch: {
                get: {
                  url: {
                    literal: this.url,
                  },
                },
              },
            },
            {
              execute: {
                pluginId: GqlFeedlessPlugins.OrgFeedlessFeeds,
                params: {},
              },
            },
          ],
        },
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
    console.log('this.location', this.geoLocation);
    if (this.geoLocation) {
      this.scrapeRequest.localized = {
        lat: parseFloat(this.geoLocation.lat),
        lon: parseFloat(this.geoLocation.lon),
      };
    }
    this.selectedFeedChanged.emit({
      scrapeRequest: this.scrapeRequest,
      feed: this.selectedFeed,
    });
  }

  async showInteractiveWebsiteModal() {
    const componentProps: InteractiveWebsiteModalComponentProps = {
      scrapeRequest: this.scrapeRequest,
    };
    const modal = await this.modalCtrl.create({
      component: InteractiveWebsiteModalComponent,
      cssClass: 'fullscreen-modal',
      componentProps,
    });

    this.errorMessage = null;

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

    const fetchAction = scrapeResponse.outputs.find((o) => o.response.fetch)
      .response.fetch;
    const { contentType } = fetchAction.debug;
    if (contentType.startsWith('text/html')) {
      this.embedWebsite = {
        mimeType: 'text/html',
        data: fetchAction.data,
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

  async showLocationPickerModal() {
    this.geoLocation = await this.modalService.openSearchAddressModal();
    console.log('this.location', this.geoLocation);
    this.changeRef.detectChanges();
  }

  getTagsString() {
    if (this.tags) {
      return tagsToString(this.tags);
    } else {
      return '';
    }
  }

  private async detectLegacyRssProxy() {
    const legacyPathFragemnts = [
      '/api/tf',
      '/api/w2f',
      '/api/web-to-feed',
      '/api/w2f/rule',
      '/api/w2f/change',
      '/api/legacy/tf',
      '/api/legacy/w2f',
    ];

    if (
      legacyPathFragemnts.some(
        (pathFragemnt) => this.url.indexOf(pathFragemnt) > -1,
      )
    ) {
      const alert = await this.alertCtrl.create({
        header: 'Legacy URL detected',
        backdropDismiss: false,
        message:
          'This URL looks like an old RSS-proxy url, do you want to convert it?',
        cssClass: 'primary-alert',
        buttons: [
          {
            role: 'cancel',
            text: 'Skip',
            handler: () => {},
          },
          {
            role: 'ok',
            cssClass: 'confirm-button',
            text: 'Convert',
            handler: () => this.convertLegacyRssProxyUrl(),
          },
        ],
      });

      await alert.present();
    }
  }

  private async convertLegacyRssProxyUrl() {
    await this.alertCtrl.dismiss();
    const url = new URL(this.url);
    const params = url.search
      .substring(1)
      .split('&')
      .reduce(
        (params, param) => {
          const parts = param.split('=', 2);
          params[parts[0]] = decodeURIComponent(parts[1]);
          return params;
        },
        {} as { [s: string]: string },
      );
    console.log('legacy url params', params);

    this.url = params['url'];
    await this.scrapeUrl();
    setTimeout(() => {
      this.webToFeedTransformerComponent.pickGenericFeedBySelectors({
        contextXPath: params['contextXPath'],
        linkXPath: params['linkXPath'],
      });
    }, 200);
  }

  private async patchUrl() {
    const url = this.router
      .createUrlTree(['.'], {
        queryParams: { url: this.url },
        relativeTo: this.activatedRoute,
      })
      .toString();
    this.location.replaceState(url);
  }
}

export function tagsToString(tags: string[]): string {
  if (tags) {
    return tags.map((tag) => `#${tag}`).join(' ');
  }
}
