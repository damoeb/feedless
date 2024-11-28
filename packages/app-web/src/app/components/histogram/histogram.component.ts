import { Component, Input, OnInit } from '@angular/core';
import { GqlRecordFrequency } from '../../../generated/graphql';
import dayjs from 'dayjs';
import { sumBy, times } from 'lodash-es';
import { scaleLinear } from 'd3-scale';
import { NgClass, NgIf } from '@angular/common';

@Component({
  selector: 'app-histogram',
  templateUrl: './histogram.component.html',
  styleUrls: ['./histogram.component.scss'],
  imports: [NgClass, NgIf],
  standalone: true,
})
export class HistogramComponent implements OnInit {
  @Input({ required: true })
  data: GqlRecordFrequency[];
  path: string;
  rate: number;

  constructor() {}

  ngOnInit() {
    const currentDate = dayjs();
    const maxPerDay = 5;
    this.rate = sumBy(this.data, 'count');
    const scaleCount = scaleLinear().domain([0, maxPerDay]).range([0, -20]);

    const path = times(28)
      .reverse()
      .map((offset) => currentDate.subtract(offset, 'days').format('YYYYMMDD'))
      .map((dateStr, index) => ({
        index,
        count:
          this.data.find((i) => dayjs(i.group).format('YYYYMMDD') === dateStr)
            ?.count || 0,
      }))
      .map(
        (v, index) =>
          `L ${3.5 * index} ${scaleCount(Math.min(v.count, maxPerDay))}`,
      );

    this.path = 'M 0 0 ' + path.join(' ');
  }
}
