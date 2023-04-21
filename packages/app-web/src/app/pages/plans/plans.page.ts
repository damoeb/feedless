import { Component, OnInit } from '@angular/core';
import {
  GqlFeatureName,
  GqlFeatureState,
  GqlPlan,
  GqlPlanAvailability,
} from '../../../generated/graphql';
import { Feature, PlanService } from '../../services/plan.service';

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
    {
      featureName: GqlFeatureName.FeedsMaxRefreshRate,
      title: 'Feed Refresh Rate',
      subtitle: 'minutes',
    },
    {
      featureName: GqlFeatureName.BucketsMaxCount,
      title: 'Buckets',
    },
    {
      featureName: GqlFeatureName.FeedsMaxCount,
      title: 'Feeds',
    },
    {
      featureName: GqlFeatureName.ItemsRetention,
      title: 'Item Retention',
    },
    {
      featureName: GqlFeatureName.BucketsAccessOther,
      title: 'Access Others Feeds',
    },
    {
      featureName: GqlFeatureName.Notifications,
      title: 'Notifications',
    },
    {
      featureName: GqlFeatureName.GenFeedFromWebsite,
      title: 'Feed From Website',
    },
    {
      featureName: GqlFeatureName.GenFeedFromFeed,
      title: 'Feed From Feed',
    },
    {
      featureName: GqlFeatureName.GenFeedFromPageChange,
      title: 'Feed From Page Change',
    },
    {
      featureName: GqlFeatureName.GenFeedWithPrerender,
      title: 'JavaScript Support',
    },
    {
      featureName: GqlFeatureName.GenFeedWithPuppeteerScript,
      title: 'JavaScript Eval Script',
    },
    {
      featureName: GqlFeatureName.FeedAuthentication,
      title: 'Feed Authentication',
    },
    {
      featureName: GqlFeatureName.FeedsPrivateAccess,
      title: 'Private Feeds',
    },
    {
      featureName: GqlFeatureName.BucketsPrivateAccess,
      title: 'Private Buckets',
    },
    {
      featureName: GqlFeatureName.FeedsFulltext,
      title: 'Fulltext Feeds',
    },
    {
      featureName: GqlFeatureName.ItemsInlineImages,
      title: 'Inline Images',
    },
    {
      featureName: GqlFeatureName.ItemsNoUrlShortener,
      title: 'Resolve URL Shortener',
    },
    {
      featureName: GqlFeatureName.Api,
      title: 'API',
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

  constructor(private readonly planService: PlanService) {}

  async ngOnInit() {
    const plans = await this.planService.fetchPlans();

    const featureGroups: FeatureGroup<GqlFeatureName>[] = [
      {
        groupLabel: 'General',
        features: [
          GqlFeatureName.RateLimit,
          GqlFeatureName.FeedsMaxRefreshRate,
          GqlFeatureName.BucketsMaxCount,
          GqlFeatureName.FeedsMaxCount,
          GqlFeatureName.ItemsRetention,
        ],
      },
      {
        groupLabel: 'Generate Feeds',
        features: [
          GqlFeatureName.GenFeedFromWebsite,
          GqlFeatureName.GenFeedFromFeed,
          GqlFeatureName.GenFeedFromPageChange,
          GqlFeatureName.GenFeedWithPrerender,
          GqlFeatureName.GenFeedWithPuppeteerScript,

          GqlFeatureName.FeedAuthentication,
          GqlFeatureName.FeedsPrivateAccess,
          GqlFeatureName.BucketsPrivateAccess,
          GqlFeatureName.FeedsFulltext,
          GqlFeatureName.ItemsInlineImages,
          GqlFeatureName.ItemsNoUrlShortener,
        ],
      },
      {
        groupLabel: 'Integration',
        features: [
          GqlFeatureName.Api,
          GqlFeatureName.ItemEmailForward,
          GqlFeatureName.ItemWebhookForward,
        ],
      },
    ];

    // const getFeatureConfig = (featureName: GqlFeatureName): FeatureConfig =>
    //   features.find((feature) => feature.featureName === featureName);
    //
    // const getPlanFeatureGroups = (planName: GqlPlanName): PlanFeatureGroup[] =>
    //   featureGroups.map(featureGroup => ({
    //     label: featureGroup.groupLabel,
    //     planFeatures: featureGroup.featureNames.map((featureName) => {
    //       const featureConfig = getFeatureConfig(featureName);
    //       return { config: featureConfig, value: featureConfig.plans[planName] };
    //     })
    //   }));

    // const toFeatureGroups = (features: Array<PlanFeature>): PlanFeatureGroup[] =>
    //     featureGroups.map(featureGroup => ({
    //       label: featureGroup.groupLabel,
    //       planFeatures: []
    //     }));

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
    features: Feature[]
  ): FeatureGroup<Feature> {
    return {
      groupLabel: group.groupLabel,
      features: group.features.map((featureName) =>
        features.find((feature) => feature.name === featureName)
      ),
    };
  }
}
