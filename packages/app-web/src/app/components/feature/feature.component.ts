import { Component, effect, input } from '@angular/core';
import { addIcons } from 'ionicons';
import { checkmarkOutline, closeOutline } from 'ionicons/icons';

import { IonCol, IonIcon, IonRow } from '@ionic/angular/standalone';
import { GqlFeatureName } from '../../../generated/graphql';
import { Feature } from '../../graphql/types';

type StringFeature = {
  title: string;
  subtitle?: string;
  valueHtml?: string;
  valueBool?: {
    value: boolean;
  };
};

@Component({
  selector: 'app-feature',
  templateUrl: './feature.component.html',
  styleUrls: ['./feature.component.scss'],
  imports: [IonRow, IonIcon, IonCol],
  standalone: true,
})
export class FeatureComponent {
  readonly feature = input.required<Feature>();

  protected sFeature: StringFeature;

  constructor() {
    addIcons({ checkmarkOutline, closeOutline });

    effect(() => {
      const title = this.localise(this.feature().name);
      if (title) {
        this.sFeature = {
          title: this.localise(this.feature().name),
          valueBool: this.feature().value.boolVal,
          valueHtml:
            this.feature().value.numVal != null
              ? this.feature().value.numVal.value == -1
                ? 'Infinite'
                : `${this.feature().value.numVal.value}`
              : null,
          subtitle: '',
        };
      }
    });
  }

  private localise(feature: GqlFeatureName): string {
    switch (feature) {
      case GqlFeatureName.Plugins:
        return 'Plugins';
      case GqlFeatureName.PublicRepository:
        return 'Public Feed Listing';
      case GqlFeatureName.RepositoryCapacityUpperLimitInt:
        return 'Feed Capacity';
      case GqlFeatureName.RepositoriesMaxCountTotalInt:
        return 'Feeds';
      case GqlFeatureName.SourceMaxCountPerRepositoryInt:
        return 'Sources per Feed';
    }
  }
}
