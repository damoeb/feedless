import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
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
import { IonContent } from '@ionic/angular/standalone';

import { PricingComponent } from '../../components/pricing/pricing.component';

@Component({
  selector: 'app-pricing-page',
  templateUrl: './pricing.page.html',
  styleUrls: ['./pricing.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, PricingComponent],
  standalone: true,
})
export class PricingPage implements OnInit, OnDestroy {
  private readonly appConfigService = inject(AppConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  productConfig: VerticalSpecWithRoutes;
  private subscriptions: Subscription[] = [];

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
