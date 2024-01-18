import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { refresh } from 'ionicons/icons';
import { ScrapeService } from '../../services/scrape.service';
import {
  GqlFeedlessPlugins,
  GqlScrapeRequest,
} from '../../../generated/graphql';
import { ScrapeResponse } from '../../graphql/types';
import { Embeddable } from '../../components/embedded-website/embedded-website.component';
import { ProfileService } from '../../services/profile.service';
import {
  fixUrl,
  isValidUrl,
} from '../../pages/getting-started/getting-started.page';
import {
  NativeOrGenericFeed,
  TransformWebsiteToFeedModalComponent,
  TransformWebsiteToFeedModalComponentProps,
} from '../../modals/transform-website-to-feed-modal/transform-website-to-feed-modal.component';
import { ModalController, ToastController } from '@ionic/angular';
import { ComponentStatus } from '../../components/transform-website-to-feed/transform-website-to-feed.component';
import {
  GenerateFeedModalComponent,
  GenerateFeedModalComponentProps,
} from '../../modals/generate-feed-modal/generate-feed-modal.component';

@Component({
  selector: 'app-rss-builder-page',
  templateUrl: './rss-builder.page.html',
  styleUrls: ['./rss-builder.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RssBuilderPage implements OnInit, OnDestroy {
  url: string;
  output: 'website' | 'list' | 'article' = 'article';
  private subscriptions: Subscription[] = [];
  protected readonly refresh = refresh;

  @ViewChild('readerContent')
  readerContent: ElementRef;

  isDarkMode: boolean;

  scrapeResponse: ScrapeResponse;
  embedWebsite: Embeddable;
  loading = false;
  scrapeRequest: GqlScrapeRequest;
  hasFeed: boolean;
  selectedFeed: NativeOrGenericFeed;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly scrapeService: ScrapeService,
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
    private readonly router: Router,
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    const urlInParams = this.activatedRoute.snapshot.params.url;
    if (urlInParams?.length > 0) {
      await this.assignUrlQueryParam(urlInParams);
    } else {
      this.subscriptions.push(
        this.activatedRoute.queryParams.subscribe(async (params) => {
          if (params.url?.length > 0) {
            this.url = fixUrl(params.url);
            await this.scrapeUrl();
          }
        }),
        this.profile.watchColorScheme().subscribe((isDarkMode) => {
          this.isDarkMode = isDarkMode;
          this.changeRef.detectChanges();
        }),
      );
    }
  }

  private async assignUrlQueryParam(url: string) {
    await this.router.navigate(['/'], {
      replaceUrl: true,
      queryParams: {
        url: fixUrl(url),
      },
    });
  }

  async scrapeUrl() {
    if (!this.url) {
      return;
    }
    if (!isValidUrl(this.url)) {
      this.url = fixUrl(this.url);
    }
    this.changeRef.detectChanges();

    this.assignUrlQueryParam(this.url);
    console.log(`scrape ${this.url}`);
    this.loading = true;
    this.changeRef.detectChanges();

    this.scrapeRequest = {
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
                },
              ],
            },
          },
        },
      ],
    };
    this.scrapeResponse = await this.scrapeService.scrape(this.scrapeRequest);

    this.embedWebsite = {
      mimeType: 'text/html',
      data: this.scrapeResponse.elements[0].selector.html.data,
      url: this.url,
    };

    this.loading = false;
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
  async triggerUpdate() {
    if (!this.loading) {
      await this.scrapeUrl();
    }
  }
  async generateFeed() {
    // if (!this.hasFeed) {
    //   const toast = await this.toastCtrl
    //     .create({
    //       message: 'Pick a feed',
    //       color: 'danger',
    //       duration: 2000,
    //       position: 'bottom',
    //       cssClass: 'tiny-toast'
    //     })
    //   await toast.present()
    //   return;
    // }

    const componentProps: GenerateFeedModalComponentProps = {
      scrapeRequest: this.scrapeRequest,
      feed: this.selectedFeed,
    };
    const modal = await this.modalCtrl.create({
      component: GenerateFeedModalComponent,
      componentProps,
    });

    await modal.present();
    const response = await modal.onDidDismiss<NativeOrGenericFeed>();
  }
}
