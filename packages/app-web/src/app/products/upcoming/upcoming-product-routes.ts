import { ActivatedRoute, Params, Routes } from '@angular/router';
import dayjs, { Dayjs } from 'dayjs';
import { OpenStreetMapService } from '../../services/open-street-map.service';
import { NamedLatLon } from '../../types';
import { createRoutes, int, param, parsePath, str, template } from 'typesafe-routes';
import { upperCaseStringParser } from '../default-routes';
import { AuthGuardService } from '../../guards/auth-guard.service';

export const perimeterUnit = 'Km';

export function parseDateFromUrl(params: Params): Dayjs {
  try {
    const { year, month, day } = parsePath(
      upcomingBaseRoute.events.countryCode.region.place.dateTime,
      params
    );
    const date = dayjs(`${year}/${month}/${day}`, 'YYYY/MM/DD');
    if (date?.isValid()) {
      return date;
    }
  } catch (e) {
    // ignore
  }
  return dayjs();
}

export async function parseLocationFromUrl(
  activatedRoute: ActivatedRoute,
  openStreetMapService: OpenStreetMapService
): Promise<NamedLatLon> {
  const { place, region, countryCode } = parsePath(
    upcomingBaseRoute.events.countryCode.region.place,
    activatedRoute.snapshot.params
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
  // parse: (value: string) => parseInt(value.replace(perimeterUnit, '')),
  parse: (value: string) => 10,
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
                        path: ['innerhalb', perimeterParser('perimeter')],
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
});

function toPath(url: string): string {
  return url.substring(1);
}

export const UPCOMING_ROUTES: Routes = [
  {
    path: toPath(template(upcomingBaseRoute.about)),
    loadComponent: () => import('./about-us/about-us.page').then((m) => m.AboutUsPage),
  },
  {
    path: toPath(template(upcomingBaseRoute.terms)),
    loadComponent: () => import('./terms/terms.page').then((m) => m.TermsPage),
  },
  {
    path: toPath(template(upcomingBaseRoute.login)),
    loadComponent: () => import('../../pages/login/login.page').then((m) => m.LoginPage),
  },
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./events/events.page').then((m) => m.EventsPage),
  },
  {
    path: '',
    canActivate: [AuthGuardService],
    children: [
      {
        path: toPath(template(upcomingBaseRoute.management.sources)),
        data: { sources: true },
        loadComponent: () => import('./management/management.page').then((m) => m.ManagementPage),
      },
      {
        path: toPath(template(upcomingBaseRoute.management.documents)),
        loadComponent: () => import('./management/management.page').then((m) => m.ManagementPage),
      },
    ],
  },
  {
    path: toPath(template(upcomingBaseRoute.events.countryCode)),
    loadComponent: () => import('./events/events.page').then((m) => m.EventsPage),
  },
  {
    path: toPath(template(upcomingBaseRoute.events.countryCode.region)),
    loadComponent: () => import('./events/events.page').then((m) => m.EventsPage),
  },
  {
    path: toPath(template(upcomingBaseRoute.events.countryCode.region.place)),
    loadComponent: () => import('./events/events.page').then((m) => m.EventsPage),
  },
  {
    path: toPath(template(upcomingBaseRoute.events.countryCode.region.place.dateTime)),
    loadComponent: () => import('./events/events.page').then((m) => m.EventsPage),
  },
  {
    path: toPath(template(upcomingBaseRoute.events.countryCode.region.place.dateTime.perimeter)),
    loadComponent: () => import('./events/events.page').then((m) => m.EventsPage),
  },
  {
    // event/in/CH/Zurich/Affoltern%2520a.A./am/2024/11/02/details/7f2bee6c-be92-49b3-bbbe-aab1e207fa5c
    path: toPath(template(upcomingBaseRoute.events.countryCode.region.place.dateTime.eventId)),
    loadComponent: () => import('./event/event.page').then((m) => m.EventPage),
  },
];
