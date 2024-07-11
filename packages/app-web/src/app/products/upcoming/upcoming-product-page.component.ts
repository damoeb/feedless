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
import { debounce, interval, Subscription } from 'rxjs';
import { isUndefined, sortBy } from 'lodash-es';
import { DocumentService } from '../../services/document.service';
import { WebDocument } from '../../graphql/types';
import { BubbleColor } from '../../components/bubble/bubble.component';
import { FormControl } from '@angular/forms';
import {
  OpenStreetMapService,
  OsmMatch,
} from '../../services/open-street-map.service';

type Day = {
  day: Dayjs | null;
  today?: boolean;
  past?: boolean;
  isFirstWeek?: boolean;
  otherMonth?: boolean;
  printMonth?: boolean;
};
type Months = {
  [month: number]: Day[];
};

type Years = {
  [year: number]: Months;
};

type Distance2Events = { [distance: string]: WebDocument[] };
type EventsByDistance = {
  distance: string;
  events: WebDocument[];
};

@Component({
  selector: 'app-upcoming-product-page',
  templateUrl: './upcoming-product-page.component.html',
  styleUrls: ['./upcoming-product-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UpcomingProductPage implements OnInit, OnDestroy {
  years: Years = {};
  productConfig: ProductConfig;
  locationFc = new FormControl<string>('');
  private subscriptions: Subscription[] = [];
  protected currentDateRef: Dayjs;
  // currentDay: Day;
  currentLatLon = [47.276541, 8.448084];

  protected now: Dayjs;
  private events: WebDocument[] = [];
  private timeWindowTo: number;
  private timeWindowFrom: number;
  protected locationSuggestions: OsmMatch[];
  protected isLocationFocussed: boolean = false;
  protected locationNotAvailable: boolean;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly documentService: DocumentService,
    private readonly openStreetMapService: OpenStreetMapService,
    private readonly appConfigService: AppConfigService,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.locationFc.valueChanges
        .pipe(debounce(() => interval(800)))
        .subscribe(async (value) => {
          this.locationSuggestions =
            await this.openStreetMapService.searchAddress(value);
          console.log('this.locationSuggestions', this.locationSuggestions);
          this.changeRef.detectChanges();
        }),
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
        }),
    );

    this.currentDateRef = dayjs();
    this.now = dayjs();

    this.locationNotAvailable = false;
    const location: OsmMatch = await this.initGeolocation()
      .then((location) =>
        this.openStreetMapService.reverseSearch(
          location.coords.latitude,
          location.coords.longitude,
        ),
      )
      .catch(() => {
        this.locationNotAvailable = true;
        return {
          lat: '47.276541',
          lon: '8.448084',
          display_name: 'Affoltern a.A.',
          address: {
            postcode: '8912',
            village: 'Affoltern a.A.',
          },
        };
      });

    this.setDate(this.now);
    await this.setLocation(location);
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  private fillCalendar(date: Dayjs) {
    this.years = {};
    const ref = date.set('date', 1);
    console.log('reference date', ref.format());

    this.timeWindowFrom = ref.valueOf();

    const isToday = (day: Dayjs): boolean => {
      return (
        this.now.isSame(day, 'day') &&
        this.now.isSame(day, 'month') &&
        this.now.isSame(day, 'year')
      );
    };

    const push = (
      day: Dayjs,
      context: Dayjs,
      isFirstWeek: boolean,
      otherMonth: boolean,
      printMonth: boolean,
    ) => {
      const month = context.month();
      const year = context.year();
      if (isUndefined(this.years[year])) {
        this.years[year] = {};
      }

      if (isUndefined(this.years[year][month])) {
        this.years[year][month] = [];
      }

      this.years[year][month].push({
        day,
        today: isToday(day),
        past: this.now.isAfter(day, 'day'),
        otherMonth,
        isFirstWeek,
        printMonth,
      });
    };

    const prefill = parseInt(ref.format('d')) - 1;
    for (let d = -prefill; d < 0; d++) {
      const day = ref.add(d, 'day');
      push(day, ref, d < 7, true, d === -prefill);
    }
    for (let d = 0; d < ref.daysInMonth(); d++) {
      const day = ref.add(d, 'day');
      push(day, day, prefill + d < 7, false, prefill > 0 && d === 0);
      this.timeWindowTo = day.valueOf();
    }

    const postfill = (ref.daysInMonth() + prefill) % 7;
    if (postfill > 0) {
      for (let d = 0; d < 7 - postfill; d++) {
        const day = ref.add(d + ref.daysInMonth(), 'day');
        push(day, ref, false, true, d === 0);
      }
    }
  }

  goToDateRelative(value: number, unit: ManipulateType) {
    this.setDate(this.currentDateRef.add(value, unit));
  }

  private initGeolocation() {
    return new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject);
    });
  }

  getEvents(day: Dayjs, maxItems: number = null): WebDocument[] {
    return this.events
      .filter((event) => dayjs(event.startingAt).isSame(day, 'day'))
      .filter((_, index) => !maxItems || index < maxItems - 1);
  }

  getEventGroups(day: Dayjs): EventsByDistance[] {
    const groups = this.getEvents(day).reduce((agg, event) => {
      const distance = this.getGeoDistance(event).toFixed(0);
      if (!agg[distance]) {
        agg[distance] = [];
      }

      agg[distance].push(event);

      return agg;
    }, {} as Distance2Events);

    return sortBy(
      Object.keys(groups).map((distance) => ({
        distance,
        events: groups[distance],
      })),
      (event) => parseInt(event.distance),
    );
  }

  private getDistanceFromLatLonInKm(
    lat1: number,
    lon1: number,
    lat2: number,
    lon2: number,
  ) {
    const R = 6371; // Radius of the earth in km
    const dLat = this.deg2rad(lat2 - lat1); // deg2rad below
    const dLon = this.deg2rad(lon2 - lon1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.deg2rad(lat1)) *
        Math.cos(this.deg2rad(lat2)) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; // Distance in km
  }

  private deg2rad(deg) {
    return deg * (Math.PI / 180);
  }

  getDots(day: Dayjs): BubbleColor[] {
    const colors: BubbleColor[] = ['blue', 'red', 'gray', 'green'];
    return this.getEvents(day)
      .map(() => colors[parseInt(`${Math.random() * colors.length}`)])
      .sort();
  }

  private fetchEvents() {
    return this.documentService.findAllByRepositoryId({
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
        localized: {
          near: {
            lat: this.currentLatLon[0],
            lon: this.currentLatLon[1],
          },
          distance: 15,
        },
        startedAt: {
          after: this.timeWindowFrom,
          before: this.timeWindowTo,
        },
      },
    });
  }

  setDate(date: Dayjs) {
    const diff = date.diff(this.now, 'months');
    if (diff < -3 || diff > 10) {
      return;
    }

    this.currentDateRef = date;
    this.fillCalendar(date);
  }

  filterFirstWeek(days: Day[]): Day[] {
    return days.filter((day) => day.isFirstWeek);
  }

  private getGeoDistance(event: WebDocument): number {
    return this.getDistanceFromLatLonInKm(
      event.localized.lat,
      event.localized.lon,
      this.currentLatLon[0],
      this.currentLatLon[1],
    );
  }

  async setLocation(location: OsmMatch) {
    this.isLocationFocussed = false;
    this.locationFc.setValue(this.getDisplayName(location), {
      emitEvent: false,
    });
    this.currentLatLon = [parseFloat(location.lat), parseFloat(location.lon)];
    this.locationSuggestions = [];
    this.events = await this.fetchEvents();
    this.changeRef.detectChanges();
  }

  getDisplayName(location: OsmMatch): string {
    // return `${location.address.postcode} ${location.address.village || location.address.town}`;
    return `${location.display_name}`;
  }

  getCurrentLocation() {}
}
