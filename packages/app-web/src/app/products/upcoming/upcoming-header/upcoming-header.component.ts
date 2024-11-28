import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../../services/app-config.service';
import dayjs, { Dayjs, ManipulateType, OpUnitType } from 'dayjs';
import {
  clone,
  compact,
  debounce as debounceLD,
  DebouncedFunc,
  isUndefined,
  uniqBy,
} from 'lodash-es';
import { LatLon } from '../../../components/map/map.component';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import weekday from 'dayjs/plugin/weekday';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { getCachedLocations } from '../places';
import { addIcons } from 'ionicons';
import {
  calendarOutline,
  chevronBackOutline,
  chevronForwardOutline,
  locationOutline,
} from 'ionicons/icons';
import { NamedLatLon, Nullable } from '../../../types';
import {
  homeRoute,
  parseLocationFromUrl,
} from '../upcoming-product-routing.module';
import { getSavedLocations } from '../events/events.page';
import { OpenStreetMapService } from '../../../services/open-street-map.service';
import {
  IonHeader,
  IonToolbar,
  IonButton,
  IonInput,
  IonSelect,
  IonSelectOption,
  IonButtons,
  IonList,
  IonItem,
} from '@ionic/angular/standalone';
import { NgIf, NgFor } from '@angular/common';
import { DarkModeButtonComponent } from '../../../components/dark-mode-button/dark-mode-button.component';

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

type SiteLocale = 'de' | 'en';

type ExpandableSection = 'map' | 'calendar' | 'suggestions';

@Component({
  selector: 'app-upcoming-header',
  templateUrl: './upcoming-header.component.html',
  styleUrls: ['./upcoming-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonHeader,
    NgIf,
    IonToolbar,
    IonButton,
    IonInput,
    FormsModule,
    ReactiveFormsModule,
    IonSelect,
    IonSelectOption,
    IonButtons,
    DarkModeButtonComponent,
    IonList,
    IonItem,
    NgFor,
    RouterLink,
  ],
  standalone: true,
})
export class UpcomingHeaderComponent implements OnInit, OnDestroy, OnChanges {
  years: Years = {};
  productConfig: VerticalSpecWithRoutes;
  locationFc = new FormControl<string>('');
  private readonly subscriptions: Subscription[] = [];
  protected currentDate: Dayjs;
  protected currentLatLon: LatLon;
  protected readonly zhLatLon: LatLon = [47.3744489, 8.5410422];
  protected readonly now = dayjs();

  @Input({ required: true })
  date: Dayjs;

  @Input({ required: true })
  location: Nullable<NamedLatLon>;

  @Input({ required: true })
  perimeter: number;

  // @Input({ required: true })
  // categories: string[];

  protected expand: Nullable<ExpandableSection> = null;

  // protected showFilters: boolean = false;

