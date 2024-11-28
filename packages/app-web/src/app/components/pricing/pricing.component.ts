import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { filter } from 'lodash-es';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { FeatureGroup, Product } from '../../graphql/types';
import {
  GqlFeatureName,
  GqlPricedProduct,
  GqlVertical,
  GqlRecurringPaymentInterval,
} from '../../../generated/graphql';
import {
  StringFeature,
  StringFeatureGroup,
  PlanColumnComponent,
} from '../plan-column/plan-column.component';
import { FeatureService } from '../../services/feature.service';

import {
  IonSegment,
  IonSegmentButton,
  IonLabel,
  IonRow,
  IonCol,
  IonNote,
  IonButton,
} from '@ionic/angular/standalone';

type TargetGroup = 'organization' | 'individual' | 'other';
type ServiceFlavor = 'selfHosting' | 'saas';
type PaymentInterval = GqlRecurringPaymentInterval;

type ProductWithFeatureGroups = Product & {
  stringifiedFeatureGroups: StringFeatureGroup[];
  featureGroups: FeatureGroup[];
};

@Component({
  selector: 'app-pricing',
  templateUrl: './pricing.component.html',
  styleUrls: ['./pricing.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonSegment,
    FormsModule,
    ReactiveFormsModule,
    IonSegmentButton,
    IonLabel,
    PlanColumnComponent,
    IonRow,
    IonCol,
    IonNote,
    IonButton
],
  standalone: true,
})
export class PricingComponent implements OnInit {
  private readonly featureService = inject(FeatureService);
  private readonly productService = inject(ProductService);
  private readonly changeRef = inject(ChangeDetectorRef);

  targetGroupFc = new FormControl<TargetGroup>('individual');
  paymentIntervalFc = new FormControl<PaymentInterval>(
    GqlRecurringPaymentInterval.Yearly,
  );
  serviceFlavorFc = new FormControl<ServiceFlavor>('saas');
  serviceFlavorSelf: ServiceFlavor = 'selfHosting';
  serviceFlavorCloud: ServiceFlavor = 'saas';
  targetGroupOrganization: TargetGroup = 'organization';
  targetGroupIndividual: TargetGroup = 'individual';
  targetGroupOther: TargetGroup = 'other';
  paymentIntervalMonthly: PaymentInterval = GqlRecurringPaymentInterval.Monthly;
  paymentIntervalYearly: PaymentInterval = GqlRecurringPaymentInterval.Yearly;
  private products: ProductWithFeatureGroups[];

  @Input({ required: true })
  vertical: GqlVertical;

  @Input()
  serviceFlavor: ServiceFlavor;

  @Input()
  hideServiceFlavor: boolean;

  @Output()
  selectionChange: EventEmitter<Product> = new EventEmitter<Product>();

  async ngOnInit() {
    if (this.serviceFlavor) {
      this.serviceFlavorFc.setValue(this.serviceFlavor);
    }
    const products = await this.productService.listProducts({
      category: this.vertical,
    });

    this.products = await Promise.all(
      products.map<Promise<ProductWithFeatureGroups>>(async (p) => {
        const featureGroups = p.featureGroupId
          ? await this.featureService.findAll(
              { id: { eq: p.featureGroupId } },
              true,
            )
          : [];
        return {
          ...p,
          stringifiedFeatureGroups:
            featureGroups.length > 0
              ? [await this.stringifyFeatureGroup(featureGroups)]
              : [],
          featureGroups: featureGroups.length > 0 ? featureGroups : [],
        };
      }),
    );
    this.changeRef.detectChanges();
  }

  filteredProducts(): ProductWithFeatureGroups[] {
    if (!this.products) {
      return [];
    }
    return filter<ProductWithFeatureGroups>(this.products, this.filterParams());
  }

  private filterParams() {
    if (this.serviceFlavorFc.value === 'saas') {
      return {
        isCloud: true,
      };
    }
    if (this.targetGroupFc.value === 'individual') {
      return {
        individual: true,
      };
    }
    if (this.targetGroupFc.value === 'organization') {
      return {
        enterprise: true,
      };
    }
    if (this.targetGroupFc.value === 'other') {
      return {
        other: true,
      };
    }
  }

  filteredPrices(prices: GqlPricedProduct[]): GqlPricedProduct[] {
    return filter<GqlPricedProduct>(prices)
      .filter((price) => price.price >= 0)
      .filter(
        (price) => price.recurringInterval === this.paymentIntervalFc.value,
      );
  }

  private async stringifyFeatureGroup(
    featureGroups: FeatureGroup[],
  ): Promise<StringFeatureGroup> {
    return {
      groupLabel: 'Features',
      features:
        featureGroups[0].features
          ?.filter((feature) => this.localise(feature.name))
          ?.map<StringFeature>((feature) => ({
            title: this.localise(feature.name),
            valueBool: feature.value.boolVal,
            valueHtml:
              feature.value.numVal != null
                ? feature.value.numVal.value == -1
                  ? 'Infinite'
                  : `${feature.value.numVal.value}`
                : null,
            subtitle: '',
          })) || [],
    };
  }

  checkout(product: Product) {
    this.selectionChange.emit(product);
  }

  getProductActionLabel(product: ProductWithFeatureGroups) {
    if (product.isCloud) {
      const features = product.featureGroups.flatMap((fg) => fg.features);
      const canActivate = features
        .filter((feature) => feature.name === GqlFeatureName.CanActivatePlan)
        .some((feature) => feature.value.boolVal.value === true);
      if (canActivate) {
        return 'Subscribe';
      } else {
        return 'Notify me';
      }
    } else {
      return 'Buy';
    }
  }

  formatPrice(price: number) {
    return price.toFixed(2);
  }

  private localise(feature: GqlFeatureName): string {
    switch (feature) {
      case GqlFeatureName.Plugins:
        return 'Plugins';
      case GqlFeatureName.PublicRepository:
        return 'Public Listing';
    }
  }
}
