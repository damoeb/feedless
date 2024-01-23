import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ScrapeService } from '../../services/scrape.service';
import { ScrapeResponse } from '../../graphql/types';
import { ProfileService } from '../../services/profile.service';
import { ModalController, ToastController } from '@ionic/angular';
import { ProductConfig, ProductService } from '../../services/product.service';
import { fixUrl } from '../../pages/about/about.page';
import { FormControl, Validators } from '@angular/forms';

@Component({
  selector: 'app-rss-builder-page',
  templateUrl: './rss-builder.page.html',
  styleUrls: ['./rss-builder.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RssBuilderPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  isDarkMode: boolean;

  scrapeResponse: ScrapeResponse;
  config: ProductConfig;

  url: string;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    private readonly scrapeService: ScrapeService,
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
    private readonly router: Router,
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.config = this.productService.getProductConfig();
    this.subscriptions.push(
      this.activatedRoute.queryParams.subscribe(queryParams => {
        if (queryParams.url) {
          this.url = queryParams.url
        }
      }),
      this.profile.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async triggerUpdate(url: string) {
    console.log('triggerUpdate')
    try {
      this.url = fixUrl(url)
      await this.router.navigate(['/builder'], {
        queryParams: {
          url: this.url,
        },
      });
    } catch (e) {
      console.warn(e)
    }
  }
}