  // private eventsOfMonth: LocalizedEvent[] = [];
  protected perimeterFc = new FormControl<number>(10);
  // protected categoriesFc = new FormControl<string[]>([]);
  // protected categories = ['Algemein', 'Kinder', 'Sport', 'Veranstaltung'];
  private timeWindowTo: number;
  private timeWindowFrom: number;
  protected locationSuggestions: NamedLatLon[];
  protected locationNotAvailable: boolean = true;
  protected currentLocation: NamedLatLon;
  private readonly fetchEventOverviewDebounced: DebouncedFunc<any>;
  protected locale: SiteLocale = 'de';
  // selectCategoriesOptions = {
  //   header: 'Kategorien',
  //   subHeader: 'Select your favorite color',
  // };

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    // private readonly apollo: ApolloClient<any>,
    private readonly router: Router,
    private readonly openStreetMapService: OpenStreetMapService,
    private readonly appConfigService: AppConfigService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    dayjs.extend(weekday);
    this.fetchEventOverviewDebounced = debounceLD(
      () => {},
      //   this.fetchEventOverview.bind(this),
      500,
    );
    addIcons({
      calendarOutline,
      locationOutline,
      chevronBackOutline,
      chevronForwardOutline,
    });
  }

  async ngOnInit() {
    this.perimeterFc.patchValue(this.perimeter, { emitEvent: false });
    this.locationNotAvailable = false;
    await this.changeLocation(this.location, false);
    await this.changeDate(this.date, false);
    this.changeRef.detectChanges();
    dayjs.locale('de');

    this.subscriptions.push(
      this.perimeterFc.valueChanges.subscribe(async () => {
        await this.fetchEventOverviewDebounced();
        await this.changeDate(this.currentDate);
        await this.patchUrl();
        this.changeRef.detectChanges();
      }),
      this.locationFc.valueChanges.subscribe(this.fetchSuggestions.bind(this)),
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
        }),
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('changes', changes);
    if (changes.perimeter?.currentValue) {
      this.perimeterFc.setValue(changes.perimeter.currentValue, {
        emitEvent: false,
      });
    }
    if (changes.date?.currentValue) {
      this.changeDate(changes.date.currentValue, false);
    }
    if (changes.location?.currentValue) {
      this.locationFc.setValue(
        changes.location.currentValue.displayName || '',
        {
          emitEvent: false,
        },
      );
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  // @HostListener('window:keydown.arrowup', ['$event'])
  // handleKeyUp() {
  //   console.log('up', this.focussedMatchIndex);
  //   if (this.focussedMatchIndex === 0) {
  //     return;
  //   }
  //   this.focussedMatchIndex--;
  //   if (this.focussedMatchIndex < 0) {
  //     this.focussedMatchIndex = this.locationSuggestions.length - 1;
  //   }
  //   this.changeRef.detectChanges();
  // }
  //
  // focussedMatchIndex: number = -1;
  //
  // @HostListener('window:keydown.arrowdown', ['$event'])
  // handleKeyDown() {
  //   console.log('down', this.focussedMatchIndex);
  //   if (this.focussedMatchIndex === this.locationSuggestions.length - 1) {
  //     return;
  //   }
  //   this.focussedMatchIndex++;
  //   if (this.focussedMatchIndex > this.locationSuggestions.length - 1) {
  //     this.focussedMatchIndex = 0;
  //   }
  //   this.changeRef.detectChanges();
  // }
  //
  @HostListener('window:keydown.enter', ['$event'])
  async handleEnter(event: KeyboardEvent) {
    if (this.locationSuggestions.length === 1) {
    }
    // console.log('handleEnter', this.focussedMatchIndex);
    // if (this.focussedMatchIndex >= 0 && this.locationSuggestions?.length > 0) {
    //   const searchResult = this.locationSuggestions[this.focussedMatchIndex];
    // } else {
    //
    // }
    event.stopPropagation();
  }

  private highlightTokens(tokens: string[]) {
    return (matches: NamedLatLon[]) =>
      matches
        .map((match) => clone(match))
        .map((match) => {
          match.displayName = tokens.reduce((hl, token) => {
            return hl.replace(new RegExp(token, 'i'), `<mark>${token}</mark>`);
          }, match.displayName);
          return match;
        });
  }

  async fetchSuggestions(query: string) {
    const tokens = compact(query.toLowerCase().trim().normalize().split(' '));
    const matchHighlighter = this.highlightTokens(tokens);
    this.expand = 'suggestions';
    let suggestions = getCachedLocations().filter((p) =>
      tokens.every((token) => p.index.indexOf(token) > -1),
    );

    if (query.length === 0) {
      suggestions = uniqBy(
        [...getSavedLocations(), ...suggestions],
        (l) => `${l.lat}:${l.lon}`,
      );
    }

    // if (staticMatches.length > 0) {
    this.locationSuggestions = matchHighlighter(
      suggestions.filter((_, index) => index < 6),
    );
    this.changeRef.detectChanges();
    // } else {
    // return matchHighlighter(
    //   await this.openStreetMapService.searchByQuery(`Schweiz ${query}`),
    // );
    // }
  }

  // formatDate(date: Dayjs, format: string) {
  //   return date?.locale(this.locale)?.format(format);
  // }

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

  // goToDateRelative(value: number, unit: ManipulateType) {
  //   this.changeMonth(this.currentDate.add(value, unit));
  // }

  // hasEvents(day: Dayjs): boolean {
  //   const freq = this.eventsOfMonth.find((event) =>
  //     day.isSame(event.group, 'day'),
  //   );
  //   return freq?.count > 0;
  // }

  // private async fetchEventOverview() {
  //   if (!this.currentLatLon) {
  //     return;
  //   }
  //   this.eventsOfMonth = await this.apollo
  //     .query<GqlFindEventsQuery, GqlFindEventsQueryVariables>({
  //       query: FindEvents,
  //       variables: {
  //         where: {
  //           repository: {
  //             id: this.getRepositoryId(),
  //           },
  //           latLng: {
  //             near: {
  //               lat: this.currentLatLon[0],
  //               lon: this.currentLatLon[1],
  //             },
  //             distanceKm: this.perimeterFc.value,
  //           },
  //           startedAt: {
  //             after: dayjs(this.timeWindowFrom).subtract(1, 'month').valueOf(),
  //             before: dayjs(this.timeWindowTo).add(1, 'month').valueOf(),
  //           },
  //         },
  //       },
  //     })
  //     .then((response) => {
  //       return response.data.recordsFrequency;
  //     });
  //   this.changeRef.detectChanges();
  // }

  async changeMonth(date: Dayjs) {
    if (!date) {
      return;
    }

    const diff = date.diff(dayjs(), 'months');
    if (diff < -3 || diff > 10) {
      return;
    }

    this.currentDate = date;

    // if (triggerUrlUpdate) {
    //   await this.patchUrl();
    // }

    this.fillCalendar(date);
    await this.fetchEventOverviewDebounced();

    this.changeRef.detectChanges();
  }

  // filterFirstWeek(days: Day[]): Day[] {
  //   return days.filter((day) => day.isFirstWeek);
  // }

  async changeLocation(location: NamedLatLon, triggerUrlUpdate = true) {
    this.expand = null;
    this.currentLocation = location;
    if (location) {
      this.currentLatLon = [location.lat, location.lon];
      this.locationFc.setValue(location.displayName, {
        emitEvent: false,
      });
    }

    if (triggerUrlUpdate) {
      this.patchUrl();
    }

    this.locationSuggestions = [];
    this.fetchEventOverviewDebounced();
    this.changeRef.detectChanges();
  }

  async changeDate(date: Dayjs, triggerUpdate: boolean = true) {
    this.currentDate = date;
    if (triggerUpdate) {
      await this.patchUrl();
    } else {
      this.fillCalendar(date);
    }
    this.changeRef.detectChanges();
  }

  private async patchUrl() {
    console.log('patchUrl');
    const { countryCode, area, place } = await parseLocationFromUrl(
      this.activatedRoute,
      this.openStreetMapService,
    );
    const url = homeRoute({})
      .events({})
      .countryCode({ countryCode })
      .region({
        region: area,
      })
      .place({
        place,
      })
      .dateTime({
        perimeter: this.perimeterFc.value,
        year: parseInt(this.currentDate.locale(this.locale).format('YYYY')),
        month: parseInt(this.currentDate.locale(this.locale).format('MM')),
        day: parseInt(this.currentDate.locale(this.locale).format('DD')),
      }).$;

    await this.router.navigateByUrl(url, { replaceUrl: true });
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
  perimeterOptions = {
    header: 'Umkreissuche',
    translucent: true,
  };

  // getLabelForCalendar(): string {
  //   if (this.currentDate) {
  //     if (this.currentDate.year() != dayjs().year()) {
  //       return this.formatDate(this.currentDate, 'D.MM.YYYY');
  //     } else {
  //       return this.formatDate(this.currentDate, 'D.MM.YY');
  //     }
  //   }
  // }

  getUrlForLocation({ countryCode, area, place }: NamedLatLon): string {
    return (
      '/' +
      homeRoute({})
        .events({})
        .countryCode({ countryCode })
        .region({ region: area })
        .place({
          place,
        })
        .dateTime({
          perimeter: this.perimeter,
          year: parseInt(this.currentDate.locale(this.locale).format('YYYY')),
          month: parseInt(this.currentDate.locale(this.locale).format('MM')),
          day: parseInt(this.currentDate.locale(this.locale).format('DD')),
        }).$
    );
  }
}
