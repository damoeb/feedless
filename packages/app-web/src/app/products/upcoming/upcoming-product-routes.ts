import { ActivatedRoute, Params, Routes } from '@angular/router';
import dayjs, { Dayjs } from 'dayjs';
import { OpenStreetMapService } from '../../services/open-street-map.service';
import { NamedLatLon } from '../../types';
import {
  createRoutes,
  int,
  param,
  parsePath,
  renderPath,
  str,
  template,
} from 'typesafe-routes';
import { upperCaseStringParser } from '../default-routes';
import { AuthGuardService } from '../../guards/auth-guard.service';

export const perimeterUnit = 'Km';

export function parseDateFromUrl(params: Params): Dayjs {
  const { year, month, day } = parsePath(
    upcomingBaseRoute.events.countryCode.region.place.dateTime,
    params,
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
  const { countryCode } = parsePath(
    upcomingBaseRoute.events.countryCode,
    activatedRoute.snapshot.params,
  );
  const { region } = parsePath(
    upcomingBaseRoute.events.countryCode.region,
    activatedRoute.snapshot.params,
  );
  const { place } = parsePath(
    upcomingBaseRoute.events.countryCode.region.place,
    activatedRoute.snapshot.params,
  );

  const err = Error('Cannot parse location from url');

  if (countryCode && region && place) {
    const results = await openStreetMapService.searchByObject({
      area: region,
      countryCode,
      place,
    });
    if (results.length > 0) {
      return results[0];
    }
    throw err;
  }
  throw err;
}

export const perimeterParser = param<number>({
  parse: (value: string) => parseInt(value.replace(perimeterUnit, '')),
  serialize: (value: number) => `${value}${perimeterUnit}`,
});

export const upcomingBaseRoute = createRoutes({
  terms: {
    path: ['agb'],
  },
  login: {
    path: ['login'],
  },
  about: {
    path: ['ueber-uns'],
  },
  management: {
    path: ['management'],
    children: {
      sources: {
        path: ['sources'],
      },
      documents: {
        path: ['documents'],
      },
    },
  },
  events: {
    path: ['events', 'in'],
    children: {
      countryCode: {
        path: [upperCaseStringParser('countryCode')],
        children: {
          region: {
            path: [str('region')],
            children: {
              place: {
                path: [str('place')],
                children: {
                  dateTime: {
                    path: ['am', int('year'), int('month'), int('day')],
                    children: {
                      perimeter: {
                        path: [perimeterParser('perimeter')],
                      },
                      eventId: {
                        path: [int('eventId')],
                      },
                    },
                  },
                },
              },
            },
          },
        },
      },
    },
  },
  // events2: {
  //   path: [
  //     'events',
  //     'in',
  //     upperCaseStringParser('countryCode'),
  //     str('region'),
  //     str('place'),
  //     'am',
  //     int('year'),
  //     int('month'),
  //     int('day'),
  //   ],
  // }
});

export const UPCOMING_ROUTES: Routes = [
  {
    path: template(upcomingBaseRoute),
    children: [
      {
        path: template(upcomingBaseRoute.about),
        loadComponent: () =>
          import('./about-us/about-us.page').then((m) => m.AboutUsPage),
      },
      {
        path: template(upcomingBaseRoute.terms),
        loadComponent: () =>
          import('./terms/terms.page').then((m) => m.TermsPage),
      },
      {
        path: template(upcomingBaseRoute.login),
        loadComponent: () =>
          import('../../pages/login/login.page').then((m) => m.LoginPage),
      },
      {
        path: template(upcomingBaseRoute.management),
        canActivate: [AuthGuardService],
        children: [
          {
            path: template(upcomingBaseRoute.management.sources),
            data: { sources: true },
            loadComponent: () =>
              import('./management/management.page').then(
                (m) => m.ManagementPage,
              ),
          },
          {
            path: template(upcomingBaseRoute.management.documents),
            loadComponent: () =>
              import('./management/management.page').then(
                (m) => m.ManagementPage,
              ),
          },
          {
            path: '**',
            redirectTo:
              '/' + renderPath(upcomingBaseRoute.management.sources, {}),
          },
        ],
      },
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () =>
          import('./events/events.page').then((m) => m.EventsPage),
      },
      {
        path: template(upcomingBaseRoute.events),
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./events/events.page').then((m) => m.EventsPage),
          },
          {
            path: template(upcomingBaseRoute.events.countryCode),
            children: [
              {
                path: '',
                loadComponent: () =>
                  import('./events/events.page').then((m) => m.EventsPage),
              },
              {
                // path: 'events/in/:state',
                path: template(upcomingBaseRoute.events.countryCode.region),
                children: [
                  {
                    path: '',
                    loadComponent: () =>
                      import('./events/events.page').then((m) => m.EventsPage),
                  },
                  {
                    // path: 'events/in/:state/:country/:place/am/:year/:month/:day/innerhalb/:perimeter',
                    path: template(
                      upcomingBaseRoute.events.countryCode.region.place,
                    ),
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
                        path: template(
                          upcomingBaseRoute.events.countryCode.region.place
                            .dateTime.perimeter,
                        ),
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
                            path: template(
                              upcomingBaseRoute.events.countryCode.region.place
                                .dateTime.eventId,
                            ),
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
