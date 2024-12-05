import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  input,
  OnDestroy,
  OnInit,
  output,
  viewChild,
} from '@angular/core';
import { Location } from '@angular/common';
import { Subscription } from 'rxjs';
import {
  GqlExtendContentOptions,
  GqlFeedlessPlugins,
  GqlItemFilterParamsInput,
  GqlRemoteNativeFeed,
  GqlSourceInput,
  GqlTransientGenericFeed,
} from '../../../generated/graphql';
import {
  AlertController,
  IonAccordion,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonProgressBar,
  IonToolbar,
  ModalController,
  ToastController,
} from '@ionic/angular/standalone';
import { ScrapeService } from '../../services/scrape.service';
import { ActivatedRoute, Router } from '@angular/router';
import { RepositoryWithFrequency, ScrapeResponse } from '../../graphql/types';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../services/app-config.service';
import {
  InteractiveWebsiteModalComponent,
  InteractiveWebsiteModalComponentProps,
} from '../../modals/interactive-website-modal/interactive-website-modal.component';
import { fixUrl, isValidUrl } from '../../app.module';
import { ApolloAbortControllerService } from '../../services/apollo-abort-controller.service';
import { ModalService } from '../../services/modal.service';
import { TransformWebsiteToFeedComponent } from '../transform-website-to-feed/transform-website-to-feed.component';
import { RepositoryService } from '../../services/repository.service';
import { SourceBuilder } from '../interactive-website/source-builder';
import { addIcons } from 'ionicons';
import {
  checkmarkOutline,
  logoJavascript,
  settingsOutline,
} from 'ionicons/icons';
import { SearchbarComponent } from '../../elements/searchbar/searchbar.component';
import { FilterItemsAccordionComponent } from '../filter-items-accordion/filter-items-accordion.component';
import { ServerConfigService } from '../../services/server-config.service';
import { TagsModalModule } from '../../modals/tags-modal/tags-modal.module';
import { SearchAddressModalModule } from '../../modals/search-address-modal/search-address-modal.module';
import { InteractiveWebsiteModalModule } from '../../modals/interactive-website-modal/interactive-website-modal.module';
import {
  standaloneV2FeedTransformRoute,
  standaloneV2WebToFeedRoute,
} from '../../router-utils';
import { LatLon } from '../../types';

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
  repository: RepositoryWithFrequency;
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
  imports: [
    IonToolbar,
    SearchbarComponent,
    IonProgressBar,
    IonList,
    IonItem,
    TransformWebsiteToFeedComponent,
    IonLabel,
    IonIcon,
    IonAccordion,
    IonNote,
    FilterItemsAccordionComponent,
    TagsModalModule,
    SearchAddressModalModule,
    InteractiveWebsiteModalModule,
  ],
  standalone: true,
})
export class FeedBuilderComponent implements OnInit, OnDestroy {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly appConfigService = inject(AppConfigService);
  private readonly apolloAbortController = inject(ApolloAbortControllerService);
  private readonly scrapeService = inject(ScrapeService);
  private readonly modalService = inject(ModalService);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly modalCtrl = inject(ModalController);
  private readonly alertCtrl = inject(AlertController);
  private readonly repositoryService = inject(RepositoryService);
  private readonly serverConfigService = inject(ServerConfigService);
  private readonly toastCtrl = inject(ToastController);
  private readonly changeRef = inject(ChangeDetectorRef);

  url: string;

  loading = false;

  readonly webToFeedTransformerComponent =
    viewChild<TransformWebsiteToFeedComponent>('webToFeedTransformer');

  readonly submitButtonText = input('Create Feed');

  readonly source = input<GqlSourceInput>();

  selectedFeed: NativeOrGenericFeed;
  productConfig: VerticalSpecWithRoutes;
  private subscriptions: Subscription[] = [];
  errorMessage: string;

  readonly hideSearchBar = input(false);

  readonly standaloneFeedMode = input(false);

  readonly hideCustomizeFeed = input(false);

  readonly selectedFeedChanged = output<FeedWithRequest>();

  readonly selectedRepositoryChanged = output<RepositoryWithFrequency>();

  protected tags: string[] = [];
  protected geoLocation: LatLon;
  // protected repositories: Repository[] = [];
  hasValidFeed: boolean;
  protected sourceBuilder: SourceBuilder;

  constructor() {
    addIcons({ logoJavascript, settingsOutline, checkmarkOutline });
  }

  async ngOnInit() {
    const source = this.source();
    if (source) {
      console.log('this.source', source);
      this.tags = source.tags;
      this.geoLocation = source.latLng;
      this.sourceBuilder = SourceBuilder.fromSource(source, this.scrapeService);
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

    // await this.fetchFeeds();
  }

  // remix(repository: Repository) {
  //   this.selectedRepositoryChanged.emit(repository);
  // }

  // private async fetchFeeds() {
  //   const page = 0;
  //   const repositories = await this.repositoryService.listRepositories({
  //     cursor: {
  //       page,
  //     },
  //     where: {
  //       product: {
  //         eq: GqlVertical.Feedless,
  //       },
  //     },
  //   });
  //   this.repositories.push(...repositories);
  //   this.changeRef.detectChanges();
  // }
  //
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
          lat: this.geoLocation.lat,
          lon: this.geoLocation.lon,
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
        header: 'Standalone URL detected',
        backdropDismiss: false,
        message:
          'This URL looks like an standalone RSS-proxy url, do you want to convert it?',
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
            handler: () => this.convertStandaloneRssProxyUrl(),
          },
        ],
      });

      await alert.present();
    }
  }

  private async convertStandaloneRssProxyUrl() {
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
      this.webToFeedTransformerComponent().pickGenericFeed({
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

  async createFeedUrl() {
    const alert = await this.alertCtrl.create({
      header: 'Standalone URL',
      backdropDismiss: false,
      message: 'You can use this URL directly in your feed reader',
      inputs: [
        {
          name: 'feedUrl',
          type: 'textarea',
          value: this.createStandaloneFeedUrl(),
          attributes: {
            readonly: true,
          },
        },
      ],
      buttons: [
        {
          role: 'ok',
          text: 'Copy URL',
          handler: async () => {
            await navigator.clipboard.writeText(alert.inputs[0].value);
            const toast = await this.toastCtrl.create({
              message: 'Copied',
              duration: 2000,
              color: 'success',
            });

            await toast.present();
          },
        },
        {
          role: 'cancel',
          text: 'Close',
        },
      ],
    });
    await alert.present();
  }

  private createStandaloneFeedUrl() {
    const baseUrl = this.serverConfigService.apiUrl + '/';
    const q = JSON.stringify(this.getFilterPlugin());
    if (this.selectedFeed.genericFeed) {
      const gf = this.selectedFeed.genericFeed;
      return (
        baseUrl +
        standaloneV2WebToFeedRoute({
          url: this.url,
          link: gf.selectors.linkXPath,
          context: gf.selectors.contextXPath,
          date: gf.selectors.dateXPath,
          dateIsEvent: gf.selectors.dateIsStartOfEvent,
          q,
          out: 'atom',
        })
      );
    } else {
      return (
        baseUrl +
        standaloneV2FeedTransformRoute({
          url: this.selectedFeed.nativeFeed.feedUrl,
          q,
          out: 'atom',
        })
      );
    }
  }
}

export function tagsToString(tags: string[]): string {
  if (tags) {
    return tags.map((tag) => `#${tag}`).join(' ');
  }
}
