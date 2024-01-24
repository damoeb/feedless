import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { Embeddable } from '../../components/embedded-website/embedded-website.component';
import { GqlFeedlessPlugins, GqlScrapeRequest, GqlScrapeRequestInput } from '../../../generated/graphql';
import { ModalController, ToastController } from '@ionic/angular';
import { ScrapeService } from '../../services/scrape.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ScrapeResponse } from '../../graphql/types';
import { NativeOrGenericFeed } from '../../modals/transform-website-to-feed-modal/transform-website-to-feed-modal.component';
import { ProductConfig, ProductService } from '../../services/product.service';
import {
  GenerateFeedModalComponent,
  GenerateFeedModalComponentProps
} from '../../modals/generate-feed-modal/generate-feed-modal.component';
import { FeedBuilderActionsModalComponent } from '../../components/feed-builder-actions-modal/feed-builder-actions-modal.component';
import { fixUrl, isValidUrl } from '../../app.module';


@Component({
  selector: 'app-feed-builder',
  templateUrl: './feed-builder.page.html',
  styleUrls: ['./feed-builder.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FeedBuilderPage implements OnInit, OnDestroy {
  url: string;
  scrapeResponse: ScrapeResponse;
  embedWebsite: Embeddable;
  loading = false;
  scrapeRequest: GqlScrapeRequest;
  hasFeed: boolean;
  selectedFeed: NativeOrGenericFeed;
  productConfig: ProductConfig;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    private readonly scrapeService: ScrapeService,
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef
  ) {
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.productService.getActiveProductConfigChange().subscribe(productConfig => {
        this.productConfig = productConfig;
        this.changeRef.detectChanges();
      }),
      this.activatedRoute.queryParams.subscribe(async (params) => {
        if (params.url?.length > 0) {
          this.url = fixUrl(params.url);
          await this.scrapeUrl();
        }
      })
    );
  }

  async scrapeUrl() {
    if (!this.url) {
      return;
    }
    if (!isValidUrl(this.url)) {
      this.url = fixUrl(this.url);
    }
    this.changeRef.detectChanges();

    console.log(`scrape ${this.url}`);
    this.loading = true;
    this.changeRef.detectChanges();

    this.scrapeRequest = {
      page: {
        url: this.url
      },
      emit: [
        {
          selectorBased: {
            xpath: {
              value: '/'
            },
            expose: {
              transformers: [
                {
                  pluginId: GqlFeedlessPlugins.OrgFeedlessFeeds
                }
              ]
            }
          }
        }
      ]
    };
    this.scrapeResponse = await this.scrapeService.scrape(this.scrapeRequest);

    this.embedWebsite = {
      mimeType: 'text/html',
      data: this.scrapeResponse.elements[0].selector.html.data,
      url: this.url
    };

    this.loading = false;
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async generateFeed() {
    if (!this.hasFeed) {
      const toast = await this.toastCtrl
        .create({
          message: 'Pick a feed',
          color: 'danger',
          duration: 2000,
          position: 'bottom',
          cssClass: 'tiny-toast'
        });
      await toast.present();
      return;
    }

    const componentProps: GenerateFeedModalComponentProps = {
      scrapeRequest: this.scrapeRequest,
      feed: this.selectedFeed
    };
    const modal = await this.modalCtrl.create({
      component: GenerateFeedModalComponent,
      componentProps
    });

    await modal.present();
    const response = await modal.onDidDismiss<NativeOrGenericFeed>();
  }

  async showActionsModal() {
    const modal = await this.modalCtrl.create({
      component: FeedBuilderActionsModalComponent,
      componentProps: {
        url: this.url
      }
    });

    await modal.present();

  }

  handleQuery(url: string) {
    this.url = url;
    return this.scrapeUrl();
  }
}
