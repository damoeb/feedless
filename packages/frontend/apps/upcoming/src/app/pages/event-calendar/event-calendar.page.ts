import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
  viewChild,
} from '@angular/core';
// eslint-disable-next-line @nx/enforce-module-boundaries
import {
  AppConfigService,
  PageService,
  PageTags,
  RecordService,
} from '@feedless/components';
import dayjs, { Dayjs } from 'dayjs';
import { groupBy, sortBy, times, unionBy, uniqBy } from 'lodash-es';
import { BreadcrumbList, Event as SchemaEvent, WebPage } from 'schema-dts';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { isPlatformBrowser, Location, NgClass } from '@angular/common';
import {
  EventsResolverData,
  parseDateFromQuery,
  DateLink,
  parseLocationFromUrl,
  renderEventUrl,
  renderDateLink,
  renderDateUrl,
  renderPlaceUrl,
} from '../../upcoming-product-routes';
import { combineLatest, Subscription } from 'rxjs';
import 'dayjs/locale/de';
import { addIcons } from 'ionicons';
import {
  arrowBackOutline,
  arrowForwardOutline,
  sendOutline,
} from 'ionicons/icons';
import { isDefined, NamedLatLon, Nullable } from '@feedless/core';
import { cleanEventTitle } from '../../event-title';
import { EVENT_DETAIL_PAGES_ENABLED } from '../../feature-flags';
import { UpcomingHeaderComponent } from '../../components/upcoming-header/upcoming-header.component';
import {
  IonChip,
  IonContent,
  IonLabel,
  IonList,
  IonListHeader,
  IonSpinner,
  IonText,
} from '@ionic/angular/standalone';
import { UpcomingFooterComponent } from '../../components/upcoming-footer/upcoming-footer.component';
import { EventService, LocalizedEvent } from '../../event.service';
import { InlineCalendarComponent } from '../../components/inline-calendar/inline-calendar.component';
import {
  AdminGeoService,
  getCachedLocations,
  OpenStreetMapService,
} from '@feedless/geo';
import { PageSidebarComponent } from '../../components/page-sidebar/page-sidebar.component';
import { SearchAboButtonComponent } from '../../components/search-abo-button/search-abo-button.component';

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
  const base = 'https://lokale.events';
  const countryPath = `${base}/events/in/${encodeURIComponent(location.countryCode)}`;
  const regionPath = `${countryPath}/${encodeURIComponent(location.area)}`;
  const placePath = `${regionPath}/${encodeURIComponent(location.place)}`;
  return {
    '@type': 'BreadcrumbList',
    itemListElement: [
      {
        '@type': 'ListItem',
        position: 1,
        item: {
          '@id': countryPath,
          name: `Events in ${location.countryCode}`,
        },
      },
      {
        '@type': 'ListItem',
        position: 2,
        item: {
          '@id': regionPath,
          name: `Events in ${location.area}, ${location.countryCode}`,
        },
      },
      {
        '@type': 'ListItem',
        position: 3,
        item: {
          '@id': placePath,
          name: `Events in ${location.displayName}`,
        },
      },
    ],
  };
}

export type DateWindowItem = {
  date: Dayjs;
  offset: number;
};

interface EventGroupsPerDay {
  date: Dayjs;
  eventGroups: PlaceByDistance[];
}

export function getDateConstraints(date: Dayjs): {
  minDate: Dayjs;
  maxDate: Dayjs;
} {
  const minDate = date;
  return {
    minDate,
    maxDate: date.add(2, 'days'),
  };
}

@Component({
  selector: 'app-events-page',
  templateUrl: './event-calendar.page.html',
  styleUrls: ['./event-calendar.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    UpcomingHeaderComponent,
    IonContent,
    NgClass,
    IonSpinner,
    RouterLink,
    UpcomingFooterComponent,
    IonText,
    InlineCalendarComponent,
    IonChip,
    PageSidebarComponent,
    IonList,
    IonLabel,
    IonListHeader,
    SearchAboButtonComponent,
  ],
  standalone: true,
})
export class EventCalendarPage implements OnInit, OnDestroy {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly eventService = inject(EventService);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly locationService = inject(Location);
  private readonly pageService = inject(PageService);
  private readonly openStreetMapService = inject(OpenStreetMapService);
  private readonly adminGeoService = inject(AdminGeoService);
  private readonly appConfigService = inject(AppConfigService);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly recordService = inject(RecordService);
  protected isBrowser = isPlatformBrowser(this.platformId);
  protected eventDetailsEnabled = EVENT_DETAIL_PAGES_ENABLED;

