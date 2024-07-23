import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import dayjs, { Dayjs, ManipulateType, OpUnitType } from 'dayjs';
import { AppConfigService, ProductConfig } from '../../services/app-config.service';
import { debounce, interval, Subscription } from 'rxjs';
import { debounce as debounceLD, compact, groupBy, isEqual, isUndefined, omit, sortBy, unionBy, DebouncedFunc, times } from 'lodash-es';
import { DocumentService } from '../../services/document.service';
import { GetElementType, WebDocument } from '../../graphql/types';
import { FormControl } from '@angular/forms';
import { OpenStreetMapService, OsmMatch } from '../../services/open-street-map.service';
import { ApolloClient } from '@apollo/client/core';
import { FindEvents, GqlFindEventsQuery, GqlFindEventsQueryVariables } from '../../../generated/graphql';
import { RepositoryService } from '../../services/repository.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';

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

type PlaceByDistance = {
  distance: string;
  places: EventsAtPlace[];
};

type EventsAtPlace = {
  place: string
  events: WebDocument[]
}

type LocalizedEvent = GetElementType<GqlFindEventsQuery['webDocumentsFrequency']>;

type UrlFragments = {
  state?: string,
  country?: string,
  place?: string,
  year?: string,
  month?: string
  day?: string
}

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
  private currentLatLon: number[];

  private readonly repositoryId = 'd7a27f3a-27bd-4917-9eda-2ab84800cb0a';

  protected now: Dayjs;
  private eventsOfMonth: LocalizedEvent[] = [];
  protected placesByDistance: PlaceByDistance[] = [];

  protected distanceFc = new FormControl<number>(10);
  private timeWindowTo: number;
  private timeWindowFrom: number;
  protected locationSuggestions: OsmMatch[];
  protected isLocationFocussed: boolean = false;
  protected locationNotAvailable: boolean;
  private currentLocation: OsmMatch;
  protected loading = true;
  private readonly fetchEventOverviewDebounced: DebouncedFunc<any>;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly documentService: DocumentService,
    private readonly repositoryService: RepositoryService,
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly location: Location,
    private readonly apollo: ApolloClient<any>,
    private readonly openStreetMapService: OpenStreetMapService,
    private readonly appConfigService: AppConfigService,
  ) {
    this.fetchEventOverviewDebounced = debounceLD(this.fetchEventOverview.bind(this), 500);
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.distanceFc.valueChanges.subscribe(async () => {
        await this.fetchEventOverviewDebounced();
        this.changeRef.detectChanges();
      }),
      this.locationFc.valueChanges
        .pipe(debounce(() => interval(800)))
        .subscribe(async (value) => {
          this.locationSuggestions =
            await this.openStreetMapService.searchAddress(value);
          // console.log('this.locationSuggestions', this.locationSuggestions);
          this.changeRef.detectChanges();
        }),
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
        }),
    );

    this.now = dayjs();
    await this.changeMonth(await this.parseDateFromUrl().catch(() => dayjs()), false)

    this.locationNotAvailable = false;
    this.currentLocation = await this.parseLocationFromUrl()
      .catch(() => this.initGeolocation()
        .then((location) =>
          this.openStreetMapService.reverseSearch(
            location.coords.latitude,
            location.coords.longitude,
          ),
        ).catch(() => {
          this.locationNotAvailable = true;
          return {
            lat: '47.276541',
            lon: '8.448084',
            display_name: 'Affoltern a.A.',
            address: {
              postcode: '8912',
              village: 'Affoltern a.A.',
              state: 'Zurich',
              country: 'CH'
            },
          };
        }));
    await this.setLocation(this.currentLocation, false);

    await this.patchUrl();
    await this.setCurrentDate(this.currentDateRef)

    this.loading = false;

    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  private fillCalendar(date: Dayjs) {
    this.years = {};
    const ref = date.set('date', 1);

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
    this.changeMonth(this.currentDateRef.add(value, unit));
  }

  private initGeolocation() {
    return new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject);
    });
  }

  getEvents(day: Dayjs): number[] {
    const freq = this.eventsOfMonth.find((event) =>
      day.isSame(event.group, 'day'),
    );
    return freq ? times(freq.count) : [];
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

  private fetchEventDetails(day: Dayjs) {
    return this.documentService.findAllByRepositoryId({
      cursor: {
        page: 0,
        pageSize: 30,
      },
      where: {
        repository: {
          id: this.repositoryId,
        },
        localized: {
          near: {
            lat: this.currentLatLon[0],
            lon: this.currentLatLon[1],
          },
          distanceKm: this.distanceFc.value,
        },
        startedAt: {
          after: day
            .clone()
            .set('hours', 0)
            .set('minutes', 0)
            .set('seconds', 0)
            .valueOf(),
          before: day
            .clone()
            .set('hours', 24)
            .set('minutes', 0)
            .set('seconds', 0)
            .valueOf(),
        },
      },
    });
  }
  private async fetchEventOverview() {
    this.eventsOfMonth = await this.apollo
      .query<GqlFindEventsQuery, GqlFindEventsQueryVariables>({
        query: FindEvents,
        variables: {
          where: {
            repository: {
              id: this.repositoryId,
            },
            localized: {
              near: {
                lat: this.currentLatLon[0],
                lon: this.currentLatLon[1],
              },
              distanceKm: this.distanceFc.value,
            },
            startedAt: {
              after: this.timeWindowFrom,
              before: this.timeWindowTo,
            },
          },
        },
      })
      .then((response) => {
        return response.data.webDocumentsFrequency;
      });
  }

  async changeMonth(date: Dayjs, triggerUrlUpdate = true) {
    const diff = date.diff(this.now, 'months');
    if (diff < -3 || diff > 10) {
      return;
    }

    this.currentDateRef = date;

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

  private getGeoDistance(event: WebDocument): number {
    return this.getDistanceFromLatLonInKm(
      event.localized.lat,
      event.localized.lon,
      this.currentLatLon[0],
      this.currentLatLon[1],
    );
  }

  async setLocation(location: OsmMatch, triggerUrlUpdate = true) {
    this.isLocationFocussed = false;
    this.currentLocation = location;
    this.currentLatLon = [parseFloat(location.lat), parseFloat(location.lon)];
    this.locationFc.setValue(this.getDisplayName(location), {
      emitEvent: false,
    });

    if (triggerUrlUpdate) {
      this.patchUrl();
    }

    this.currentLatLon = [parseFloat(location.lat), parseFloat(location.lon)];
    this.locationSuggestions = [];
    await this.fetchEventOverview();
    this.changeRef.detectChanges();
  }

  getDisplayName(location: OsmMatch): string {
    const fields: (keyof OsmMatch['address'])[] = [
      'country', 'country_code', 'ISO3166-2-lvl4', 'postcode', 'state', 'amenity', 'house_number', 'road', 'county', 'neighbourhood', 'city_district'
    ];
    return compact(Object.values(omit(location.address, ...fields))).join(' ');
  }

  getCurrentLocation() {}

  async setCurrentDate(day: Dayjs) {
    this.currentDateRef = day;
    await this.patchUrl();
    const events = await this.fetchEventDetails(day.clone());
    const places = await Promise.all(unionBy(events.map(e => e.localized), e => `${e.lat},${e.lon}`)
      .map(latLon => this.openStreetMapService.reverseSearch(latLon.lat, latLon.lon).then(match => ({ latLon, place: this.getDisplayName(match) }))));

    const groups = events.reduce((agg, event) => {
      const distance = this.getGeoDistance(event).toFixed(0);
      if (!agg[distance]) {
        agg[distance] = [];
      }

      agg[distance].push(event);

      return agg;
    }, {} as Distance2Events);

    this.placesByDistance = sortBy(
      Object.keys(groups).map((distance) => ({
        distance,
        place: null,
        events: groups[distance],
      })),
      (event) => parseInt(event.distance),
    ).reduce((groupedPlaces, eventGroup: EventsByDistance) => {

      const latLonGroups = groupBy(eventGroup.events, e => JSON.stringify(e.localized));
      groupedPlaces.push({
        distance: eventGroup.distance,
        places: Object.keys(latLonGroups).map(latLonGroup => {
          return {
            events: latLonGroups[latLonGroup],
            place: places.find(place => isEqual(place.latLon, latLonGroups[latLonGroup][0].localized) ).place
          }
        })
      })

      return groupedPlaces
    }, [] as PlaceByDistance[]);

    this.changeRef.detectChanges();
  }

  private async patchUrl() {
    const parts: UrlFragments = {
      state: this.currentLocation.address.state,
      country: this.currentLocation.address.country,
      place: this.getDisplayName(this.currentLocation),
      year: this.currentDateRef.format('YYYY'),
      month: this.currentDateRef.format('MM'),
      day: this.currentDateRef.format('DD')
    };
    const url = this.router.createUrlTree(['/events', parts.country, parts.state, parts.place, parts.year, parts.month, parts.day]).toString()
    this.location.replaceState(url)
  }

  private async parseDateFromUrl(): Promise<Dayjs> {
    const {year, month, day} = this.activatedRoute.snapshot.params;
    if (year && month && day) {
      return dayjs(`${year}/${month}/${day}`, "YYYY/MM/DD")
    }
    throw Error();
  }

  private async parseLocationFromUrl(): Promise<OsmMatch> {
    const {state, country, place} = this.activatedRoute.snapshot.params;
    if (state && country && place) {
      const results = await this.openStreetMapService.searchAddress(`${state} ${country} ${place}`)
      if (results.length > 0) {
        return results[0];
      }
      throw Error();
    }

    throw Error();
  }

  isSame(a: Dayjs, b: Dayjs, units: OpUnitType[]) {
    return units.every(unit => a.isSame(b, unit));
  }
}
