import { Component, effect, inject, input, PLATFORM_ID } from '@angular/core';
import { addIcons } from 'ionicons';
import { checkmarkOutline, closeOutline } from 'ionicons/icons';

import { IonCol, IonRow } from '@ionic/angular/standalone';
import { Feature, GqlFeatureName } from '@feedless/graphql-api';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../icon/icon.component';

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
  imports: [IonRow, IconComponent, IonCol],
  standalone: true,
})
export class FeatureComponent {
  readonly feature = input.required<Feature>();

  protected sFeature: StringFeature;
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ checkmarkOutline, closeOutline });
    }

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
    return '';
  }
}
