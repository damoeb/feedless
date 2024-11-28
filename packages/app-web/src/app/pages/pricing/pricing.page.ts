import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../services/app-config.service';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../graphql/types';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';
import { IonContent } from '@ionic/angular/standalone';
import { NgIf } from '@angular/common';
import { PricingComponent } from '../../components/pricing/pricing.component';

@Component({
  selector: 'app-pricing-page',
  templateUrl: './pricing.page.html',
  styleUrls: ['./pricing.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FeedlessHeaderComponent, IonContent, NgIf, PricingComponent],
  standalone: true,
})
export class PricingPage implements OnInit, OnDestroy {
  productConfig: VerticalSpecWithRoutes;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly appConfigService: AppConfigService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly activatedRoute: ActivatedRoute,
  ) {}

  async ngOnInit() {
    this.appConfigService.setPageTitle('Pricing');
    const productConfigs = await this.appConfigService.getAllAppConfigs();
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
