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
  parseDateFromUrl,
  parseLocationFromUrl,
  RelativeDate,
  relativeDateIncrement,
  upcomingBaseRoute,
} from '../../upcoming-product-routes';
import { Subscription } from 'rxjs';
import 'dayjs/locale/de';
import { addIcons } from 'ionicons';
import {
  arrowBackOutline,
  arrowForwardOutline,
  sendOutline,
} from 'ionicons/icons';
import { isDefined, LatLng, NamedLatLon, Nullable } from '@feedless/core';
import { UpcomingHeaderComponent } from '../../components/upcoming-header/upcoming-header.component';
import {
  IonChip,
  IonContent,
  IonFooter,
  IonLabel,
  IonList,
  IonListHeader,
  IonSpinner,
  IonText,
  ModalController,
} from '@ionic/angular/standalone';
import { UpcomingFooterComponent } from '../../components/upcoming-footer/upcoming-footer.component';
import { EventService, LocalizedEvent } from '../../event.service';
import { InlineCalendarComponent } from '../../components/inline-calendar/inline-calendar.component';
import { renderPath } from 'typesafe-routes';
import { getCachedLocations, OpenStreetMapService } from '@feedless/geo';
import { EventDetailModalComponent } from '../../components/event-detail-modal/event-detail-modal.component';
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
  // todo fix
  // const country = parsePath(upcomingBaseRoute.events.countryCode.region.({}).countryCode({
  //   countryCode: location.countryCode,
  // });
  // const region = country.region({
  //   region: location.area,
  // });
  // const place = region.place({
  //   place: location.place,
  // });
  return {
    '@type': 'BreadcrumbList',
    itemListElement: [
      // {
      //   '@type': 'ListItem',
      //   position: 1,
      //   item: {
      //     '@id': `https://lokale.events/${country.$}`,
      //     name: `Events in ${location.countryCode}`,
      //   },
      // },
      // {
      //   '@type': 'ListItem',
      //   position: 2,
      //   item: {
      //     '@id': `https://lokale.events/${region.$}`,
      //     name: `Events in ${location.area}, ${location.countryCode}`,
      //   },
      // },
      // {
      //   '@type': 'ListItem',
      //   position: 3,
      //   item: {
      //     '@id': `https://lokale.events/${place.$}`,
      //     name: `Events in ${location.displayName}`,
      //   },
      // },
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

@Component({
  selector: 'app-events-page',
  templateUrl: './events.page.html',
  styleUrls: ['./events.page.scss'],
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
    IonFooter,
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
  private readonly platformId = inject(PLATFORM_ID);
  private readonly modalCtrl = inject(ModalController);
  private readonly recordService = inject(RecordService);
  protected isBrowser = isPlatformBrowser(this.platformId);

  date: Dayjs = dayjs();
  readonly now: Dayjs = dayjs();
  readonly minDate: Dayjs = dayjs().subtract(2, 'week');
  readonly maxDate: Dayjs = dayjs().add(2, 'month');
  perimeter = 10;
  latLon: Nullable<LatLng>;
  namedLatLon: Nullable<NamedLatLon>;
  loading = true;
  dateIsFromRelativeUrl = false;
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

    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        try {
          this.namedLatLon = await parseLocationFromUrl(
            this.activatedRoute,
            this.openStreetMapService,
          );
          this.saveLocation(this.namedLatLon);

          this.latLon = this.namedLatLon;

          // Try to parse perimeter from absolute date route

          this.perimeter = 10;

          // Check if date is present in URL
          const hasDateInUrl =
            params['year'] ||
            params['month'] ||
            params['day'] ||
            params['relativeDate'];

          const { date: dateFromUrl } = parseDateFromUrl(params);
          this.dateIsFromRelativeUrl = !!params['relativeDate'];

          if (hasDateInUrl) {
            // Only validate and redirect if date was explicitly in URL
            // if (
            //   dateFromUrl.isBefore(this.minDate) ||
            //   dateFromUrl.isAfter(this.maxDate)
            // ) {
            //   await this.redirectToToday();
            // } else {
            await this.changeDate(dateFromUrl);
            // }
          } else {
            // No date in URL - just show today without redirecting
            this.date = dateFromUrl;
            await this.fetchEvents(this.date);
          }

          this.changeRef.detectChanges();

          this.pageService.setMetaTags(this.getPageTags());

          await this.fetchEvents(this.date);

          const eventId = params['eventId'];
          if (eventId != null) {
            await this.openEventModalForId(String(eventId));
          }
        } catch (e) {
          // todo save and retrieve last place window.localStorage.getItem('lastPlace')
          // const currentLocation = await firstValueFrom(
          //   this.geoService.getCurrentLatLon(),
          // );

          if (this.headerComponent()) {
            await this.headerComponent().fetchSuggestions('');
          }
        } finally {
          this.loading = false;
        }
        this.changeRef.detectChanges();
      }),
    );

    if (
      !this.namedLatLon &&
      Object.keys(this.activatedRoute.snapshot.params).length === 0
    ) {
      const savedLocations: NamedLatLon[] = this.getSavedLocations();
      if (savedLocations.length > 0) {
        await this.router.navigateByUrl(
          this.createDateUrl(dayjs(), savedLocations[0]),
        );
      }
    }
  }

  formatDate(date: Dayjs, format: string) {
    return date?.locale('de')?.format(format);
  }

  private getPageTags(): PageTags {
    const location = this.namedLatLon;
    const robots = this.activatedRoute.snapshot.params['eventId']
      ? 'noindex, follow'
      : 'index, follow';

    if (location) {
      const dateTitlePart = this.dateIsFromRelativeUrl
        ? this.getRelativeDateLabel(this.date)
        : `am ${this.formatDate(this.date, 'DD.MM.YYYY')}`;

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
        title: `Events ${dateTitlePart} in ${location.displayName}, ${location.area} | lokale.events`,
        description: `Entdecke aktuelle Veranstaltungen in ${location.displayName}, ${location.area}. Von Familien-Events über Sport-Aktivitäten bis hin zu kulturellen Veranstaltungen und Märkten - finde spannende Events in deiner Nähe.`,
        publisher: 'lokale.events',
        category: 'Events',
        url: this.getCurrentUrl(),
        region: location.area,
        place: location.displayName,
        lang: 'de',
        publishedAt: dayjs(),
        position: location,
        keywords,
        expiresAt: this.date,
        author: 'lokale.events Team',
        robots,
        canonicalUrl: `https://lokale.events${this.createDateUrl(this.date, location)}`,
      };
    } else {
      return {
        title: `lokale.events | Entdecke Events und Veranstaltungen in deiner Umgebung`,
        description: `Finde spannende lokale Veranstaltungen in deiner Nähe. Von Familien-Events über Sport-Aktivitäten bis hin zu kulturellen Veranstaltungen und Märkten - entdecke was deine Region zu bieten hat.`,
        publisher: 'lokale.events',
        category: 'Events',
        url: this.getCurrentUrl(),
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

  private getCurrentUrl(): string {
    if (isPlatformBrowser(this.platformId)) {
      return document.location.href;
    }
    return '';
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

  fetchEventsBetweenDates(
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
          after: minDate.startOf('day').valueOf(),
          before: maxDate.endOf('day').valueOf(),
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
    return this.appConfigService.customProperties['eventRepositoryId'] as any;
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
      temporalCoverage: `${this.date.subtract(2, 'months').toISOString()}/${this.date.add(1, 'week').toISOString()}`,
      url: 'this.locationService.href',
      inLanguage: 'de-DE',
      about: {
        '@type': 'Thing',
        name: `Events in ${this.namedLatLon?.displayName || 'deiner Nähe'}`,
        description: tags.description,
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
  ): SchemaEvent {
    const startDate = dayjs(event.startingAt);
    return {
      '@type': 'Event',
      name: event.title,
      description: event.text || `Veranstaltung in ${location.displayName}`,
      eventStatus: startDate.isBefore(dayjs())
        ? 'EventScheduled'
        : 'EventCancelled',
      eventAttendanceMode: 'OfflineEventAttendanceMode',
      startDate: startDate.toISOString(),
      endDate: startDate.endOf('day').toISOString(),
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

  createDateUrl(
    date: Nullable<Dayjs>,
    location: Nullable<NamedLatLon> = null,
  ): string {
    const { countryCode, region, place } = this.getLocationOrElse(location);

    const now = dayjs().startOf('day');
    const diffInDays = date.startOf('day').diff(now, 'day');

    const hasRelativeDateExpression = Object.values<number>(
      relativeDateIncrement,
    ).includes(diffInDays);
    if (hasRelativeDateExpression) {
      const relativeDates = Object.keys(
        relativeDateIncrement,
      ) as RelativeDate[];
      const relativeDateParam = relativeDates.find(
        (relativeDate) => relativeDateIncrement[relativeDate] === diffInDays,
      );

      return renderUrlWithRelativeDate(
        countryCode,
        region,
        place,
        relativeDateParam,
      );
    } else {
      const { year, month, day } = this.getDateOrElse(date);

      return renderUrlWithAbsoluteDate(
        countryCode,
        region,
        place,
        year,
        month,
        day,
      );
    }
  }

  createEventUrl(event: LocalizedEvent): string {
    const baseUrl = this.createDateUrl(this.date, this.namedLatLon);
    return baseUrl ? `${baseUrl}/${(event as any).id}` : '';
  }

  getPlaceUrl(location: NamedLatLon): string {
    if (location) {
      const { countryCode, area, place } = location;
      // Use parseDateFromUrl to handle both absolute and relative dates
      const { date } = parseDateFromUrl(this.activatedRoute.snapshot.params);
      return renderUrlWithAbsoluteDate(
        countryCode,
        area,
        place,
        parseInt(date.format('YYYY')),
        parseInt(date.format('MM')),
        parseInt(date.format('DD')),
      );
    }
    return '';
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
    return title
      .replace(/[0-9]{1,2}\.[ .]?[a-z]{3,10}[ .]?[0-9]{2,4}/gi, '')
      .replace(/[0-9]{1,2}\.[ .]?[0-9]{1,2}[ .]?[0-9]{2,4}/gi, '');
  }

  async openEventModal(
    event: LocalizedEvent,
    place: NamedLatLon,
  ): Promise<void> {
    const eventUrl = this.createEventUrl(event);
    if (eventUrl && isPlatformBrowser(this.platformId)) {
      this.locationService.replaceState(eventUrl);
    }
    const modal = await this.modalCtrl.create({
      component: EventDetailModalComponent,
      componentProps: {
        event,
        place,
        repositoryId: this.getRepositoryId(),
      },
      breakpoints: [0, 0.25, 0.5, 0.75, 1],
      initialBreakpoint: 0.75,
      backdropBreakpoint: 0.5,
      cssClass: 'event-detail-sheet',
    });
    await modal.present();
    const result = await modal.onDidDismiss();
    if (result.data?.saved === true || result.data?.deleted === true) {
      await this.fetchEvents(this.date);
    }
    const listUrl = this.createDateUrl(this.date, this.namedLatLon);
    if (listUrl && isPlatformBrowser(this.platformId)) {
      this.locationService.replaceState(listUrl);
    }
    this.changeRef.detectChanges();
  }

  private async openEventModalForId(eventId: string): Promise<void> {
    const found = this.findEventAndPlace(eventId);
    if (found) {
      await this.openEventModal(found.event, found.place);
    } else {
      console.error('Unable to find event with id ' + eventId);
    }
  }

  private findEventAndPlace(
    eventId: string,
  ): { event: LocalizedEvent; place: NamedLatLon } | null {
    for (const day of this.placesByDistancePerDay) {
      for (const group of day.eventGroups) {
        for (const { place, events } of group.places) {
          const event = events.find((e) => String((e as any).id) === eventId);
          if (event) {
            return { event, place };
          }
        }
      }
    }
    return null;
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

  getDateUrlFactory() {
    return this.createDateUrl.bind(this);
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

export function renderUrlWithAbsoluteDate(
  countryCode: string,
  region: string,
  place: string,
  year: number,
  month: number,
  day: number,
) {
  return renderPath(
    upcomingBaseRoute.events.countryCode.region.place.dateTime,
    {
      countryCode,
      region,
      place,
      year,
      month,
      day,
    },
  );
}

export function renderUrlWithRelativeDate(
  countryCode: string,
  region: string,
  place: string,
  relativeDate: RelativeDate,
) {
  return renderPath(
    upcomingBaseRoute.events.countryCode.region.place.relativeDateTime,
    {
      countryCode,
      region,
      place,
      relativeDate,
    },
  );
}