  date: Dayjs = dayjs();
  readonly now: Dayjs = dayjs();
  readonly minDate: Dayjs = dayjs().subtract(2, 'week');
  readonly maxDate: Dayjs = dayjs().add(2, 'month');
  perimeter = 10;
  namedLatLon: Nullable<NamedLatLon>;
  loading = true;
  private subscriptions: Subscription[] = [];

  readonly headerComponent = viewChild<UpcomingHeaderComponent>('header');

  placesByDistancePerDay: EventGroupsPerDay[] = [];
  loadingDay = true;

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ arrowBackOutline, arrowForwardOutline, sendOutline });
    }
  }

  async ngOnInit(): Promise<void> {
    this.pageService.setMetaTags(this.getPageTags());

    if (isPlatformBrowser(this.platformId)) {
      this.subscriptions.push(
        combineLatest([
          this.activatedRoute.params,
          this.activatedRoute.queryParams,
        ]).subscribe(async ([, queryParams]) => {
          try {
            this.namedLatLon = await parseLocationFromUrl(
              this.activatedRoute.snapshot,
              this.adminGeoService,
            );
            this.saveLocation(this.namedLatLon);

            this.perimeter = 10;

            const { date } = parseDateFromQuery(queryParams);
            await this.changeDate(date);

            this.changeRef.detectChanges();

            this.pageService.setMetaTags(this.getPageTags());
          } catch (e) {
            if (this.headerComponent()) {
              await this.headerComponent().fetchSuggestions('');
            }
          } finally {
            this.loading = false;
          }
          this.changeRef.detectChanges();
        }),
      );
    } else {
      const data = this.activatedRoute.snapshot.data[
        'events'
      ] as EventsResolverData;

      if (data) {
        this.namedLatLon = data?.latlng;
        this.date = data?.date;
        await this.handleEventsResponse(data.events);
      }
      this.pageService.setMetaTags(this.getPageTags());
    }

    if (
      !this.namedLatLon &&
      Object.keys(this.activatedRoute.snapshot.params).length === 0 &&
      isPlatformBrowser(this.platformId)
    ) {
      const savedLocations: NamedLatLon[] = this.getSavedLocations();
      if (
        savedLocations.length > 0 &&
        sessionStorage.getItem('visited') == null
      ) {
        sessionStorage.setItem('visited', 'true');
        await this.router.navigateByUrl(
          this.createDateUrl(dayjs(), savedLocations[0]),
        );
      }
    }
  }

  formatDate(date: Dayjs, format: string) {
    return date?.locale('de')?.format(format);
  }

  private getCanonicalUrlFromPath(): string {
    const path = this.locationService.path();
    if (!path) {
      return 'https://lokale.events/';
    }
    return `https://lokale.events${path.split('?')[0]}`;
  }

  private getPageTags(): PageTags {
    const location = this.namedLatLon;
    const queryParams = this.activatedRoute.snapshot.queryParams;
    const robots =
      queryParams['event'] || queryParams['date']
        ? 'noindex, follow'
        : 'index, follow';

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
      ];

      return {
        title: `Events in ${location.displayName}, ${location.area} | lokale.events`,
        description: `Entdecke aktuelle Veranstaltungen in ${location.displayName}, ${location.area}. Von Familien-Events über Sport-Aktivitäten bis hin zu kulturellen Veranstaltungen und Märkten - finde spannende Events in deiner Nähe.`,
        publisher: 'lokale.events',
        category: 'Events',
        url: this.getCurrentUrl(),
        region: location.area,
        place: location.displayName,
        lang: 'de-CH',
        locale: 'de_CH',
        publishedAt: dayjs(),
        position: location,
        keywords,
        expiresAt: this.date,
        author: 'lokale.events Team',
        robots,
        canonicalUrl: this.getCanonicalUrlFromPath(),
      };
    } else {
      return {
        title: `Veranstaltungen in deiner Nähe entdecken`,
        description: `Finde spannende lokale Veranstaltungen in deiner Nähe. Von Familien-Events über Sport-Aktivitäten bis hin zu kulturellen Veranstaltungen und Märkten - entdecke was deine Region zu bieten hat.`,
        publisher: 'lokale.events',
        category: 'Events',
        url: this.getCurrentUrl(),
        lang: 'de-CH',
        locale: 'de_CH',
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
        canonicalUrl: this.getCanonicalUrlFromPath(),
      };
    }
  }

  private getCurrentUrl(): string {
    if (isPlatformBrowser(this.platformId)) {
      return document.location.href;
    }
    return this.getCanonicalUrlFromPath();
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

  private async fetchEvents(date: Dayjs) {
    try {
      this.loadingDay = true;
      this.placesByDistancePerDay = [];
      this.changeRef.detectChanges();

      const events = await this.eventService.fetchEventsBetweenDates(
        date,
        this.getRepositoryId(),
        this.namedLatLon.lat,
        this.namedLatLon.lng,
      );
      return this.handleEventsResponse(events);
    } catch (e) {
      console.error(e);
    } finally {
      this.loadingDay = false;
    }
    this.changeRef.detectChanges();
  }

  private async handleEventsResponse(events: LocalizedEvent[]) {
    try {
      const { minDate, maxDate } = getDateConstraints(this.date);
      const toIsoString = (date: Dayjs): string =>
        date.startOf('day').toISOString();

      const daysInWindow = times(maxDate.diff(minDate, 'days') + 1).map(
        (offset) => toIsoString(minDate.add(offset, 'days')),
      );

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
      this.namedLatLon.lat,
      this.namedLatLon.lng,
    );
  }

  private getRepositoryId(): string {
    return this.appConfigService.customProperties['eventRepositoryId'] as any;
  }

  createWebsiteSchema(): WebPage {
    const tags = this.getPageTags();
    const events =
      this.placesByDistancePerDay?.flatMap((day) =>
        day.eventGroups.flatMap((distanced) =>
          distanced.places.flatMap((place) =>
            place.events.map((event) =>
              this.toSchemaOrgEvent(
                event,
                place.place,
                this.eventDetailsEnabled
                  ? `https://lokale.events${this.getEventUrl(event, place.place)}`
                  : event.url,
              ),
            ),
          ),
        ),
      ) ?? [];

    return {
      '@type': 'WebPage',
      name: tags.title,
      description: tags.description,
      datePublished: tags.publishedAt.toISOString(),
      temporalCoverage: `${this.date.subtract(2, 'months').toISOString()}/${this.date.add(1, 'week').toISOString()}`,
      url: tags.canonicalUrl ?? tags.url ?? 'https://lokale.events',
      inLanguage: 'de-DE',
      about: {
        '@type': 'Thing',
        name: `Events in ${this.namedLatLon?.displayName || 'deiner Nähe'}`,
        description: tags.description,
      },
      contentLocation: {
        '@type': 'Place',
        name: this.namedLatLon?.displayName,
        geo: {
          '@type': 'GeoCoordinates',
          latitude: this.namedLatLon?.lat,
          longitude: this.namedLatLon?.lng,
        },
      },
      breadcrumb: createBreadcrumbsSchema(this.namedLatLon),
      mainEntity: {
        '@type': 'ItemList',
        name: `Events in ${this.namedLatLon?.displayName || 'deiner Nähe'}`,
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
    eventPageUrl: string,
  ): SchemaEvent {
    const startDate = dayjs(event.startingAt);
    return {
      '@type': 'Event',
      name: event.title,
      description: event.text || `Veranstaltung in ${location.displayName}`,
      eventStatus: 'EventScheduled',
      eventAttendanceMode: 'OfflineEventAttendanceMode',
      // Reines Datum: 96 % der Events tragen 10:00, weil die Pipeline diesen
      // Wert stempelt, wenn sie keine Zeit erkennt. Eine Uhrzeit hier wäre
      // für fast jedes Event eine Falschangabe. Kein endDate aus demselben
      // Grund.
      startDate: startDate.format('YYYY-MM-DD'),
      url: eventPageUrl,
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

  createDateUrl(
    date: Nullable<Dayjs>,
    location: Nullable<NamedLatLon> = null,
  ): string {
    const { countryCode, region, place } = this.getLocationOrElse(location);
    return renderDateUrl(countryCode, region, place, date);
  }

  createDateLink(
    date: Nullable<Dayjs>,
    location: Nullable<NamedLatLon> = null,
  ): DateLink {
    const { countryCode, region, place } = this.getLocationOrElse(location);
    return renderDateLink(countryCode, region, place, date);
  }

  /**
   * Das Event hängt unter *seinem* Ort, nicht unter dem der aufgerufenen Seite.
   * Sonst wäre dasselbe Event unter jedem Nachbarort erreichbar und jede
   * Variante würde auf sich selbst kanonisieren.
   */
  getEventUrl(event: LocalizedEvent, place: NamedLatLon): string {
    if (!place?.countryCode || !place?.area || !place?.place) {
      return '';
    }
    return renderEventUrl(
      place.countryCode,
      place.area,
      place.place,
      event as unknown as { id: string; title?: string | null },
    );
  }

  getPlaceLink(location: NamedLatLon): DateLink {
    if (!location) {
      return { path: '', queryParams: {} };
    }
    return this.createDateLink(this.date, location);
  }

  // private toSchemaOrgPlace(place: EventsAtPlace): SchemaPlace {
  //   return {
  //     '@type': 'Place',
  //     name: place.place.place,
  //     geo: {
  //       '@type': 'GeoCoordinates',
  //       latitude: place.place.lat,
  //       longitude: place.place.lng,
  //     },
  //     event: place.events.map((event) =>
  //       this.toSchemaOrgEvent(event, place.place),
  //     ),
  //   };
  // }

  async changeDate(date: Dayjs) {
    this.date = date;
    // this.patchUrlInAddressBar();
    await this.fetchEvents(this.date);
    this.changeRef.detectChanges();
  }

  private saveLocation(location: Nullable<NamedLatLon>) {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    const savedLocations: NamedLatLon[] = this.getSavedLocations();
    const locations = uniqBy(
      [location, ...savedLocations],
      (l) => `${l.lat}:${l.lng}`,
    ).filter((_, index) => index < 4);
    localStorage.setItem('savedLocations', JSON.stringify(locations));
  }

  private getSavedLocations(): NamedLatLon[] {
    return getPreviousLocations(isPlatformBrowser(this.platformId));
  }

  isPast(day: Dayjs): boolean {
    return day.isBefore(dayjs().startOf('day'));
  }

  protected readonly getWeekday = getWeekday;
  protected collapsedDescription = true;

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

  toIsoString(startingAt: number): string {
    return dayjs(startingAt).toISOString();
  }

  /** Leerstring für Ganztages-Einträge, damit die Zeile dort entfällt. */
  formatTime(startingAt: number): string {
    const date = dayjs(startingAt);
    if (date.hour() === 0 && date.minute() === 0) {
      return '';
    }
    return date.locale('de').format('HH:mm');
  }

  cleanTitle(title: string) {
    return cleanEventTitle(title);
  }

  private async resolvePlaceFromLatLon(latLng: {
    lat: number;
    lng: number;
  }): Promise<NamedLatLon> {
    const named = getCachedLocations().find(
      (p) =>
        roundLatLon(p.lat) === roundLatLon(latLng.lat) &&
        roundLatLon(p.lng) === roundLatLon(latLng.lng),
    );
    if (named) {
      return named;
    }
    const result = await this.openStreetMapService
      .reverseSearch(latLng.lat, latLng.lng)
      .catch((): null => null);
    return (
      result ?? {
        lat: latLng.lat,
        lng: latLng.lng,
        place: '',
        displayName: 'Unbekannt',
        area: '',
        countryCode: '',
      }
    );
  }

  getDateLinkFactory() {
    return (date: Dayjs): DateLink => this.createDateLink(date);
  }
}

export function getPreviousLocations(isBrowser: boolean): NamedLatLon[] {
  if (isBrowser) {
    return JSON.parse(localStorage.getItem('savedLocations') || '[]');
  } else {
    return [];
  }
}

export function getWeekday(date: Dayjs): string {
  if (date) {
    const now = dayjs()
      .set('hours', date.hour())
      .set('minutes', date.minute())
      .set('seconds', date.second())
      .set('milliseconds', date.millisecond());
    const diffInHours = date.diff(now, 'day');
    return ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'][date.day()];
  }
  return '';
}

export function formatDate(date: Dayjs, format: string) {
  return date?.locale('de')?.format(format);
}
