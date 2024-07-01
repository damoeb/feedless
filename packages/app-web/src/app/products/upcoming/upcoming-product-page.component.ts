import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import dayjs, { Dayjs, ManipulateType } from 'dayjs';
import {
  AppConfigService,
  ProductConfig,
} from '../../services/app-config.service';
import { Subscription } from 'rxjs';
import { isUndefined } from 'lodash-es';
import { DocumentService } from '../../services/document.service';
import { WebDocument } from '../../graphql/types';
import { BubbleColor } from '../../components/bubble/bubble.component';

type Day = {
  day: Dayjs | null;
  today?: boolean;
  past?: boolean;
  isFirstWeek?: boolean;
};
type Months = {
  [month: number]: Day[];
};

type Years = {
  [year: number]: Months;
};

@Component({
  selector: 'app-upcoming-product-page',
  templateUrl: './upcoming-product-page.component.html',
  styleUrls: ['./upcoming-product-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UpcomingProductPage implements OnInit, OnDestroy {
  // map https://leafletjs.com/examples/quick-start/example-overlays.html
  // https://forum.ionicframework.com/t/ionic-6-and-leaflet-1-7-1/202030

  years: Years = {};
  productConfig: ProductConfig;
  private subscriptions: Subscription[] = [];
  private refDate: Dayjs;
  private now: Dayjs;
  private events: WebDocument[] = [];
  private timeWindowTo: number;
  private timeWindowFrom: number;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly documentService: DocumentService,
    private readonly appConfigService: AppConfigService,
  ) {}

  private fillCalendar() {
    this.years = {};
    const dayOfWeek = parseInt(this.refDate.format('d'));
    const ref = this.refDate.subtract(dayOfWeek, 'day');

    this.timeWindowFrom = ref.valueOf();

    const isToday = (day: Dayjs): boolean => {
      return (
        this.now.isSame(day, 'day') &&
        this.now.isSame(day, 'month') &&
        this.now.isSame(day, 'year')
      );
    };

    let previousDay: Dayjs = null;
    const push = (day: Dayjs, isFirstWeek: boolean) => {
      const month = day.month();
      const year = day.year();
      if (isUndefined(this.years[year])) {
        this.years[year] = {};
      }

      if (isUndefined(this.years[year][month])) {
        this.years[year][month] = [];
      }

      const changedMonth = previousDay && month != previousDay.month();
      if (changedMonth) {
        // prefill empty

        for (let i = 0; i < parseInt(day.format('d')) - 1; i++) {
          this.years[year][month].push({ day: null });
        }
      }
      this.years[year][month].push({
        day,
        today: isToday(day),
        past: this.now.isAfter(day, 'day'),
        isFirstWeek,
      });

      previousDay = day;
    };

    for (let w = 0; w < 4; w++) {
      for (let d = 0; d < 7; d++) {
        const day = ref.add(w, 'week').add(d + 1, 'day');
        push(day, w === 0);
        this.timeWindowTo = day.valueOf();
      }
    }

    this.changeRef.detectChanges();
    this.fetchEvents();
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
    );

    this.refDate = dayjs();
    this.now = dayjs();

    this.initGeolocation();
    this.fillCalendar();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  getMonth(month: unknown): string {
    return dayjs()
      .set('month', parseInt(`${month}`))
      .format('MMMM');
  }

  next(value: number, unit: ManipulateType) {
    this.refDate = this.refDate.add(value, unit);
    this.fillCalendar();
  }

  private initGeolocation() {
    navigator.geolocation.getCurrentPosition(
      this.handleLocation,
      console.error,
    );
  }

  private handleLocation(position: GeolocationPosition) {
    console.log(position);
  }

  getEvents(day: Dayjs, maxItems: number = null): WebDocument[] {
    return this.events
      .filter((event) => dayjs(event.startingAt).isSame(day, 'day'))
      .filter((_, index) => !maxItems || index < maxItems - 1);
  }

  getDots(day: Dayjs): BubbleColor[] {
    const colors: BubbleColor[] = ['blue', 'red', 'gray', 'green'];
    return this.getEvents(day)
      .map(() => colors[parseInt(`${Math.random() * colors.length}`)])
      .sort();
  }

  private async fetchEvents() {
    this.events = await this.documentService.findAllByRepositoryId({
      cursor: {
        page: 0,
        pageSize: 100,
      },
      where: {
        repository: {
          where: {
            id: 'abe894d1-2098-4a1f-b7fe-2fc8e6d74105',
          },
        },
        startedAt: {
          after: this.timeWindowFrom,
          before: this.timeWindowTo,
        },
      },
    });
    this.changeRef.detectChanges();
  }
}
