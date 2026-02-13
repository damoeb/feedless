import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  input,
  OnChanges,
  OnInit,
  output,
  PLATFORM_ID,
  SimpleChanges,
} from '@angular/core';
import dayjs, { Dayjs, OpUnitType } from 'dayjs';
import { RouterLink } from '@angular/router';
import { isPlatformBrowser, NgClass } from '@angular/common';
import 'dayjs/locale/de';
import { IonButton } from '@ionic/angular/standalone';
import { DateWindowItem, formatDate, getWeekday } from '../events/events.page';
import { addIcons } from 'ionicons';
import { calendarNumberOutline } from 'ionicons/icons';
import { IconComponent } from '@feedless/components';
import {
  RelativeDate,
  relativeDateIncrement,
} from '../../upcoming-product-routes';

@Component({
  selector: 'app-inline-calendar',
  templateUrl: './inline-calendar.component.html',
  styleUrls: ['./inline-calendar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonButton, IconComponent, NgClass, RouterLink],
  standalone: true,
})
export class InlineCalendarComponent implements OnInit, OnChanges {
  private readonly changeRef = inject(ChangeDetectorRef);

  readonly changeDate = output<Dayjs>();
  readonly now: Dayjs = dayjs();
  readonly date = input.required<Dayjs>();
  readonly paddingLeft = input.required<number>();
  readonly paddingRight = input.required<number>();
  readonly minDate = input.required<Dayjs>();
  readonly maxDate = input.required<Dayjs>();
  readonly dateUrlFactory = input.required<(date: Dayjs) => string>();
  private readonly platformId = inject(PLATFORM_ID);

  dateWindow: DateWindowItem[] = [];

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ calendarNumberOutline });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['date']?.currentValue) {
      this.createDateWindow(changes['date']?.currentValue);
    }
  }

  ngOnInit(): void {
    this.createDateWindow(this.date());
  }

  getDateUrl(date: Dayjs): string {
    return this.dateUrlFactory()(date);
  }

  isPast(day: Dayjs): boolean {
    return day.isBefore(dayjs().startOf('day'));
  }

  private createDateWindow(dateP: Dayjs) {
    const date = this.coerceDate(dateP, this.minDate(), this.maxDate());
    if (
      this.dateWindow.length > 0 &&
      this.dateWindow[0].date.isBefore(date) &&
      this.dateWindow[this.dateWindow.length - 1].date.isAfter(date) &&
      this.dateWindow[0].date.isAfter(dayjs().add(2, 'month').add(7, 'day'))
    ) {
      return;
    }

    const createDateWindowItem = (offset: number): DateWindowItem => {
      return {
        date: date.add(offset, 'day'),
        offset: Math.abs(offset),
      };
    };

    this.dateWindow = [
      createDateWindowItem(-1),
      createDateWindowItem(0),
      createDateWindowItem(1),
      createDateWindowItem(2),
      createDateWindowItem(3),
      createDateWindowItem(4),
      createDateWindowItem(5),
    ];
    this.changeRef.detectChanges();
  }

  private coerceDate(date: Dayjs, min: Dayjs, max: Dayjs) {
    if (date.isBefore(min)) {
      return min;
    } else {
      if (date.isAfter(max)) {
        return max;
      } else {
        return date;
      }
    }
  }

  protected readonly getWeekday = getWeekday;
  protected readonly formatDate = formatDate;

  protected isSameDate(a: Dayjs, b: Dayjs, units: OpUnitType[]) {
    return units.every((unit) => a.isSame(b, unit));
  }

  isDateInCalendar(date: Dayjs) {
    return this.dateWindow.some((d) =>
      this.isSameDate(date, d.date, ['year', 'month', 'day']),
    );
  }

  shiftDateWindow(offset: number, event: MouseEvent) {
    this.changeDate.emit(this.date().add(offset, 'day'));
    event.preventDefault();
    event.stopPropagation();
  }

  isValidOffset(offset: number) {
    return (
      this.date().add(offset, 'day').isAfter(this.minDate()) &&
      this.date().add(offset, 'day').isBefore(this.maxDate())
    );
  }

  getSeoLinkAttributes(date: Dayjs): string {
    const relativeDates = Object.keys(relativeDateIncrement) as RelativeDate[];
    const diff = date.diff(dayjs(), 'day');
    const relativeDateExpressionMaybe = relativeDates.find(
      (relativeDate) => relativeDateIncrement[relativeDate] === diff,
    );
    if (relativeDateExpressionMaybe) {
      return ['follow', 'index'].join(' ');
    } else {
      return ['follow', 'no-index'].join(' ');
    }
  }
}
