import { Component, input, OnInit, output } from '@angular/core';
import { KeyLabelOption } from '../../elements/select/select.component';
import { IonButton, IonCol, IonGrid, IonRow } from '@ionic/angular/standalone';
import { InputComponent } from '../../elements/input/input.component';

type SortDirection = 'asc' | 'desc';

export interface Field {
  type: 'text' | 'markup' | 'base64' | 'url' | 'date';
  name: string;
}

type ScheduledPolicy = {
  cronString: string;
};

export type SegmentedOutput = {
  filter?: string;
  orderBy?: string;
  orderAsc?: boolean;
  size?: number;
  digest?: boolean;
  scheduled?: ScheduledPolicy;
};

@Component({
  selector: 'app-segmented-output',
  templateUrl: './segmented-output.component.html',
  styleUrls: ['./segmented-output.component.scss'],
  imports: [IonGrid, IonRow, IonCol, IonButton, InputComponent],
  standalone: true,
})
export class SegmentedOutputComponent implements OnInit {
  readonly segmented = input.required<SegmentedOutput>();

  readonly fields = input.required<Field[]>();

  readonly segmentedChanged = output<SegmentedOutput>();

  // sortDirection: KeyLabelOption<SortDirection>[] = [
  //   {
  //     key: 'asc',
  //     label: 'Ascending',
  //   },
  //   {
  //     key: 'desc',
  //     label: 'Descending',
  //   },
  // ];

  // timeSegments: KeyLabelOption<number>[] = this.getTimeSegments();

  constructor() {}

  ngOnInit() {
    // if (this.segmented) {
    //   this.segmented = {
    //     // todo fill
    //   };
    // }
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
