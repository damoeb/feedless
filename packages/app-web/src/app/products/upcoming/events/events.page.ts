import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
  viewChild,
} from '@angular/core';
import { AppConfigService } from '../../../services/app-config.service';
import dayjs, { Dayjs } from 'dayjs';
import { OpenStreetMapService } from '../../../services/open-street-map.service';
import { groupBy, sortBy, times, unionBy, uniqBy } from 'lodash-es';
import {
  BreadcrumbList,
  Event as SchemaEvent,
  Place as SchemaPlace,
  WebPage,
} from 'schema-dts';
import { getCachedLocations } from '../places';
import { PageService, PageTags } from '../../../services/page.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Location, NgClass } from '@angular/common';
import {
  parseDateFromUrl,
  parseLocationFromUrl,
  upcomingBaseRoute,
} from '../upcoming-product-routes';
import { Subscription } from 'rxjs';
import 'dayjs/locale/de';
import { addIcons } from 'ionicons';
import {
  arrowBackOutline,
  arrowForwardOutline,
  sendOutline,
} from 'ionicons/icons';
import { isDefined, LatLng, NamedLatLon, Nullable } from '../../../types';
import { UpcomingHeaderComponent } from '../upcoming-header/upcoming-header.component';
import {
  IonBadge,
  IonButton,
  IonContent,
  IonIcon,
  IonNote,
  IonSpinner,
  IonText,
} from '@ionic/angular/standalone';
import { UpcomingFooterComponent } from '../upcoming-footer/upcoming-footer.component';
import { EventService, LocalizedEvent } from '../event.service';

type Distance2Events = { [distance: string]: LocalizedEvent[] };
type EventsByDistance = {
  distance: string;
  events: LocalizedEvent[];
};

type PlaceByDistance = {
  distance: number;
  places: EventsAtPlace[];
};

type EventsAtPlace = {
  place: NamedLatLon;
  events: LocalizedEvent[];
};

function roundLatLon(v: number): number {
  return Math.round(v * 1000) / 1000;
}

export function createBreadcrumbsSchema(location: NamedLatLon): BreadcrumbList {
  const country = upcomingBaseRoute({}).events({}).countryCode({
    countryCode: location.countryCode,
  });
  const region = country.region({
    region: location.area,
  });
  const place = region.place({
    place: location.place,
  });
  return {
    '@type': 'BreadcrumbList',
    itemListElement: [
      {
        '@type': 'ListItem',
        position: 1,
        item: {
          '@id': `https://lokale.events/${country.$}`,
          name: `Events in ${location.countryCode}`,
        },
      },
      {
        '@type': 'ListItem',
        position: 2,
        item: {
          '@id': `https://lokale.events/${region.$}`,
          name: `Events in ${location.area}, ${location.countryCode}`,
        },
      },
      {
        '@type': 'ListItem',
        position: 3,
        item: {
          '@id': `https://lokale.events/${place.$}`,
          name: `Events in ${location.displayName}`,
        },
      },
    ],
  };
}

type DateWindowItem = {
  date: Dayjs;
  offset: number;
};

interface EventGroupsPerDay {
  date: Dayjs;
  eventGroups: PlaceByDistance[];
}

@Component({
  selector: 'app-events-page',
  templateUrl: './events.page.html',
  styleUrls: ['./events.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    UpcomingHeaderComponent,
    IonContent,
    IonButton,
    IonIcon,
    NgClass,
    IonBadge,
    IonSpinner,
    RouterLink,
    IonNote,
    UpcomingFooterComponent,
    IonText,
  ],
  standalone: true,
})
export class EventsPage implements OnInit, OnDestroy {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly eventService = inject(EventService);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly locationService = inject(Location);
  private readonly pageService = inject(PageService);
  private readonly openStreetMapService = inject(OpenStreetMapService);
  private readonly appConfigService = inject(AppConfigService);
  private readonly router = inject(Router);

  date: Dayjs = dayjs();
  now: Dayjs = dayjs();
  perimeter: number = 10;
  latLon: Nullable<LatLng>;
  location: Nullable<NamedLatLon>;
  loading: boolean = true;
  dateWindow: DateWindowItem[] = [];
  private subscriptions: Subscription[] = [];

  readonly headerComponent = viewChild<UpcomingHeaderComponent>('header');

  protected placesByDistancePerDay: EventGroupsPerDay[] = [];
  protected loadingDay = true;

  constructor() {
    addIcons({ arrowBackOutline, arrowForwardOutline, sendOutline });
  }

