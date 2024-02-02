import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { GqlFeatureName } from '../../../../generated/graphql';
import { PlanService } from '../../../services/plan.service';
import { Plan } from '../../../graphql/types';
import {
  FeatureGroup,
  FeatureLabel,
} from '../../../components/plans/plans.component';

@Component({
  selector: 'app-visual-diff-plans-page',
  templateUrl: './plans.page.html',
  styleUrls: ['./plans.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlansPage implements OnInit {
  plans: Plan[];
  featureGroups: FeatureGroup<GqlFeatureName>[];
  labels: FeatureLabel[] = [
    {
      featureName: GqlFeatureName.PublicScrapeSource,
      title: 'Public Trackers',
    },
    {
      featureName: GqlFeatureName.ScrapeSourceRetentionMaxItems,
      title: 'Items per Tracker',
    },
    {
      featureName: GqlFeatureName.ScrapeSourceMaxCountTotal,
      title: 'Trackers',
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

    this.changeRef.detectChanges();
  }
}
