import { Component, Input, OnInit } from '@angular/core';
import { GqlFeatureName, GqlFeatureState } from '../../../generated/graphql';
import { ServerSettingsService } from '../../services/server-settings.service';
import { Feature } from '../../graphql/types';

@Component({
  selector: 'app-feature-chip',
  templateUrl: './feature-chip.component.html',
  styleUrls: ['./feature-chip.component.scss'],
})
export class FeatureChipComponent implements OnInit {
  @Input()
  featureName: GqlFeatureName;
  feature: Feature;

  constructor(private readonly serverSettings: ServerSettingsService) {}

  ngOnInit() {
    const feature = this.serverSettings.getFeature(this.featureName);
    if (
      feature &&
      ![GqlFeatureState.Stable, GqlFeatureState.Off].includes(feature.state)
    ) {
      this.feature = feature;
    }
  }
}
