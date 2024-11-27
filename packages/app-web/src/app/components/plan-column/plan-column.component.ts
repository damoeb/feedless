import { Component, Input } from '@angular/core';
import { addIcons } from 'ionicons';
import { checkmarkOutline, closeOutline } from 'ionicons/icons';

export type StringFeature = {
  title: string;
  subtitle?: string;
  valueHtml?: string;
  valueBool?: {
    value: boolean;
  };
};

export type StringFeatureGroup = {
  groupLabel: string;
  features: StringFeature[];
};

@Component({
    selector: 'app-plan-column',
    templateUrl: './plan-column.component.html',
    styleUrls: ['./plan-column.component.scss'],
    standalone: false
})
export class PlanColumnComponent {
  @Input()
  price: string;

  @Input()
  imploded: boolean;

  @Input()
  featureGroups: StringFeatureGroup[];

  @Input()
  pricePerUnit: string = '/ Month';

  constructor() {
    addIcons({ checkmarkOutline, closeOutline });
  }
}
