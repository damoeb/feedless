import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ModalController, ToastController } from '@ionic/angular';
import { ScrapeService } from '../../services/scrape.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductConfig, ProductService } from '../../services/product.service';
import { GenerateFeedModalComponentProps, getScrapeRequest } from '../../modals/generate-feed-modal/generate-feed-modal.component';
import { ApolloAbortControllerService } from '../../services/apollo-abort-controller.service';
import { FeedWithRequest } from '../../components/feed-builder/feed-builder.component';
import { ModalService } from '../../services/modal.service';
import { NativeOrGenericFeed } from '../../modals/transform-website-to-feed-modal/transform-website-to-feed-modal.component';
import { GqlScrapeRequest } from '../../../generated/graphql';

@Component({
  selector: 'app-feed-builder-page',
  templateUrl: './feed-builder.page.html',
  styleUrls: ['./feed-builder.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedBuilderPage implements OnInit, OnDestroy {
  loading = false;
  productConfig: ProductConfig;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    private readonly apolloAbortController: ApolloAbortControllerService,
    private readonly scrapeService: ScrapeService,
    private readonly modalCtrl: ModalController,
    private readonly modalService: ModalService,
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
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async handleFeed(feed: FeedWithRequest) {
    const { title, description } = this.getFeedData(feed.feed, feed.scrapeRequest.page.url)
    const componentProps: GenerateFeedModalComponentProps = {
      subscription: {
        title,
        description,
        plugins: [],
        sources: [getScrapeRequest(feed.feed, feed.scrapeRequest as GqlScrapeRequest)]
      } as any
    };
    await this.modalService.openFeedMetaEditor(componentProps);
  }

  private getFeedData(feed: NativeOrGenericFeed, urlString: string) {
    if (feed.nativeFeed) {
      return {
        title: feed.nativeFeed.title,
        description: `Source: ${feed.nativeFeed.feedUrl}`,
      };
    } else {
      const url = new URL(urlString);
      return {
        title: `Feed from ${url.host}`,
        description: `Source: ${url}`,
      };
    }
  }

}
