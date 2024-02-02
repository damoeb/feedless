import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Field } from '../../modals/feed-builder-modal/scrape-builder';
import { SegmentedOutput } from '../../modals/feed-builder-modal/feed-builder-modal.component';
import { KeyLabelOption } from '../../elements/select/select.component';

type SortDirection = 'asc' | 'desc';

@Component({
  selector: 'app-segmented-output',
  templateUrl: './segmented-output.component.html',
  styleUrls: ['./segmented-output.component.scss'],
})
export class SegmentedOutputComponent implements OnInit {
  @Input({ required: true })
  segmented: SegmentedOutput;

  @Input({ required: true })
  fields: Field[] = [];

  @Output()
  segmentedChanged: EventEmitter<SegmentedOutput> =
    new EventEmitter<SegmentedOutput>();

  sortDirection: KeyLabelOption<SortDirection>[] = [
    {
      key: 'asc',
      label: 'Ascending',
    },
    {
      key: 'desc',
      label: 'Descending',
    },
  ];

  timeSegments: KeyLabelOption<number>[] = this.getTimeSegments();

  constructor() {}

  ngOnInit() {
    if (this.segmented) {
      this.segmented = {
        // todo fill
      };
    }
  }

  private getTimeSegments(): KeyLabelOption<number>[] {
    const hour = 60;
    const day = 24 * hour;
    return [
      {
        key: day,
        label: 'Every day',
      },
      {
        key: 7 * day,
        label: 'Every week',
      },
      {
        key: 28 * day,
        label: 'Every month',
      },
    ];
  }
}
