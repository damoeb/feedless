import { Component, Input, input } from '@angular/core';
import { addIcons } from 'ionicons';
import { checkmarkOutline, closeOutline } from 'ionicons/icons';

import { IonRow, IonCol, IonIcon } from '@ionic/angular/standalone';

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
  imports: [IonRow, IonCol, IonIcon],
  standalone: true,
})
export class PlanColumnComponent {
  readonly price = input<string>();

  readonly imploded = input<boolean>();

  @Input()
  featureGroups: StringFeatureGroup[];

  readonly pricePerUnit = input<string>('/ Month');

  constructor() {
    addIcons({ checkmarkOutline, closeOutline });
  }
}
