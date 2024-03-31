import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import dayjs, { Dayjs } from 'dayjs';
import { ProductConfig, ProductService } from '../../services/product.service';
import { Subscription } from 'rxjs';
import { isUndefined } from 'lodash-es';
import { PopoverController } from '@ionic/angular';

type Day = {
  day: Dayjs | null;
  today?: boolean;
  past?: boolean;
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

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    readonly popoverCtrl: PopoverController,
    private readonly productService: ProductService,
  ) {}

  private fillCalendar() {
    this.years = {};
    const dayOfWeek = parseInt(this.refDate.format('d'));
    const ref = this.refDate.subtract(1, 'week').subtract(dayOfWeek, 'day');

    const isToday = (day: Dayjs): boolean => {
      return (
        this.now.isSame(day, 'day') &&
        this.now.isSame(day, 'month') &&
        this.now.isSame(day, 'year')
      );
    };

    let previousDay: Dayjs = null;
    const push = (day: Dayjs) => {
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
        past: this.now.isAfter(day),
      });

      previousDay = day;
    };

    for (let w = 0; w < 4; w++) {
      for (let d = 0; d < 7; d++) {
        push(ref.add(w, 'week').add(d + 1, 'day'));
      }
    }

    this.changeRef.detectChanges();
  }

  ngOnInit() {
    this.subscriptions.push(
      this.productService
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

  next() {
    this.refDate = this.refDate.add(1, 'week');
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
}
