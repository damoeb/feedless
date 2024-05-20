import { Component, EventEmitter, Input, Output } from '@angular/core';

export type StringFeature = {
  title: string
  subtitle?: string
  valueHtml?: string
  valueBool?: {
    value: boolean
  }
}

export type StringFeatureGroup = {
  groupLabel: string;
  features: StringFeature[];
}

@Component({
  selector: 'app-plan-column',
  templateUrl: './plan-column.component.html',
  styleUrls: ['./plan-column.component.scss'],
})
export class PlanColumnComponent {
  @Input({ required: true })
  price: string

  @Input({ required: true })
  featureGroups: StringFeatureGroup[]

  @Input()
  pricePerUnit: string = '/ Month';

  @Input()
  scrollTop: number = 0;

  @Output()
  scrollTopChange: EventEmitter<number> = new EventEmitter<number>();

  constructor() {}

  onScroll(event: Event) {
    this.scrollTop = (event.target as any).scrollTop;
    this.scrollTopChange.emit(this.scrollTop);
  }
}
