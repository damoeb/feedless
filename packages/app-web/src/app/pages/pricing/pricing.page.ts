import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter } from 'lodash-es';
import { AppConfigService, ProductConfig } from '../../services/app-config.service';
import { FormControl } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { FeatureGroup, Product } from '../../graphql/types';
import { GqlFeatureName, GqlPricedProduct } from '../../../generated/graphql';
import { ShoppingBasketService } from '../../services/shopping-basekt.service';
import { StringFeature, StringFeatureGroup } from '../../components/plan-column/plan-column.component';
import { FeatureService } from '../../services/feature.service';

type TargetGroup = 'organization' | 'individual' | 'other';
type ServiceFlavor = 'self' | 'cloud';

type ProductWithFeatureGroups = Product & { stringifiedFeatureGroups: StringFeatureGroup[]; featureGroups: FeatureGroup[] };

@Component({
  selector: 'app-checkout-page',
  templateUrl: './pricing.page.html',
  styleUrls: ['./pricing.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PricingPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  targetGroup = new FormControl<TargetGroup>('individual');
  serviceFlavor = new FormControl<ServiceFlavor>('self');
  serviceFlavorSelf: ServiceFlavor = 'self';
  serviceFlavorCloud: ServiceFlavor = 'cloud';
  targetGroupOrganization: TargetGroup = 'organization';
  targetGroupIndividual: TargetGroup = 'individual';
  targetGroupOther: TargetGroup = 'other';
  private products: ProductWithFeatureGroups[];
  productCategory: ProductConfig;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly appConfigService: AppConfigService,
    private readonly featureService: FeatureService,
    private readonly shoppingBasketService: ShoppingBasketService,
    private readonly productService: ProductService,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef
  ) {
  }

  async ngOnInit() {
    const productConfigs = await this.appConfigService.getProductConfigs();
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.productId) {
          const product = productConfigs.find((p) => p.id === params.productId);
          this.productCategory = product;
          const products = await this.productService.listProducts({
            category: product.product
          });

          this.products = await Promise.all(products.map<Promise<ProductWithFeatureGroups>>(async p => {
            const featureGroups = p.featureGroupId ? await this.featureService.findAll({ id: { equals: p.featureGroupId } }, true) : [];
            return {
                ...p,
                stringifiedFeatureGroups: featureGroups.length > 0 ? [await this.stringifyFeatureGroup(featureGroups)] : [],
                featureGroups: featureGroups.length > 0 ? featureGroups : [],
              };
            }
          ));
          this.changeRef.detectChanges();
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  filteredProducts(): ProductWithFeatureGroups[] {
    if (!this.products) {
      return [];
    }
    return filter(this.products, {
      isCloud: this.serviceFlavor.value === 'cloud'
    }).filter(
      (product) =>
        filter<GqlPricedProduct>(product.prices, this.filterParams()).length >
        0
    );
  }

  private filterParams() {
    if (this.serviceFlavor.value === 'cloud') {
      return {};
    }
    if (this.targetGroup.value === 'individual') {
      return {
        individual: true
      };
    }
    if (this.targetGroup.value === 'organization') {
      return {
        enterprise: true
      };
    }
    if (this.targetGroup.value === 'other') {
      return {
        other: true
      };
    }
  }

  filteredPrices(prices: GqlPricedProduct[]): GqlPricedProduct[] {
    return filter<GqlPricedProduct>(prices, this.filterParams()).filter(
      (price) => price.price >= 0
    );
  }

  private async stringifyFeatureGroup(featureGroups: FeatureGroup[]): Promise<StringFeatureGroup> {
    return {
      groupLabel: 'Features',
      features:
        featureGroups[0].features?.map<StringFeature>((feature) => ({
          title: feature.name,
          valueBool: feature.value.boolVal,
          valueHtml:
            feature.value.numVal != null
              ? (feature.value.numVal.value == -1 ? 'Infinite' : `${feature.value.numVal.value}`)
              : null,
          subtitle: ''
        })) || []
    };
  }

  checkout(product: Product) {
    return this.router.navigateByUrl(this.router.createUrlTree([`/checkout/${product.id}`], { queryParamsHandling: 'merge' }));
  }

  getProductActionLabel(product: ProductWithFeatureGroups) {
    if (product.isCloud) {
      const features = product.featureGroups.flatMap(fg => fg.features);
      const canActivate = features.filter(feature => feature.name === GqlFeatureName.CanActivatePlan).some(feature => feature.value.boolVal.value === true)
      if (canActivate) {
        return 'Subscribe'
      } else {
        return 'Notify me'
      }
    } else {
      return 'Buy';
    }
  }
}
