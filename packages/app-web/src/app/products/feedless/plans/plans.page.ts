import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { GqlFeatureName, GqlPlanName } from '../../../../generated/graphql';
import { PlanService } from '../../../services/plan.service';
import { Plan } from '../../../graphql/types';
import { FeatureGroup, FeatureLabel, PlanHeaders } from '../../../components/plans/plans.component';


@Component({
  selector: 'app-feedless-plans-page',
  templateUrl: './plans.page.html',
  styleUrls: ['./plans.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlansPage implements OnInit {
  plans: Plan[];
  featureGroups: FeatureGroup<GqlFeatureName>[];
  labels: FeatureLabel[] = [
    {
      featureName: GqlFeatureName.PublicScrapeSource,
      title: 'Public Feeds'
    },
    {
      featureName: GqlFeatureName.ScrapeSourceRetentionMaxItems,
      title: 'Items per Feed'
    },
    {
      featureName: GqlFeatureName.ScrapeSourceMaxCountTotal,
      title: 'Feeds'
    },
  ];
  headers: PlanHeaders = {
    [GqlPlanName.Free]: 'Developer',
    [GqlPlanName.Basic]: 'Pro',
  };

  constructor(private readonly planService: PlanService,
              private readonly changeRef: ChangeDetectorRef) {
  }

  async ngOnInit() {
    this.plans = await this.planService.fetchPlans();

    this.featureGroups = [
      {
        groupLabel: 'General',
        features: [
          GqlFeatureName.RateLimit,
          GqlFeatureName.Plugins,
          GqlFeatureName.PublicScrapeSource
        ]
      },
      {
        groupLabel: 'Storage',
        features: [
          GqlFeatureName.ScrapeSourceMaxCountTotal,
          GqlFeatureName.ScrapeSourceRetentionMaxItems
        ]
      },
      {
        groupLabel: 'Integration',
        features: [
          GqlFeatureName.Api,
        ]
      }
    ];



    this.changeRef.detectChanges();
  }
}
