import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { GqlFeatureName, GqlFeatureState, GqlPlan, GqlPlanAvailability } from '../../../generated/graphql';
import { PlanService } from '../../services/plan.service';
import { Feature } from '../../graphql/types';

interface PlanAction {
  label: string;
  color?: string;
  redirectTo: string;
}

interface FeatureGroup<T> {
  groupLabel: string;
  features: T[];
}

type UIPlan = Partial<GqlPlan> & {
  featureGroups: FeatureGroup<Feature>[];
  action: PlanAction;
  color?: string;
};

interface FeatureLabel {
  featureName: GqlFeatureName;
  title: string;
  subtitle?: string;
}

@Component({
  selector: 'app-plans',
  templateUrl: './plans.page.html',
  styleUrls: ['./plans.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlansPage implements OnInit {
  plans: UIPlan[];
  scrollTop = 0;
  private labels: FeatureLabel[] = [
    {
      featureName: GqlFeatureName.RateLimit,
      title: 'Rate Limit',
      subtitle: 'max requests/min',
    },
    // {
    //   featureName: GqlFeatureName.FeedsMaxRefreshRate,
    //   title: 'Feed Refresh Rate',
    //   subtitle: 'minutes',
    // },
    {
      featureName: GqlFeatureName.Api,
      title: 'API',
    },
    {
      featureName: GqlFeatureName.PublicScrapeSource,
      title: 'Public Subscriptions',
    },
    {
      featureName: GqlFeatureName.ScrapeSourceRetentionMaxItems,
      title: 'Items per Subscription',
    },
    {
      featureName: GqlFeatureName.ScrapeSourceMaxCountTotal,
      title: 'Subscriptions',
    },
    {
      featureName: GqlFeatureName.Plugins,
      title: 'Plugins Support',
      subtitle: 'e.g. Fulltext, Privacy'
    },
    {
      featureName: GqlFeatureName.ItemEmailForward,
      title: 'Email Forwards',
    },
    {
      featureName: GqlFeatureName.ItemWebhookForward,
      title: 'Webhooks',
    },
  ];

  constructor(private readonly planService: PlanService,
              private readonly changeRef: ChangeDetectorRef) {}

  async ngOnInit() {
    const plans = await this.planService.fetchPlans();

    const featureGroups: FeatureGroup<GqlFeatureName>[] = [
      {
        groupLabel: 'General',
        features: [
          GqlFeatureName.RateLimit,
          GqlFeatureName.Plugins,
          GqlFeatureName.PublicScrapeSource,
        ],
      },
      {
        groupLabel: 'Storage',
        features: [
          GqlFeatureName.ScrapeSourceMaxCountTotal,
          GqlFeatureName.ScrapeSourceRetentionMaxItems,
        ],
      },
      {
        groupLabel: 'Integration',
        features: [
          GqlFeatureName.ItemWebhookForward,
          GqlFeatureName.Api,
          GqlFeatureName.ItemEmailForward,
        ],
      },
    ];

    const toFeatureGroups = (features: Feature[]): FeatureGroup<Feature>[] =>
      featureGroups.map((group) => this.toFeatureGroup(group, features));

    this.plans = plans.map<UIPlan>((plan) => ({
      name: plan.name,
      costs: plan.costs,
      color: plan.isPrimary
        ? 'var(--ion-color-primary)'
        : 'var(--ion-color-dark)',
      action: this.getAction(plan.availability),
      featureGroups: toFeatureGroups(plan.features),
    }));
    console.log(this.plans);
    this.changeRef.detectChanges();
  }

  isFalse(feature: Feature): boolean {
    return this.isBoolean(feature) && !feature.value.boolVal.value;
  }

  isTrue(feature: Feature): boolean {
    return this.isBoolean(feature) && feature.value.boolVal.value;
  }

  isBoolean(feature: Feature): boolean {
    return !!feature.value.boolVal;
  }

  formatPrice(price: number): string {
    return price.toFixed(2);
  }

  isBeta(feature: Feature): boolean {
    return feature.state === GqlFeatureState.Beta;
  }

  isExperimental(feature: Feature): boolean {
    return feature.state === GqlFeatureState.Experimental;
  }

  getFeatureTitle(feature: Feature): string {
    console.log(feature.name)
    return this.labels.find((label) => label.featureName === feature.name)
      .title;
  }

  getFeatureSubTitle(feature: Feature): string {
    return this.labels.find((label) => label.featureName === feature.name)
      .subtitle;
  }

  getFeatureValue(feature: Feature): number | boolean {
    return feature.value.boolVal
      ? feature.value.boolVal.value
      : feature.value.numVal.value;
  }

  onScroll(event: Event) {
    this.scrollTop = (event.target as any).scrollTop;
  }

  private getAction(availability: GqlPlanAvailability): PlanAction {
    switch (availability) {
      case GqlPlanAvailability.Available:
        return {
          label: 'Join Public Beta',
          color: 'primary',
          redirectTo: '/signup',
        };
      case GqlPlanAvailability.ByRequest:
        return {
          label: 'Contact Us',
          color: 'dark',
          redirectTo: '/contact',
        };
    }
    return {
      label: 'Join Public Beta',
      color: 'primary',
      redirectTo: '/signup',
    };
  }

  private toFeatureGroup(
    group: FeatureGroup<GqlFeatureName>,
    features: Feature[],
  ): FeatureGroup<Feature> {
    return {
      groupLabel: group.groupLabel,
      features: group.features.map((featureName) =>
        features.find((feature) => feature.name === featureName),
      ),
    };
  }
}
