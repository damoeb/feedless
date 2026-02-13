
import { Component, input, output } from '@angular/core';
import { IonButton, IonCol, IonGrid, IonRow } from '@ionic/angular/standalone';
import { InputComponent } from '../../form-elements/input/input.component';
import { KeyLabelOption } from '../../form-elements/select/select.component';

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
export class SegmentedOutputComponent {
  readonly segmented = input.required<SegmentedOutput>();

  readonly fields = input.required<Field[]>();

  readonly segmentedChanged = output<SegmentedOutput>();

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
