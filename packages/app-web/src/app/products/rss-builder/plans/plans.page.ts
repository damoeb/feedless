import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { GqlFeatureName } from '../../../../generated/graphql';
import { PlanService } from '../../../services/plan.service';
import { Plan } from '../../../graphql/types';
import { FeatureGroup, FeatureLabel } from '../../../components/plans/plans.component';

@Component({
  selector: 'app-rss-builder-plans-page',
  templateUrl: './plans.page.html',
  styleUrls: ['./plans.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlansPage implements OnInit {
  plans: Plan[];
  featureGroups: FeatureGroup<GqlFeatureName>[];
  labels: FeatureLabel[] = [
    {
      featureName: GqlFeatureName.PublicRepository,
      title: 'Public Feeds',
    },
    {
      featureName: GqlFeatureName.RepositoryRetentionMaxItemsUpperLimitInt,
      title: 'Items per Feed',
    },
    {
      featureName: GqlFeatureName.ScrapeSourceMaxCountTotal,
      title: 'Feeds',
    },
  ];

  constructor(
    private readonly planService: PlanService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.plans = await this.planService.fetchPlans();

    this.featureGroups = [
      {
        groupLabel: 'General',
        features: [
          GqlFeatureName.RateLimit,
          GqlFeatureName.Plugins,
          GqlFeatureName.PublicRepository,
        ],
      },
      {
        groupLabel: 'Storage',
        features: [
          GqlFeatureName.ScrapeSourceMaxCountTotal,
          GqlFeatureName.RepositoryRetentionMaxItemsUpperLimitInt,
        ],
      },
      // {
      //   groupLabel: 'Integration',
      //   features: [GqlFeatureName.Api],
      // },
    ];

    this.changeRef.detectChanges();
  }
}
