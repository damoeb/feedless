import { ActivatedRoute, Params, Routes } from '@angular/router';
import dayjs, { Dayjs } from 'dayjs';
import { OpenStreetMapService } from '../../services/open-street-map.service';
import { NamedLatLon } from '../../types';
import { intParser, route } from 'typesafe-routes';
import { Parser } from 'typesafe-routes/build/parser';

export const perimeterUnit = 'Km';

export function parseDateFromUrl(params: Params): Dayjs {
  const { year, month, day } =
    homeRoute.children.events.children.countryCode.children.region.children.place.children.dateTime.parseParams(
      params as any,
    );
  const date = dayjs(`${year}/${month}/${day}`, 'YYYY/MM/DD');
  if (date?.isValid()) {
    return date;
  } else {
    return dayjs();
  }
}

export async function parseLocationFromUrl(
  activatedRoute: ActivatedRoute,
  openStreetMapService: OpenStreetMapService,
): Promise<NamedLatLon> {
  const { countryCode } =
    homeRoute.children.events.children.countryCode.parseParams(
      activatedRoute.snapshot.params as any,
    );
  const { region } =
    homeRoute.children.events.children.countryCode.children.region.parseParams(
      activatedRoute.snapshot.params as any,
    );
  const { place } =
    homeRoute.children.events.children.countryCode.children.region.children.place.parseParams(
      activatedRoute.snapshot.params as any,
    );

  if (countryCode && region && place) {
    const results = await openStreetMapService.searchByObject({
      area: region,
      countryCode,
      place,
    });
    if (results.length > 0) {
      return results[0];
    }
    throw Error();
  }
  throw Error();
}

export const perimeterParser: Parser<number> = {
  parse: (s) => parseInt(s.replace(perimeterUnit, '')),
  serialize: (s) => `${s}${perimeterUnit}`,
};
export const upperCaseStringParser: Parser<string> = {
  parse: (s) => s.toUpperCase(),
  serialize: (s) => s.toUpperCase(),
};
export const strParser: Parser<string> = {
  parse: (s) => decodeURIComponent(s),
  serialize: (s) => s,
};

export const homeRoute = route(
  '',
  {},
  {
    agb: route('agb', {}, {}),
    about: route('ueber-uns', {}, {}),
    events: route(
      'events/in',
      {},
      {
        countryCode: route(
          ':countryCode',
          { countryCode: upperCaseStringParser },
          {
            region: route(
              ':region',
              { region: strParser },
              {
                place: route(
                  ':place',
                  {
                    place: strParser,
                  },
                  {
                    dateTime: route(
                      'am/:year/:month/:day/innerhalb/:perimeter',
                      {
                        year: intParser,
                        month: intParser,
                        day: intParser,
                        perimeter: perimeterParser,
                      },
                      {
                        eventId: route(':eventId', { eventId: strParser }, {}),
                      },
                    ),
                  },
                ),
              },
            ),
          },
        ),
      },
    ),
  },
);

export const UPCOMING_ROUTES: Routes = [
  {
    path: homeRoute.template,
    children: [
      {
        path: homeRoute.children.about.template,
        loadComponent: () =>
          import('./about-us/about-us.page').then((m) => m.AboutUsPage),
      },
      {
        path: '',
        loadComponent: () =>
          import('./events/events.page').then((m) => m.EventsPage),
      },
      {
        path: homeRoute.children.events.template,
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./events/events.page').then((m) => m.EventsPage),
          },
          {
            path: homeRoute.children.events.children.countryCode.template,
            children: [
              {
                path: '',
                loadComponent: () =>
                  import('./events/events.page').then((m) => m.EventsPage),
              },
              {
                // path: 'events/in/:state',
                path: homeRoute.children.events.children.countryCode.children
                  .region.template,
                children: [
                  {
                    path: '',
                    loadComponent: () =>
                      import('./events/events.page').then((m) => m.EventsPage),
                  },
                  {
                    // path: 'events/in/:state/:country/:place/am/:year/:month/:day/innerhalb/:perimeter',
                    path: homeRoute.children.events.children.countryCode
                      .children.region.children.place.template,
                    children: [
                      {
                        path: '',
                        loadComponent: () =>
                          import('./events/events.page').then(
                            (m) => m.EventsPage,
                          ),
                      },
                      {
                        // path: 'events/in/:state/:country/:place/am/:year/:month/:day/innerhalb/:perimeter',
                        path: homeRoute.children.events.children.countryCode
                          .children.region.children.place.children.dateTime
                          .template,
                        children: [
                          {
                            path: '',
                            loadComponent: () =>
                              import('./events/events.page').then(
                                (m) => m.EventsPage,
                              ),
                          },
                          {
                            // event/in/CH/Zurich/Affoltern%2520a.A./am/2024/11/02/details/7f2bee6c-be92-49b3-bbbe-aab1e207fa5c
                            path: homeRoute.children.events.children.countryCode
                              .children.region.children.place.children.dateTime
                              .children.eventId.template,
                            loadComponent: () =>
                              import('./event/event.page').then(
                                (m) => m.EventPage,
                              ),
                          },
                        ],
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  // {
  //   path: '**',
  //   redirectTo: ''
  // }
];
