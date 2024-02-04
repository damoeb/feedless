import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { Embeddable } from '../embedded-website/embedded-website.component';
import {
  GqlFeedlessPlugins,
  GqlScrapeRequest,
} from '../../../generated/graphql';
import { ModalController, ToastController } from '@ionic/angular';
import { ScrapeService } from '../../services/scrape.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ScrapeResponse } from '../../graphql/types';
import { NativeOrGenericFeed } from '../../modals/transform-website-to-feed-modal/transform-website-to-feed-modal.component';
import { ProductConfig, ProductService } from '../../services/product.service';
import { FeedBuilderActionsModalComponent } from '../../modals/feed-builder-actions-modal/feed-builder-actions-modal.component';
import { fixUrl, isValidUrl } from '../../app.module';
import { ApolloAbortControllerService } from '../../services/apollo-abort-controller.service';

export type FeedWithRequest = {
  scrapeRequest: GqlScrapeRequest;
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
  scrapeRequest: GqlScrapeRequest;
  hasFeed: boolean;
  selectedFeed: NativeOrGenericFeed;
  productConfig: ProductConfig;
  private subscriptions: Subscription[] = [];
  errorMessage: string;

  @Output()
  selectedFeedChanged = new EventEmitter<FeedWithRequest>();

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    private readonly apolloAbortController: ApolloAbortControllerService,
    private readonly scrapeService: ScrapeService,
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.productService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
      this.activatedRoute.queryParams.subscribe(async (params) => {
        if (params.url?.length > 0) {
          this.url = fixUrl(params.url);
          await this.scrapeUrl();
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
    // await this.router.navigate(['/builder'], {
    //   queryParams: {
    //     url: this.url,
    //   },
    // });

    try {
      console.log(`scrape ${this.url}`);
      this.errorMessage = null;
      this.loading = true;
      this.scrapeResponse = null;
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

    this.selectedFeedChanged.emit({
      scrapeRequest: this.scrapeRequest,
      feed: this.selectedFeed,
    });
  }

  async showActionsModal() {
    const modal = await this.modalCtrl.create({
      component: FeedBuilderActionsModalComponent,
      componentProps: {
        url: this.url,
      },
    });

    await modal.present();
  }

  handleQuery(url: string) {
    this.url = url;
    return this.scrapeUrl();
  }

  handleCancel() {
    console.log('handleCancel');
    this.apolloAbortController.abort('user canceled');
  }
}
