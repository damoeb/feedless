import { Component, Input, OnInit } from '@angular/core';
import { Feature, Plan } from '../../graphql/types';
import {
  GqlFeatureName,
  GqlPlan,
  GqlPlanAvailability,
} from '../../../generated/graphql';

export type PlanAction = {
  label: string;
  color?: string;
  redirectTo: string;
};

export type FeatureGroup<T> = {
  groupLabel: string;
  features: T[];
};

export type PlanForUi = Partial<GqlPlan> & {
  featureGroups: FeatureGroup<Feature>[];
  action: PlanAction;
  color?: string;
};

export type FeatureLabel = {
  featureName: GqlFeatureName;
  title: string;
  subtitle?: string;
};

export type PlanHeaders = { [planName: string]: string };

@Component({
  selector: 'app-plans',
  templateUrl: './plans.component.html',
  styleUrls: ['./plans.component.scss'],
})
export class PlansComponent implements OnInit {
  @Input({ required: true })
  plans: Plan[];

  @Input({ required: true })
  featureGroups: FeatureGroup<GqlFeatureName>[];

  @Input()
  customLabels: FeatureLabel[];

  @Input()
  planHeaders: PlanHeaders;

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
    // {
    //   featureName: GqlFeatureName.Api,
    //   title: 'API',
    // },
    {
      featureName: GqlFeatureName.PublicRepository,
      title: 'Public Subscriptions',
    },
    {
      featureName: GqlFeatureName.RepositoryRetentionMaxItemsUpperLimitInt,
      title: 'Items per Subscription',
    },
    {
      featureName: GqlFeatureName.ScrapeSourceMaxCountTotal,
      title: 'Subscriptions',
    },
    {
      featureName: GqlFeatureName.Plugins,
      title: 'Plugins Support',
      subtitle: 'e.g. Fulltext, Privacy',
    },
    // {
    //   featureName: GqlFeatureName.ItemEmailForward,
    //   title: 'Email Forwards',
    // },
    // {
    //   featureName: GqlFeatureName.ItemWebhookForward,
    //   title: 'Webhooks',
    // },
  ];
  finalPlans: PlanForUi[];

  constructor() {}

  ngOnInit() {
    const toFeatureGroups = (features: Feature[]): FeatureGroup<Feature>[] =>
      this.featureGroups.map((group) => this.toFeatureGroup(group, features));

    this.finalPlans = this.plans.map<PlanForUi>((plan) => ({
      name: plan.name,
      currentCosts: plan.currentCosts,
      beforeCosts: plan.beforeCosts,
      color: 'var(--ion-color-dark)',
      // color: plan.isPrimary
      //   ? 'var(--ion-color-primary)'
      //   : 'var(--ion-color-dark)',
      action: this.getAction(plan.availability),
      featureGroups: toFeatureGroups(plan.features),
    }));
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

  getFeatureTitle(feature: Feature): string {
    const resolver = (label) => label.featureName === feature.name;
    return (this.customLabels?.find(resolver) ?? this.labels.find(resolver))
      .title;
  }

  getFeatureSubTitle(feature: Feature): string {
    const resolver = (label) => label.featureName === feature.name;
    return (this.customLabels?.find(resolver) ?? this.labels.find(resolver))
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
          redirectTo: '/login',
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
}