  async ngOnInit(): Promise<void> {
    this.pageService.setMetaTags(this.getPageTags());

    // const fgl: PlacesLatLon[] = [];
    // for (let index in sg) {
    //   try {
    //     const place = sg[index];
    //     const matches = await this.openStreetMapService.searchByQuery(
    //       `Schweiz ${place.area} ${place.place}`,
    //     );
    //     console.log(matches[0]);
    //     fgl.push({
    //       lat: matches[0].lat,
    //       lng: matches[0].lng,
    //       language: 'de',
    //       place: place.place,
    //       zip: matches[0].zip,
    //       area: place.area,
    //     });
    //   } catch (e) {
    //     console.error(e);
    //     fgl.push(sg[index]);
    //   }
    // }
    //
    // console.log(JSON.stringify(fgl, null, 2));

    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        try {
          this.location = await parseLocationFromUrl(
            this.activatedRoute,
            this.openStreetMapService,
          );
          this.saveLocation(this.location);

          this.latLon = this.location;

          const { perimeter } =
            upcomingBaseRoute.children.events.children.countryCode.children.region.children.place.children.dateTime.parseParams(
              params as any,
            );

          this.perimeter = perimeter || 10;
          await this.changeDate(parseDateFromUrl(params));

          this.changeRef.detectChanges();

          this.pageService.setMetaTags(this.getPageTags());

          await this.fetchEvents(this.date);
        } catch (e) {
          // todo save and retrieve last place window.localStorage.getItem('lastPlace')
          // const currentLocation = await firstValueFrom(
          //   this.geoService.getCurrentLatLon(),
          // );

          await this.headerComponent().fetchSuggestions('');
        } finally {
          this.loading = false;
        }
        this.changeRef.detectChanges();
      }),
    );

    if (
      !this.location &&
      Object.keys(this.activatedRoute.snapshot.params).length === 0
    ) {
      const savedLocations: NamedLatLon[] = this.getSavedLocations();
      if (savedLocations.length > 0) {
        await this.router.navigateByUrl(
          this.getDateUrl(dayjs(), savedLocations[0]),
        );
      }
    }
  }

  formatDate(date: Dayjs, format: string) {
    return date?.locale('de')?.format(format);
  }

  private getPageTags(): PageTags {
    const location = this.location;
    const dateStr = this.date ? this.formatDate(this.date, 'DD.MM.YYYY') : '';

    if (location) {
      const keywords = [
        'Events',
        'Veranstaltungen',
        location.displayName,
        location.area,
        location.countryCode,
        'lokale Events',
        'Aktivitäten',
        'Familien',
        'Sport',
        'Kultur',
        'Freizeit',
        dateStr,
      ].filter(Boolean);

      return {
        title: `Events in ${location.displayName}, ${location.area} | lokale.events`,
        description: `Entdecke aktuelle Veranstaltungen in ${location.displayName}, ${location.area}${dateStr ? ' am ' + dateStr : ''}. Von Familien-Events über Sport-Aktivitäten bis hin zu kulturellen Veranstaltungen und Märkten - finde spannende Events in deiner Nähe.`,
        publisher: 'lokale.events',
        category: 'Events',
        url: document.location.href,
        region: location.area,
        place: location.displayName,
        lang: 'de',
        publishedAt: dayjs(),
        position: location,
        keywords,
        author: 'lokale.events Team',
        robots: 'index, follow',
        canonicalUrl: `https://lokale.events${this.createUrl(location, this.date)}`,
      };
    } else {
      return {
        title: `lokale.events | Entdecke Events und Veranstaltungen in deiner Umgebung`,
        description: `Finde spannende lokale Veranstaltungen in deiner Nähe. Von Familien-Events über Sport-Aktivitäten bis hin zu kulturellen Veranstaltungen und Märkten - entdecke was deine Region zu bieten hat.`,
        publisher: 'lokale.events',
        category: 'Events',
        url: document.location.href,
        lang: 'de',
        publishedAt: dayjs(),
        keywords: [
          'Events',
          'Veranstaltungen',
          'lokale Events',
          'Aktivitäten',
          'Familien',
          'Sport',
          'Kultur',
          'Freizeit',
        ],
        author: 'lokale.events Team',
        robots: 'index, follow',
        canonicalUrl: 'https://lokale.events/',
      };
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
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

  private deg2rad(deg: number) {
    return deg * (Math.PI / 180);
  }

  private fetchEventsBetweenDates(
    minDate: Dayjs,
    maxDate: Dayjs,
  ): Promise<LocalizedEvent[]> {
    return this.eventService.findAllByRepositoryId({
      cursor: {
        page: 0,
        pageSize: 50,
      },
      where: {
        repository: {
          id: this.getRepositoryId(),
        },
        latLng: {
          near: {
            point: {
              lat: this.latLon.lat,
              lng: this.latLon.lng,
            },
            distanceKm: this.perimeter,
          },
        },
        startedAt: {
          after: minDate
            .clone()
            .set('hours', 0)
            .set('minutes', 0)
            .set('seconds', 0)
            .valueOf(),
          before: maxDate
            .clone()
            .set('hours', 24)
            .set('minutes', 0)
            .set('seconds', 0)
            .valueOf(),
        },
      },
    });
  }

  private async fetchEvents(date: Dayjs) {
    try {
      this.loadingDay = true;
      this.placesByDistancePerDay = [];
      this.changeRef.detectChanges();

      const minDate = date;
      const maxDate = minDate.add(2, 'days');

      const toIsoString = (date: Dayjs): string =>
        date.startOf('day').toISOString();

      const daysInWindow = times(maxDate.diff(minDate, 'days') + 1).map(
        (offset) => toIsoString(minDate.add(offset, 'days')),
      );

      const events = await this.fetchEventsBetweenDates(minDate, maxDate);

      const places: NamedLatLon[] = await this.resolvePlaces(events).then(
        (places) => places.filter((place) => isDefined(place)),
      );

      const eventsPerDay = groupBy(events, (event: LocalizedEvent) =>
        toIsoString(dayjs(event.startingAt)),
      );

      this.placesByDistancePerDay = daysInWindow.map<EventGroupsPerDay>(
        (dateKey) => {
          return {
            date: dayjs(dateKey),
            eventGroups: this.getPlacesByDistance(
              eventsPerDay[dateKey] ?? [],
              places,
            ),
          };
        },
      );

      this.pageService.setJsonLdData(this.createWebsiteSchema());
    } catch (e) {
      console.error(e);
    } finally {
      this.loadingDay = false;
    }
    this.changeRef.detectChanges();
  }

  private async resolvePlaces(
    events: LocalizedEvent[],
  ): Promise<(NamedLatLon | null)[]> {
    return Promise.all(
      unionBy(
        events.map((e) => e.latLng),
        (e) => `${e.lat},${e.lng}`,
      )
        .filter((e) => e)
        .map((latLon) => {
          const namedPlace = getCachedLocations().find(
            (place) =>
              roundLatLon(place.lat) == roundLatLon(latLon.lat) &&
              roundLatLon(place.lng) == roundLatLon(latLon.lng),
          );
          if (namedPlace) {
            return namedPlace;
          } else {
            console.log('Cannot resolve', latLon);
            return this.openStreetMapService
              .reverseSearch(latLon.lat, latLon.lng)
              .catch((): null => null);
          }
        }),
    );
  }

  private getPlacesByDistance(
    events: LocalizedEvent[],
    places: NamedLatLon[],
  ): PlaceByDistance[] {
    const groups = events.reduce((agg, event) => {
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
        places: [] as string[],
        events: groups[distance],
      })),
      (event) => parseInt(event.distance),
    ).reduce((groupedPlaces, eventGroup: EventsByDistance) => {
      const latLonGroups = groupBy(eventGroup.events, (e) =>
        JSON.stringify(e.latLng),
      );
      groupedPlaces.push({
        distance: parseInt(eventGroup.distance),
        places: Object.keys(latLonGroups).map((latLonGroup) => {
          const latLon = latLonGroups[latLonGroup][0].latLng;
          const fallbackPlace: NamedLatLon = {
            lat: latLon.lat,
            lng: latLon.lng,
            place: '',
            displayName: 'Unbekannt',
            area: '',
            countryCode: '',
          };
          const place: NamedLatLon =
            places.find(
              (place) =>
                roundLatLon(place.lat) == roundLatLon(latLon.lat) &&
                roundLatLon(place.lng) == roundLatLon(latLon.lng),
            ) ?? fallbackPlace;
          if (!place) {
            console.warn(`Cannot resolve latlon` + JSON.stringify(latLon));
          }
          return {
            events: latLonGroups[latLonGroup],
            place: place,
          };
        }),
      });

      return groupedPlaces;
    }, [] as PlaceByDistance[]);
  }

  private getGeoDistance(event: LocalizedEvent): number {
    return this.getDistanceFromLatLonInKm(
      event.latLng.lat,
      event.latLng.lng,
      this.latLon.lat,
      this.latLon.lng,
    );
  }

  private getRepositoryId(): string {
    return this.appConfigService.customProperties.eventRepositoryId as any;
  }

  createWebsiteSchema(): WebPage {
    const tags = this.getPageTags();
    const events = this.placesByDistancePerDay[0].eventGroups
      .flatMap((distanced) => distanced.places)
      .flatMap((place) =>
        place.events.map((event) => this.toSchemaOrgEvent(event, place.place)),
      )
      .filter((event) => event);

    return {
      '@type': 'WebPage',
      name: tags.title,
      description: tags.description,
      datePublished: tags.publishedAt.toISOString(),
      url: location.href,
      inLanguage: 'de-DE',
      about: {
        '@type': 'Thing',
        name: `Events in ${this.location?.displayName || 'deiner Nähe'}`,
        description: tags.description,
      },
      breadcrumb: createBreadcrumbsSchema(this.location),
      mainEntity: {
        '@type': 'ItemList',
        name: `Events in ${this.location?.displayName || 'deiner Nähe'}`,
        description: `Liste der aktuellen Veranstaltungen${this.date ? ' am ' + this.formatDate(this.date, 'DD.MM.YYYY') : ''}`,
        itemListElement: events.map((event, index) => ({
          '@type': 'ListItem',
          position: index + 1,
          item: event,
        })),
      },
      publisher: {
        '@type': 'Organization',
        name: 'lokale.events',
        url: 'https://lokale.events',
      },
    };
  }

  private toSchemaOrgEvent(
    event: LocalizedEvent,
    location: NamedLatLon,
  ): SchemaEvent {
    const startDate = dayjs(event.startingAt);
    return {
      '@type': 'Event',
      name: event.title,
      description: event.text || `Veranstaltung in ${location.displayName}`,
      eventStatus: 'EventScheduled',
      eventAttendanceMode: 'OfflineEventAttendanceMode',
      startDate: startDate.toISOString(),
      url: event.url,
      location: {
        '@type': 'Place',
        name: location.displayName,
        address: {
          '@type': 'PostalAddress',
          addressLocality: location.place,
          addressRegion: location.area,
          addressCountry: location.countryCode,
        },
        geo: {
          '@type': 'GeoCoordinates',
          latitude: location.lat,
          longitude: location.lng,
        },
      },
      organizer: {
        '@type': 'Organization',
        name: 'lokale.events',
        url: 'https://lokale.events',
      },
    };
  }

  getDateUrl(date: Nullable<Dayjs>, location: Nullable<NamedLatLon> = null) {
    const { countryCode, region, place } = this.getLocationOrElse(location);
    const { year, month, day } = this.getDateOrElse(date);

    return (
      '/' +
      upcomingBaseRoute({})
        .events({})
        .countryCode({ countryCode })
        .region({ region })
        .place({ place })
        .dateTime({
          perimeter: this.perimeter,
          year,
          month,
          day,
        })
    );
  }

  // getEventUrl(event: LocalizedEvent) {
  //   const { countryCode, region, place, year, month, day } =
  //     this.activatedRoute.snapshot.params;
  //
  //   return (
  //     '/' +
  //     homeRoute({})
  //       .events({})
  //       .countryCode({ countryCode })
  //       .region({ region })
  //       .place({ place })
  //       .dateTime({
  //         perimeter: this.perimeter,
  //         year,
  //         month,
  //         day,
  //       })
  //       .eventId({ eventId: event.id }).$
  //   );
  // }

  private toSchemaOrgPlace(place: EventsAtPlace): SchemaPlace {
    return {
      '@type': 'Place',
      name: place.place.place,
      geo: {
        '@type': 'GeoCoordinates',
        latitude: place.place.lat,
        longitude: place.place.lng,
      },
      event: place.events.map((event) =>
        this.toSchemaOrgEvent(event, place.place),
      ),
    };
  }

  getPlaceUrl(location: NamedLatLon): string {
    if (location) {
      const { countryCode, area, place } = location;
      const { year, month, day } =
        upcomingBaseRoute.children.events.children.countryCode.children.region.children.place.children.dateTime.parseParams(
          this.activatedRoute.snapshot.params as any,
        );
      return (
        '/' +
        upcomingBaseRoute({})
          .events({})
          .countryCode({ countryCode })
          .region({ region: area })
          .place({ place })
          .dateTime({
            perimeter: this.perimeter,
            year,
            month,
            day,
          }).$
      );
    }
  }

  async changeDate(date: Dayjs) {
    this.date = date;
    this.createDateWindow(this.date);
    this.patchUrlInAddressBar();
    await this.fetchEvents(this.date);
  }

  patchUrlInAddressBar() {
    this.locationService.replaceState(this.createUrl(this.location, this.date));
  }

  createUrl(location: NamedLatLon, date: Dayjs): string {
    return upcomingBaseRoute({})
      .events({})
      .countryCode({ countryCode: location.countryCode })
      .region({ region: location.area })
      .place({ place: location.place })
      .dateTime({
        perimeter: 10,
        year: parseInt(date.format('YYYY')),
        month: parseInt(date.format('MM')),
        day: parseInt(date.format('DD')),
      }).$;
  }

  private saveLocation(location: Nullable<NamedLatLon>) {
    const savedLocations: NamedLatLon[] = this.getSavedLocations();
    const locations = uniqBy(
      [location, ...savedLocations],
      (l) => `${l.lat}:${l.lng}`,
    ).filter((_, index) => index < 4);
    localStorage.setItem('savedLocations', JSON.stringify(locations));
  }

  private getSavedLocations(): NamedLatLon[] {
    return JSON.parse(localStorage.getItem('savedLocations') || '[]');
  }

  private createDateWindow(dateP: Dayjs) {
    const date = this.coerceDate(
      dateP,
      dayjs().subtract(2, 'week'),
      dayjs().add(2, 'month'),
    );
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

  isPast(day: Dayjs): boolean {
    return day.isBefore(
      dayjs()
        .set('hours', 0)
        .set('minutes', 0)
        .set('seconds', 0)
        .set('milliseconds', 0),
    );
  }

  getWeekday(date: Dayjs): string {
    if (date) {
      const now = dayjs()
        .set('hours', date.hour())
        .set('minutes', date.minute())
        .set('seconds', date.second())
        .set('milliseconds', date.millisecond());
      const diffInHours = date.diff(now, 'day');
      return ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'][date.day()];
    }
  }

  getRelativeDateLabel(date: Dayjs): string {
    if (date) {
      const now = dayjs().startOf('day');
      const diffInDays = date.startOf('day').diff(now, 'day');
      // const diffInWeeks = date.diff(now, 'week');
      if (Math.abs(diffInDays) < 7) {
        const prefix = diffInDays < 0 ? 'vor' : 'in';
        switch (diffInDays) {
          case 0:
            return 'Heute';
          case -1:
            return 'Gestern';
          case 1:
            return 'Morgen';
          default:
            return `${prefix} ${diffInDays} Tagen`;
        }
      }
    }
    return '';
  }

  protected isSame(a: dayjs.Dayjs, b: dayjs.Dayjs, units: dayjs.OpUnitType[]) {
    return units.every((unit) => a.isSame(b, unit));
  }

  moveCalendarWindow(days: number) {
    this.createDateWindow(this.dateWindow[0].date.add(days, 'days'));
  }

  isDateInCalendar(date: Dayjs) {
    return this.dateWindow.some((d) =>
      this.isSame(date, d.date, ['year', 'month', 'day']),
    );
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

  private getDateOrElse(date: Dayjs): {
    year: number;
    month: number;
    day: number;
  } {
    if (date) {
      return {
        year: parseInt(date.format('YYYY')),
        month: parseInt(date.format('MM')),
        day: parseInt(date.format('DD')),
      };
    } else {
      return this.activatedRoute.snapshot.params as any;
    }
  }

  private getLocationOrElse(location: NamedLatLon): {
    countryCode: string;
    region: string;
    place: string;
  } {
    if (location) {
      return {
        countryCode: location.countryCode,
        region: location.area,
        place: location.place,
      };
    } else {
      return this.activatedRoute.snapshot.params as any;
    }
  }

  cleanTitle(title: string) {
    return title.replaceAll(/[0-9]{1,2}\.[ ]?[a-z]{3,10}[ ]?[0-9]{2,4}/gi, '');
  }

  isToday(date: Dayjs): boolean {
    return date.startOf('day').diff(dayjs().startOf('day'), 'hour') === 0;
  }
}

export function getPreviousLocations(): NamedLatLon[] {
  return JSON.parse(localStorage.getItem('savedLocations') || '[]');
}
