import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ScrapeResponse } from '../../graphql/types';
import { ProductConfig, ProductService } from '../../services/product.service';
import { ServerSettingsService } from '../../services/server-settings.service';
import { dateFormat } from '../../services/session.service';
import { LicenseService } from '../../services/license.service';
import { GqlLicenseQuery, GqlProductName } from '../../../generated/graphql';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { RepositoryService } from '../../services/repository.service';
import { AuthService } from '../../services/auth.service';

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
  license: GqlLicenseQuery['license'];

  protected readonly dateFormat = dateFormat;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly authService: AuthService,
    private readonly productService: ProductService,
    private readonly repositoryService: RepositoryService,
    private readonly licenseService: LicenseService,
    readonly serverSettings: ServerSettingsService,
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
      this.licenseService.licenseChange.subscribe((license) => {
        this.license = license;
        this.changeRef.detectChanges();
      }),
      this.activatedRoute.queryParams.subscribe((queryParams) => {
        if (queryParams.url) {
          this.url = queryParams.url;
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
  // async handleQuery(url: string) {
  //   try {
  //     this.url = fixUrl(url);
  //     await this.router.navigate(['/builder'], {
  //       queryParams: {
  //         url: this.url,
  //       },
  //     });
  //   } catch (e) {
  //     console.warn(e);
  //   }
  // }
  protected readonly GqlProductName = GqlProductName;
}
