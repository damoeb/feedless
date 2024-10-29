import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { AppConfigService, ProductConfig } from '../../../services/app-config.service';
import dayjs, { Dayjs, ManipulateType, OpUnitType } from 'dayjs';
import { OpenStreetMapService, OsmMatch } from '../../../services/open-street-map.service';
import { compact, debounce as debounceLD, DebouncedFunc, isUndefined, omit } from 'lodash-es';
import { GetElementType } from '../../../graphql/types';
import { LatLon } from '../../../components/map/map.component';
import { FindEvents, GqlFindEventsQuery, GqlFindEventsQueryVariables } from '../../../../generated/graphql';
import { FormControl } from '@angular/forms';
import { debounce, interval, Subscription } from 'rxjs';
import { ApolloClient } from '@apollo/client/core';
import weekday from 'dayjs/plugin/weekday';


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

type LocalizedEvent = GetElementType<GqlFindEventsQuery['recordsFrequency']>;

export type EventsUrlFragments = {
  state?: string;
  country?: string;
  place?: string;
  perimeter?: number;
  year?: number;
  month?: number;
  day?: number;
};

type SiteLocale = 'de' | 'en';

export function convertOsmMatchToString(location: OsmMatch): string {
  if (!location) {
    return '';
  }
  const fields: (keyof OsmMatch['address'])[] = [
    'country',
    'country_code',
    'ISO3166-2-lvl4',
    'postcode',
    'state',
    'amenity',
    'house_number',
    'road',
    'county',
    'neighbourhood',
    'city_district',
    'state_district',
  ];
  return compact(Object.values(omit(location.address, ...fields))).join(' ');
}

