import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  AppConfigService,
  ProductConfig,
} from '../../services/app-config.service';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../graphql/types';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-pricing-page',
  templateUrl: './pricing.page.html',
  styleUrls: ['./pricing.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PricingPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  productConfig: ProductConfig;

  constructor(
    private readonly appConfigService: AppConfigService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly titleService: Title,
    private readonly activatedRoute: ActivatedRoute,
  ) {}

  async ngOnInit() {
    this.titleService.setTitle('Pricing');
    const productConfigs = await this.appConfigService.getProductConfigs();
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.productId) {
          this.productConfig = productConfigs.find(
            (p) => p.id === params.productId,
          );
          this.changeRef.detectChanges();
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  checkout(product: Product) {
    return this.router.navigateByUrl(
      this.router.createUrlTree([`/checkout/${product.id}`], {
        queryParamsHandling: 'merge',
      }),
    );
  }
}
