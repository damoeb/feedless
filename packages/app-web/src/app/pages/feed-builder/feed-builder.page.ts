import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ModalController, ToastController } from '@ionic/angular';
import { ScrapeService } from '../../services/scrape.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductConfig, ProductService } from '../../services/product.service';
import {
  GenerateFeedModalComponent,
  GenerateFeedModalComponentProps
} from '../../modals/generate-feed-modal/generate-feed-modal.component';
import { ApolloAbortControllerService } from '../../services/apollo-abort-controller.service';
import { FeedWithRequest } from '../../components/feed-builder/feed-builder.component';

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
    const componentProps: GenerateFeedModalComponentProps = {
      scrapeRequest: feed.scrapeRequest,
      feed: feed.feed,
    };
    const modal = await this.modalCtrl.create({
      component: GenerateFeedModalComponent,
      componentProps,
    });

    await modal.present();

  }
}
