import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { AppConfigService } from '../../../services/app-config.service';
import dayjs, { Dayjs } from 'dayjs';
import { OpenStreetMapService, OsmMatch } from '../../../services/open-street-map.service';
import { groupBy, sortBy, unionBy } from 'lodash-es';
import { RecordService } from '../../../services/record.service';
import { Record } from '../../../graphql/types';
import { Event as SchemaEvent, Place as SchemaPlace, WebPage } from 'schema-dts';
import { NamedLatLon, namedPlaces } from '../places';
import { LatLon } from '../../../components/map/map.component';
import { PageService } from '../../../services/page.service';
import { ActivatedRoute, Router } from '@angular/router';
import { convertOsmMatchToString, EventsUrlFragments } from '../upcoming-header/upcoming-header.component';
import { parseDateFromUrl, parseLocationFromUrl, parsePerimeterFromUrl, perimeterUnit } from '../upcoming-product-routing.module';
import { Subscription } from 'rxjs';

type Distance2Events = { [distance: string]: Record[] };
type EventsByDistance = {
  distance: string;
  events: Record[];
};

type PlaceByDistance = {
  distance: string;
  places: EventsAtPlace[];
};

type EventsAtPlace = {
  place: NamedLatLon;
  events: Record[];
};

export function createEventsUrl(parts: EventsUrlFragments, router: Router): string {
  let texts: string[];
  // if (this.locale == 'de') {
  texts = ['events/in', 'am', 'innerhalb', ];
  // } else {
  //   texts = ['events/in', 'on', 'within', ];
  // }
  return router
    .createUrlTree([
      texts[0],
      parts.country,
      parts.state,
      parts.place,
      texts[1],
      parts.year,
      parts.month,
      parts.day,
      texts[2],
      `${parts.perimeter}${perimeterUnit}`,

    ])
    .toString();
}