@Component({
  selector: 'app-upcoming-header',
  templateUrl: './upcoming-header.component.html',
  styleUrls: ['./upcoming-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UpcomingHeaderComponent  implements OnInit, OnDestroy, OnChanges {
  years: Years = {};
  productConfig: ProductConfig;
  locationFc = new FormControl<string>('');
  private subscriptions: Subscription[] = [];
  protected currentDate: Dayjs;
  protected currentLatLon: LatLon;
  protected now = dayjs();

  @Input({required: true})
  date: Dayjs

  @Input({required: true})
  location: OsmMatch

  @Input()
  perimeter: number = 10;

  @Input({required: true})
  categories: string[]

  protected showMap: boolean = false;
  protected showCalendar: boolean = false;
  protected showFilters: boolean = false;

  private eventsOfMonth: LocalizedEvent[] = [];
  protected perimeterFc = new FormControl<number>(10);
  protected categoriesFc = new FormControl<string[]>([]);
  // protected categories = ['Algemein', 'Kinder', 'Sport', 'Veranstaltung'];
  private timeWindowTo: number;
  private timeWindowFrom: number;
  protected locationSuggestions: OsmMatch[];
  protected isLocationFocussed: boolean = false;
  protected locationNotAvailable: boolean = true;
  protected currentLocation: OsmMatch;
  protected loadingCalendar = true;
  private readonly fetchEventOverviewDebounced: DebouncedFunc<any>;
  protected locale: SiteLocale = 'de';
  selectCategoriesOptions = {
    header: 'Kategorien',
    subHeader: 'Select your favorite color',
  };
  // protected eventId: string;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly apollo: ApolloClient<any>,
    private readonly openStreetMapService: OpenStreetMapService,
    private readonly appConfigService: AppConfigService,
  ) {
    dayjs.extend(weekday);
    this.fetchEventOverviewDebounced = debounceLD(
      this.fetchEventOverview.bind(this),
      500,
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log(changes);
    if (changes.perimeter?.currentValue) {
      this.perimeterFc.setValue(changes.perimeter.currentValue);
    }
    if (changes.date?.currentValue) {
      this.changeDate(changes.date.currentValue);
    }
  }

  async ngOnInit() {
    this.perimeterFc.patchValue(this.perimeter, {emitEvent: false});
    this.locationNotAvailable = false;
    await this.changeLocation(this.location, false);
    await this.changeDate(this.currentDate);
    this.changeRef.detectChanges();
    dayjs.locale('de');

    this.loadingCalendar = false;

    this.subscriptions.push(
      this.perimeterFc.valueChanges.subscribe(async () => {
        await this.fetchEventOverviewDebounced();
        await this.changeDate(this.currentDate);
        await this.patchUrl();
        this.changeRef.detectChanges();
      }),
      this.locationFc.valueChanges
        .pipe(debounce(() => interval(400)))
        .subscribe(async (value) => {
          this.locationSuggestions =
            await this.openStreetMapService.searchAddress(`${value} Schweiz`);
          // console.log('this.locationSuggestions', this.locationSuggestions);
          this.changeRef.detectChanges();
        }),
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
        }),
    );
  }

  formatDate(date: Dayjs, format: string) {
    return date?.locale(this.locale)?.format(format);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  private fillCalendar(date: Dayjs) {
    this.years = {};
    const ref = date.set('date', 1);

    this.timeWindowFrom = ref.valueOf();
    const now = dayjs();

    const isToday = (day: Dayjs): boolean => {
      return (
        now.isSame(day, 'day') &&
        now.isSame(day, 'month') &&
        now.isSame(day, 'year')
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
        past: now.isAfter(day, 'day'),
        otherMonth,
        isFirstWeek,
        printMonth,
      });
    };

    const prefill = parseInt(ref.format('d'));
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
        this.timeWindowTo = day.valueOf();
      }
    }
  }

  goToDateRelative(value: number, unit: ManipulateType) {
    this.changeMonth(this.currentDate.add(value, unit));
  }

  private initGeolocation() {
    return new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject);
    });
  }

  hasEvents(day: Dayjs): boolean {
    const freq = this.eventsOfMonth.find((event) =>
      day.isSame(event.group, 'day'),
    );
    return freq?.count > 0;
  }

  private async fetchEventOverview() {
    if (!this.currentLatLon) {
      return;
    }
    this.eventsOfMonth = await this.apollo
      .query<GqlFindEventsQuery, GqlFindEventsQueryVariables>({
        query: FindEvents,
        variables: {
          where: {
            repository: {
              id: this.getRepositoryId(),
            },
            latLng: {
              near: {
                lat: this.currentLatLon[0],
                lon: this.currentLatLon[1],
              },
              distanceKm: this.perimeterFc.value,
            },
            startedAt: {
              after: dayjs(this.timeWindowFrom).subtract(1, 'month').valueOf(),
              before: dayjs(this.timeWindowTo).add(1, 'month').valueOf(),
            },
          },
        },
      })
      .then((response) => {
        return response.data.recordsFrequency;
      });
    this.changeRef.detectChanges();
  }

  async changeMonth(date: Dayjs, triggerUrlUpdate = true) {
    if (!date) {
      return;
    }

    const diff = date.diff(dayjs(), 'months');
    if (diff < -3 || diff > 10) {
      return;
    }

    this.currentDate = date;

    if (triggerUrlUpdate) {
      await this.patchUrl();
    }

    this.fillCalendar(date);
    await this.fetchEventOverviewDebounced();

    this.changeRef.detectChanges();
  }

  filterFirstWeek(days: Day[]): Day[] {
    return days.filter((day) => day.isFirstWeek);
  }

  async changeLocation(location: OsmMatch, triggerUrlUpdate = true) {
    this.isLocationFocussed = false;
    this.currentLocation = location;
    if (location) {
      this.currentLatLon = [parseFloat(location.lat), parseFloat(location.lon)];
      this.locationFc.setValue(convertOsmMatchToString(location), {
        emitEvent: false,
      });
    }

    if (triggerUrlUpdate) {
      this.patchUrl();
    }

    this.locationSuggestions = [];
    await this.fetchEventOverviewDebounced();
    this.changeRef.detectChanges();
  }

  getCurrentLocation() {}

  async changeDate(day: Dayjs) {
    this.currentDate = day;
    await this.patchUrl();
    this.changeRef.detectChanges();
  }

  private async patchUrl() {
    console.log('patchUrl')
    // const parts: EventsUrlFragments = {
    //   state: this.currentLocation.address.state,
    //   country: this.currentLocation.address.country_code?.toUpperCase(),
    //   perimeter: this.perimeterFc.value,
    //   place: convertOsmMatchToString(this.currentLocation),
    //   year: this.currentDate.locale(this.locale).year(),
    //   month: this.currentDate.locale(this.locale).month(),
    //   day: this.currentDate.locale(this.locale).day(),
    // };
    // const url = createEventsUrl(parts, this.router)
    // await this.router.navigateByUrl(url)
  }

  isSame(a: Dayjs, b: Dayjs, units: OpUnitType[]) {
    return units.every((unit) => a.isSame(b, unit));
  }

  private getRepositoryId(): string {
    return this.appConfigService.customProperties.eventRepositoryId as any;
  }

  handlePositionChange(latLon: number[]) {}

  convertOsmMatchToString(location: OsmMatch) {
    return convertOsmMatchToString(location);
  }

  // formatToRelativeDay(inputDate: Dayjs, suffix: string = '') {
  //   const today = dayjs();
  //
  //   if (inputDate) {
  //     if (inputDate.isSame(today, 'day')) {
  //       return 'Heute' + suffix;
  //     } else if (inputDate.subtract(1, 'day').isSame(today, 'day')) {
  //       return 'Morgen' + suffix;
  //     } else if (inputDate.add(1, 'day').isSame(today, 'day')) {
  //       return 'Gestern' + suffix;
  //     }
  //   }
  //   return '';
  // }

  getLabelForCalendar(): string {
    if (this.currentDate) {
      if (this.currentDate.year() != dayjs().year()) {
        return this.formatDate(this.currentDate, "D.MM.YYYY")
      } else {
        return this.formatDate(this.currentDate, "D.MM")
      }
    }
  }

  getWeekday(): string {
    if (this.currentDate) {
      return [
        'Sonntag', 'Montag', 'Dienstag', 'Mittwoch',
        'Donnerstag', 'Freitag', 'Samstag'
      ][this.currentDate.day()];
    }
  }
}
