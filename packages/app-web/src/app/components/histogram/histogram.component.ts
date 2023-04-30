import { Component, Input, OnInit } from '@angular/core';
import { GqlHistogram } from '../../../generated/graphql';
import dayjs from 'dayjs';
import { sumBy, times } from 'lodash-es';
import { ScaleLinear, scaleLinear } from 'd3-scale';

@Component({
  selector: 'app-histogram',
  templateUrl: './histogram.component.html',
  styleUrls: ['./histogram.component.scss'],
})
export class HistogramComponent implements OnInit {
  @Input()
  data: GqlHistogram;
  path: string;
  rate: number;

  constructor() {}

  ngOnInit() {
    const currentDate = dayjs();
    const maxPerDay = 5;
    this.rate = sumBy(this.data.items, 'count');
    const scaleCount = scaleLinear().domain([0, maxPerDay]).range([0, -20]);

    const path = times(28)
      .reverse()
      .map((offset) => currentDate.subtract(offset, 'days').format('YYYYMMDD'))
      .map((dateStr, index) => ({
        index,
        count: this.data.items.find((i) => i.index === dateStr)?.count || 0,
      }))
      .map(
        (v, index) =>
          `L ${3.5 * index} ${scaleCount(Math.min(v.count, maxPerDay))}`
      );

    this.path = 'M 0 0 ' + path.join(' ');
  }
}
