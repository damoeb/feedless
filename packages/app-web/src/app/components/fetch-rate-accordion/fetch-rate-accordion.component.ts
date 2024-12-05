import {
  ChangeDetectionStrategy,
  Component,
  forwardRef,
  OnInit,
} from '@angular/core';
import {
  FormsModule,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { ControlValueAccessorDirective } from '../../directives/control-value-accessor/control-value-accessor.directive';
import { KeyValue } from '@angular/common';
import {
  IonCol,
  IonRow,
  IonSelect,
  IonSelectOption,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-fetch-rate-accordion',
  templateUrl: './fetch-rate-accordion.component.html',
  styleUrls: ['./fetch-rate-accordion.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FetchRateAccordionComponent),
      multi: true,
    },
  ],
  imports: [
    IonRow,
    IonCol,
    IonSelect,
    FormsModule,
    ReactiveFormsModule,
    IonSelectOption,
  ],
  standalone: true,
})
export class FetchRateAccordionComponent
  extends ControlValueAccessorDirective<string>
  implements OnInit
{
  options: KeyValue<string, string>[] = [
    {
      value: '0 */5 * * * *',
      key: 'Every 5 min',
    },
    {
      value: '0 */10 * * * *',
      key: 'Every 10 min',
    },
    {
      value: '0 */15 * * * *',
      key: 'Every 15 min',
    },
    {
      value: '0 */30 * * * *',
      key: 'Every 30 min',
    },
    {
      value: '0 0 * * * *',
      key: 'Every hour',
    },
    {
      value: '0 0 */6 * * *',
      key: 'Every 6 hours',
    },
    {
      value: '0 0 */12 * * *',
      key: 'Every 12 hours',
    },
    {
      value: '0 0 0 * * *',
      key: 'Every Day',
    },
    {
      value: '0 0 0 * * 0',
      key: 'Every Week',
    },
  ];

  ngOnInit() {
    super.ngOnInit();
  }

  getCurrentLabel() {
    return this.options.find((o) => o.value === this.control.value)?.key;
  }
}
