import { Component, inject, input, PLATFORM_ID } from '@angular/core';
import { addIcons } from 'ionicons';
import { checkmarkOutline, closeOutline } from 'ionicons/icons';
import { FeatureComponent } from '../feature/feature.component';
import { Feature } from '@feedless/graphql-api';
import { isPlatformBrowser } from '@angular/common';

export type StringFeatureGroup = {
  groupLabel: string;
  features: Feature[];
};

@Component({
  selector: 'app-plan-column',
  templateUrl: './plan-column.component.html',
  styleUrls: ['./plan-column.component.scss'],
  imports: [FeatureComponent],
  standalone: true,
})
export class PlanColumnComponent {
  readonly price = input<string>();

  readonly imploded = input<boolean>();

  readonly featureGroups = input<StringFeatureGroup[]>();

  readonly pricePerUnit = input<string>('/ Month');
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ checkmarkOutline, closeOutline });
    }
  }
}
