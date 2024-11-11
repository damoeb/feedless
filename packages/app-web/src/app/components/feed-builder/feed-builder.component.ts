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
  GqlItemFilterParamsInput,
  GqlProductCategory,
  GqlRemoteNativeFeed,
  GqlSourceInput,
  GqlTransientGenericFeed,
} from '../../../generated/graphql';
import { AlertController, ModalController } from '@ionic/angular/standalone';
import { ScrapeService } from '../../services/scrape.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Repository, ScrapeResponse } from '../../graphql/types';
import {
  AppConfigService,
  ProductConfig,
} from '../../services/app-config.service';
import {
  InteractiveWebsiteModalComponent,
  InteractiveWebsiteModalComponentProps,
} from '../../modals/interactive-website-modal/interactive-website-modal.component';
import { fixUrl, isValidUrl } from '../../app.module';
import { ApolloAbortControllerService } from '../../services/apollo-abort-controller.service';
import { ModalService } from '../../services/modal.service';
import { TransformWebsiteToFeedComponent } from '../transform-website-to-feed/transform-website-to-feed.component';
import { OsmMatch } from '../../services/open-street-map.service';
import { RepositoryService } from '../../services/repository.service';
import { SourceBuilder } from '../interactive-website/source-builder';
import { addIcons } from 'ionicons';
import {
  logoJavascript,
  settingsOutline,
  checkmarkOutline,
} from 'ionicons/icons';

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
  request: GqlSourceInput;
  response?: ScrapeResponse;
};

export type FeedOrRepository = {
  feed: FeedWithRequest;
  repository: Repository;
};
export type FeedWithRequest = {
  source: GqlSourceInput;
  feed: NativeOrGenericFeed;
  refine: boolean;
};