@Component({
  selector: 'app-events-page',
  templateUrl: './events-page.component.html',
  styleUrls: ['./events-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventsPageComponent implements OnInit, OnDestroy {
  date: Dayjs;
  perimeter: number;
  latLon: LatLon;
  location: OsmMatch;
  loading: boolean = true;
  private subscriptions: Subscription[] = [];

  protected placesByDistance: PlaceByDistance[] = [];
  protected loadingDay = true;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly recordService: RecordService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly pageService: PageService,
    private readonly openStreetMapService: OpenStreetMapService,
    private readonly appConfigService: AppConfigService,
  ) {}

  async ngOnInit(): Promise<void> {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        try {
          console.log('params', params, this.activatedRoute.snapshot.params)
          this.location = await parseLocationFromUrl(this.activatedRoute, this.openStreetMapService);
          this.latLon = [parseFloat(this.location.lat), parseFloat(this.location.lon)];
          this.perimeter = parsePerimeterFromUrl(this.activatedRoute);
          this.date = parseDateFromUrl(this.activatedRoute);
          this.changeRef.detectChanges();

          await this.fetchEvents();

        } catch (e) {
          const today = dayjs();
          const url = createEventsUrl({
            state: 'CH',
            country: 'Zurich',
            place: 'Affoltern a.A.',
            perimeter: 10,
            year: today.year(),
            month: today.month(),
            day: today.day()
          }, this.router);
          console.log('redirect');
          await this.router.navigateByUrl(url);
        } finally {
          this.loading = false;
        }
        this.changeRef.detectChanges();
      })
    )

    // this.pageService.setMetaTags({
    //   title: `Events in ${parts.place}, ${parts.state} | Entdecke lokale Veranstaltungen, Familien- und Sport-Aktivitäten in deiner Umgebung`,
    //   description: `Erfahre alles über aktuelle Veranstaltungen in ${parts.place}, ${parts.state}. Von Veranstaltungen, Familien- und Sport-Aktivitäten und Märkten, entdecke, was in ${parts.place}, ${parts.state} geboten wird. Ideal für Einheimische, Familien und Besucher.`,
    //   publisher: 'upcoming',
    //   category: '',
    //   url: document.location.href,
    //   region: parts.state,
    //   place: parts.place,
    //   lang: 'de',
    //   publishedAt: dayjs(),
    //   position: [
    //     parseFloat(`${location.lat}`),
    //     parseFloat(`${location.lon}`),
    //   ],
    // })
    //
    // this.fetchEvents();
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

  private deg2rad(deg) {
    return deg * (Math.PI / 180);
  }

  private fetchEventOfDay(day: Dayjs): Promise<Record[]> {
    return this.recordService.findAllByRepositoryId({
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
            lat: this.latLon[0],
            lon: this.latLon[1],
          },
          distanceKm: this.perimeter,
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

  async fetchEvents() {
    try {
      const events = await this.fetchEventOfDay(this.date.clone());
      const places: NamedLatLon[] = await Promise.all(
        unionBy(
          events.map((e) => e.latLng),
          (e) => `${e.lat},${e.lon}`,
        )
          .filter((e) => e)
          .map((latLon) => {
            const namedPlace = namedPlaces.find(
              (place) => place.lat == latLon.lat && place.lon == latLon.lon,
            );
            if (namedPlace) {
              return {
                lat: latLon.lat,
                lon: latLon.lon,
                name: namedPlace.name,
              };
            } else {
              return this.openStreetMapService
                .reverseSearch(latLon.lat, latLon.lon)
                .then((match) => ({
                  lat: latLon.lat,
                  lon: latLon.lon,
                  name: convertOsmMatchToString(match),
                }));
            }
          }),
      );

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
        const latLonGroups = groupBy(eventGroup.events, (e) =>
          JSON.stringify(e.latLng),
        );
        groupedPlaces.push({
          distance: eventGroup.distance,
          places: Object.keys(latLonGroups).map((latLonGroup) => {
            return {
              events: latLonGroups[latLonGroup],
              place: places.find(
                (place) =>
                  place.lat == latLonGroups[latLonGroup][0].latLng.lat &&
                  place.lon == latLonGroups[latLonGroup][0].latLng.lon,
              ),
            };
          }),
        });

        return groupedPlaces;
      }, [] as PlaceByDistance[]);

      this.pageService.setJsonLdData(this.createSchemaOrgGraph());
    } catch (e) {
    } finally {
      this.loadingDay = false;
    }
    this.changeRef.detectChanges();
  }

  private getGeoDistance(event: Record): number {
    return this.getDistanceFromLatLonInKm(
      event.latLng.lat,
      event.latLng.lon,
      this.latLon[0],
      this.latLon[1],
    );
  }

  private getRepositoryId(): string {
    return this.appConfigService.customProperties.eventRepositoryId as any;
  }

  createSchemaOrgGraph(): WebPage {
    return {
      '@type': 'WebPage',
      name: 'Upcoming Events by Location',
      url: 'https://example.com/upcoming-events',
      mainEntity: {
        '@type': 'ItemList',
        itemListElement: this.placesByDistance
          .flatMap((distanced) => distanced.places)
          .map((place) => this.toSchemaOrgPlace(place)),
      },
    };
  }

  private toSchemaOrgEvent(event: Record): SchemaEvent {
    return {
      '@type': 'Event',
      name: event.title,
      description: event.text,
      startDate: dayjs(event.startingAt).format('YYYY-MM-DD'),
      // endDate: '2024-11-15T22:15',
      url: event.url,
      location: {
        '@type': 'Place',
        name: 'Hedingen',
      },
    };
  }

  getEventUrl(event: Record) {
    const { state, country, place, year, month, day } = this.activatedRoute.snapshot.params;
    // event/in/:state/:country/:place/am/:year/:month/:day/details/:eventId/
    let texts = ['event/in', 'am', 'details', ];

    return this.router
      .createUrlTree([
        texts[0],
        state,
        country,
        place,
        texts[1],
        year,
        month,
        day,
        texts[2],
        event.id,
      ])
      .toString();
  }

  private toSchemaOrgPlace(place: EventsAtPlace): SchemaPlace {
    return {
      '@type': 'Place',
      name: place.place.name,
      geo: {
        '@type': 'GeoCoordinates',
        latitude: place.place.lat,
        longitude: place.place.lon,
      },
      event: place.events.map((event) => this.toSchemaOrgEvent(event)),
    };
  }
}
