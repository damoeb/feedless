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
import {
  ProductConfig,
  AppConfigService,
} from '../../services/app-config.service';
import { ServerConfigService } from '../../services/server-config.service';
import { dateFormat } from '../../services/session.service';
import { LicenseService } from '../../services/license.service';
import {
  GqlLicenseQuery,
  GqlProductCategory,
} from '../../../generated/graphql';

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
  protected embedded: boolean = false;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly appConfigService: AppConfigService,
    private readonly licenseService: LicenseService,
    readonly serverConfig: ServerConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.appConfigService
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
          this.changeRef.detectChanges();
        }
        if (queryParams.embedded) {
          this.embedded = true;
          this.changeRef.detectChanges();
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
  protected readonly GqlProductName = GqlProductCategory;
}
