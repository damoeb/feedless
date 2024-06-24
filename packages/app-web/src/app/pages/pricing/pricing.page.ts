import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { AppConfigService, ProductConfig } from '../../services/app-config.service';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../graphql/types';

@Component({
  selector: 'app-pricing-page',
  templateUrl: './pricing.page.html',
  styleUrls: ['./pricing.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PricingPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  productCategory: ProductConfig;

  constructor(private readonly appConfigService: AppConfigService,
              private readonly changeRef: ChangeDetectorRef,
              private readonly router: Router,
              private readonly activatedRoute: ActivatedRoute) {
  }

  async ngOnInit() {
    const productConfigs = await this.appConfigService.getProductConfigs();
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.productId) {
          this.productCategory = productConfigs.find((p) => p.id === params.productId);
          this.changeRef.detectChanges();
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  checkout(product: Product) {
    return this.router.navigateByUrl(this.router.createUrlTree([`/checkout/${product.id}`], { queryParamsHandling: 'merge' }));
  }
}
