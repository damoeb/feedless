import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ScrapeResponse } from '../../graphql/types';
import { ProductConfig, ProductService } from '../../services/product.service';
import { fixUrl } from '../../app.module';
import { ServerSettingsService } from '../../services/server-settings.service';
import { dateFormat } from '../../services/profile.service';
import { LicenseService } from '../../services/license.service';
import { GqlLicenseQuery } from '../../../generated/graphql';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

@Component({
  selector: 'app-rss-builder-product-page',
  templateUrl: './rss-builder-product.page.html',
  styleUrls: ['./rss-builder-product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RssBuilderProductPage implements OnInit, OnDestroy {
  scrapeResponse: ScrapeResponse;
  productConfig: ProductConfig;
  url: string;
  private subscriptions: Subscription[] = [];
  showSearchBar: boolean;
  license: GqlLicenseQuery['license'];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    readonly serverSettings: ServerSettingsService,
    private readonly licenseService: LicenseService,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
  ) {
    dayjs.extend(relativeTime);
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.productService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
      this.activatedRoute.queryParams.subscribe((queryParams) => {
        if (queryParams.url) {
          this.url = queryParams.url;
        }
      }),
      this.licenseService.licenseChange.subscribe((license) => {
        this.license = license;
        this.trialEndIn = dayjs(license.trialUntil).toNow(true);
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  isTrialPeriod() {
    return (
      this.license &&
      !this.license.isLocated &&
      this.license.trialUntil > new Date().getTime()
    );
  }

  async handleQuery(url: string) {
    try {
      this.url = fixUrl(url);
      await this.router.navigate(['/builder'], {
        queryParams: {
          url: this.url,
        },
      });
    } catch (e) {
      console.warn(e);
    }
  }

  protected readonly dateFormat = dateFormat;
  trialEndIn: string;
}
