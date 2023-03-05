import { Component, OnInit } from '@angular/core';
import {
  GqlFeatureName,
  GqlFeatureState,
  GqlPlan,
  GqlPlanName,
} from '../../../generated/graphql';

interface PlanFeature {
  config: FeatureConfig;
  value: boolean | number;
}

type PlanFeatureConfig = {
  [k in GqlPlanName]: boolean | number;
};

interface FeatureConfig {
  featureName: GqlFeatureName;
  title: string;
  subtitle?: string;
  state: GqlFeatureState;
  plans: PlanFeatureConfig;
}

interface PlanAction {
  label: string;
  color?: string;
  redirectTo: string;
}

interface FeatureGroup {
  groupLabel: string;
  featureNames: GqlFeatureName[];
}

interface PlanFeatureGroup {
  label: string;
  planFeatures: PlanFeature[];
}

@Component({
  selector: 'app-plans',
  templateUrl: './plans.page.html',
  styleUrls: ['./plans.page.scss'],
})
export class PlansPage implements OnInit {
  plans: (GqlPlan & {
    featureGroups: PlanFeatureGroup[];
    action: PlanAction;
    color?: string;
  })[];
  constructor() {}

  ngOnInit() {
    const features: FeatureConfig[] = [
      {
        featureName: GqlFeatureName.RateLimit,
        title: 'Rate Limit',
        subtitle: 'max requests/min',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: 40,
          [GqlPlanName.Basic]: 120,
        },
      },
      {
        featureName: GqlFeatureName.FeedsMaxRefreshRate,
        title: 'Feed Refresh Rate',
        subtitle: 'minutes',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: 120,
          [GqlPlanName.Basic]: 10,
        },
      },
      {
        featureName: GqlFeatureName.BucketsMaxCount,
        title: 'Buckets',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: 3,
          [GqlPlanName.Basic]: 100,
        },
      },
      {
        featureName: GqlFeatureName.FeedsMaxCount,
        title: 'Feeds',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: 30,
          [GqlPlanName.Basic]: 1000,
        },
      },
      {
        featureName: GqlFeatureName.ItemsRetention,
        title: 'Item Retention',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: 400,
          [GqlPlanName.Basic]: 10000,
        },
      },
      {
        featureName: GqlFeatureName.BucketsAccessOther,
        title: 'Access Others Feeds',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: true,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.Notifications,
        title: 'Notifications',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: true,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.GenFeedFromWebsite,
        title: 'Feed From Website',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: true,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.GenFeedFromFeed,
        title: 'Feed From Feed',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: true,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.GenFeedFromPageChange,
        title: 'Feed From Page Change',
        state: GqlFeatureState.Experimental,
        plans: {
          [GqlPlanName.Free]: true,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.GenFeedWithPrerender,
        title: 'JavaScript Support',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: true,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.GenFeedWithPuppeteerScript,
        title: 'JavaScript Eval Script',
        state: GqlFeatureState.Experimental,
        plans: {
          [GqlPlanName.Free]: false,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.FeedAuthentication,
        title: 'Feed Authentication',
        state: GqlFeatureState.Experimental,
        plans: {
          [GqlPlanName.Free]: false,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.FeedsPrivateAccess,
        title: 'Private Feeds',
        state: GqlFeatureState.Beta,
        plans: {
          [GqlPlanName.Free]: false,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.BucketsPrivateAccess,
        title: 'Private Buckets',
        state: GqlFeatureState.Beta,
        plans: {
          [GqlPlanName.Free]: false,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.FeedsFulltext,
        title: 'Fulltext Feeds',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: false,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.ItemsInlineImages,
        title: 'Inline Images',
        state: GqlFeatureState.Stable,
        plans: {
          [GqlPlanName.Free]: false,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.ItemsNoUrlShortener,
        title: 'Resolve URL Shortener',
        state: GqlFeatureState.Experimental,
        plans: {
          [GqlPlanName.Free]: true,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.Api,
        title: 'API',
        state: GqlFeatureState.Off,
        plans: {
          [GqlPlanName.Free]: false,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.ItemEmailForward,
        title: 'Email Forwards',
        state: GqlFeatureState.Off,
        plans: {
          [GqlPlanName.Free]: false,
          [GqlPlanName.Basic]: true,
        },
      },
      {
        featureName: GqlFeatureName.ItemWebhookForward,
        title: 'Webhooks',
        state: GqlFeatureState.Off,
        plans: {
          [GqlPlanName.Free]: true,
          [GqlPlanName.Basic]: true,
        },
      },
    ];

    const featureGroups: FeatureGroup[] = [
      {
        groupLabel: 'General',
        featureNames: [
          GqlFeatureName.RateLimit,
          GqlFeatureName.FeedsMaxRefreshRate,
          GqlFeatureName.BucketsMaxCount,
          GqlFeatureName.FeedsMaxCount,
          GqlFeatureName.ItemsRetention
        ]
      },
      {
        groupLabel: 'Generate Feeds',
        featureNames: [
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
          GqlFeatureName.ItemsNoUrlShortener
        ]
      },
      {
        groupLabel: 'Integration',
        featureNames: [
          GqlFeatureName.Api,
          GqlFeatureName.ItemEmailForward,
          GqlFeatureName.ItemWebhookForward
        ]
      }
    ];

    const getFeatureConfig = (featureName: GqlFeatureName): FeatureConfig =>
      features.find((feature) => feature.featureName === featureName);

    const getPlanFeatureGroups = (planName: GqlPlanName): PlanFeatureGroup[] =>
      featureGroups.map(featureGroup => ({
        label: featureGroup.groupLabel,
        planFeatures: featureGroup.featureNames.map((featureName) => {
            const featureConfig = getFeatureConfig(featureName);
            return { config: featureConfig, value: featureConfig.plans[planName] };
          })
      }));
      // features.map((feature) => {
      //   const featureConfig = getFeatureLabel(feature.featureName);
      //   return { config: featureConfig, value: feature.plans[planName] };
      // });

    this.plans = [
      {
        planName: GqlPlanName.Free,
        description: 'Lorem ipsum',
        price: 0.0,
        color: 'var(--ion-color-primary)',
        action: {
          label: 'Join Public Beta',
          color: 'primary',
          redirectTo: '/signup',
        },
        featureGroups: getPlanFeatureGroups(GqlPlanName.Free),
      },
      {
        planName: GqlPlanName.Basic,
        description: 'Lorem ipsum',
        price: 9.99,
        color: 'var(--ion-color-dark)',
        action: {
          label: 'Contact Us',
          color: 'dark',
          redirectTo: '/contact',
        },
        featureGroups: getPlanFeatureGroups(GqlPlanName.Basic),
      },
    ];
  }

  isFalse(feature: PlanFeature): boolean {
    return `${feature.value}` === 'false';
  }
  isTrue(feature: PlanFeature): boolean {
    return `${feature.value}` === 'true';
  }

  isBoolean(feature: PlanFeature): boolean {
    return this.isTrue(feature) || this.isFalse(feature);
  }

  formatPrice(price: number): string {
    return price.toFixed(2);
  }

  isBeta(config: FeatureConfig): boolean {
    return config.state === GqlFeatureState.Beta;
  }

  isExperimental(config: FeatureConfig): boolean {
    return config.state === GqlFeatureState.Experimental;
  }
}