@Component({
  selector: 'app-feed-builder',
  templateUrl: './feed-builder.component.html',
  styleUrls: ['./feed-builder.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedBuilderComponent implements OnInit, OnDestroy {
  url: string;

  loading = false;

  @ViewChild('webToFeedTransformer')
  webToFeedTransformerComponent: TransformWebsiteToFeedComponent;

  @Input()
  submitButtonText = 'Create Feed';

  @Input()
  source: GqlSourceInput;

  selectedFeed: NativeOrGenericFeed;
  productConfig: ProductConfig;
  private subscriptions: Subscription[] = [];
  errorMessage: string;

  @Input()
  hideSearchBar = false;

  @Input()
  hideCustomizeFeed = false;

  @Output()
  selectedFeedChanged = new EventEmitter<FeedWithRequest>();

  @Output()
  selectedRepositoryChanged = new EventEmitter<Repository>();

  protected tags: string[] = [];
  protected geoLocation: OsmMatch;
  protected repositories: Repository[] = [];
  hasValidFeed: boolean;
  protected sourceBuilder: SourceBuilder;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly appConfigService: AppConfigService,
    private readonly apolloAbortController: ApolloAbortControllerService,
    private readonly scrapeService: ScrapeService,
    private readonly modalService: ModalService,
    private readonly location: Location,
    private readonly router: Router,
    private readonly modalCtrl: ModalController,
    private readonly alertCtrl: AlertController,
    private readonly repositoryService: RepositoryService,
    private readonly changeRef: ChangeDetectorRef,
  ) {
    addIcons({ logoJavascript, settingsOutline, checkmarkOutline });
  }

  async ngOnInit() {
    if (this.source) {
      console.log('this.source', this.source);
      this.sourceBuilder = SourceBuilder.fromSource(
        this.source,
        this.scrapeService,
      );
      this.url = this.sourceBuilder.getUrl();
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
          await this.receiveUrl(params.url);
        }
      }),
    );

    await this.fetchFeeds();
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
          eq: GqlProductCategory.Feedless,
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
    await this.patchUrlInAddressBar();
    await this.detectLegacyRssProxy();

    try {
      console.log(`scrape ${this.url}`);
      this.errorMessage = null;
      this.loading = true;
      this.changeRef.detectChanges();

      if (this.sourceBuilder) {
        console.log('patch fetch');
        this.sourceBuilder.patchFetch({
          url: {
            literal: this.url,
          },
        });
      } else {
        this.sourceBuilder = SourceBuilder.fromUrl(
          this.url,
          this.scrapeService,
        );
      }

      await this.sourceBuilder.fetchFeedsUsingStatic();
    } catch (e: any) {
      this.errorMessage = e?.message;
    }

    this.loading = false;
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async createOrRefineFeed(refine: boolean) {
    this.sourceBuilder.patch({ tags: this.tags });

    // console.log('this.location', this.geoLocation);
    if (this.geoLocation) {
      this.sourceBuilder.patch({
        latLng: {
          lat: parseFloat(this.geoLocation.lat),
          lon: parseFloat(this.geoLocation.lon),
        },
      });
    }
    this.selectedFeedChanged.emit({
      source: this.sourceBuilder.build(),
      feed: this.selectedFeed,
      refine,
    });
  }

  async showInteractiveWebsiteModal() {
    const componentProps: InteractiveWebsiteModalComponentProps = {
      source: this.sourceBuilder.build(),
    };
    const modal = await this.modalCtrl.create({
      component: InteractiveWebsiteModalComponent,
      cssClass: 'fullscreen-modal',
      componentProps,
    });

    this.errorMessage = null;

    await modal.present();
    const result = await modal.onDidDismiss<SourceBuilder>();
    if (result.data) {
      this.sourceBuilder = null;
      this.changeRef.detectChanges();

      this.sourceBuilder = result.data;
      this.changeRef.detectChanges();
    }
  }

  receiveUrl(url: string) {
    this.url = url;
    return this.scrapeUrl();
  }

  handleCancel() {
    console.log('handleCancel');
    this.apolloAbortController.abort('user canceled');
  }

  async showTagsModal() {
    this.tags = await this.modalService.openTagModal({
      tags: this.tags || [],
    });
    this.changeRef.detectChanges();
  }

  async showLocationPickerModal() {
    this.geoLocation = await this.modalService.openSearchAddressModal();
    console.log('this.geoLocation', this.geoLocation);
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
    const legacyPathFragments = [
      '/api/tf',
      '/api/w2f',
      '/api/web-to-feed',
      '/api/w2f/rule',
      '/api/w2f/change',
      '/api/legacy/tf',
      '/api/legacy/w2f',
    ];

    if (
      legacyPathFragments.some(
        (pathFragment) => this.url.indexOf(pathFragment) > -1,
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
      this.webToFeedTransformerComponent.pickGenericFeed({
        selectors: {
          contextXPath: params['contextXPath'],
          linkXPath: params['linkXPath'],
          dateIsStartOfEvent: false,
          extendContext: GqlExtendContentOptions.None,
          paginationXPath: '',
          dateXPath: '',
        },
        hash: '',
        score: 0,
        count: 0,
      });
    }, 200);
  }

  private async patchUrlInAddressBar() {
    const url = this.router
      .createUrlTree(this.activatedRoute.snapshot.url, {
        queryParams: { url: this.url },
        relativeTo: this.activatedRoute,
      })
      .toString();
    this.location.replaceState(url);
  }

  getFilterPlugin() {
    return this.sourceBuilder.findFirstByPluginsId(
      GqlFeedlessPlugins.OrgFeedlessFilter,
    )?.execute?.params?.org_feedless_filter;
  }

  onFilterChange(params: GqlItemFilterParamsInput[]) {
    if (params.length === 0) {
      this.sourceBuilder.removePluginById(GqlFeedlessPlugins.OrgFeedlessFilter);
    } else {
      console.log('patchFilterAction');
      this.sourceBuilder.addOrUpdatePluginById(
        GqlFeedlessPlugins.OrgFeedlessFilter,
        {
          execute: {
            pluginId: GqlFeedlessPlugins.OrgFeedlessFilter,
            params: {
              org_feedless_filter: params,
            },
          },
        },
      );
    }
  }

  needsJavaScript() {
    return this.sourceBuilder.needsJavascript();
  }
}

export function tagsToString(tags: string[]): string {
  if (tags) {
    return tags.map((tag) => `#${tag}`).join(' ');
  }
}
