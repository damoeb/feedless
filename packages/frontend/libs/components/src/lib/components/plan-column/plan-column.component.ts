import { Component, input } from '@angular/core';
import { addIcons } from 'ionicons';
import { checkmarkOutline, closeOutline } from 'ionicons/icons';
import { FeatureComponent } from '../feature/feature.component';
import { Feature } from '@feedless/graphql-api';

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

  constructor() {
    addIcons({ checkmarkOutline, closeOutline });
  }
}
