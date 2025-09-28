import { ActivatedRoute, Params, Routes } from '@angular/router';
import dayjs, { Dayjs } from 'dayjs';
import { OpenStreetMapService } from '../../services/open-street-map.service';
import { NamedLatLon } from '../../types';
import { intParser, route } from 'typesafe-routes';
import { Parser } from 'typesafe-routes/build/parser';
import { strParser, upperCaseStringParser } from '../default-routes';
import { AuthGuardService } from '../../guards/auth-guard.service';

export const perimeterUnit = 'Km';

export function parseDateFromUrl(params: Params): Dayjs {
  const { year, month, day } =
    upcomingBaseRoute.children.events.children.countryCode.children.region.children.place.children.dateTime.parseParams(
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
    upcomingBaseRoute.children.events.children.countryCode.parseParams(
      activatedRoute.snapshot.params as any,
    );
  const { region } =
    upcomingBaseRoute.children.events.children.countryCode.children.region.parseParams(
      activatedRoute.snapshot.params as any,
    );
  const { place } =
    upcomingBaseRoute.children.events.children.countryCode.children.region.children.place.parseParams(
      activatedRoute.snapshot.params as any,
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

export const perimeterParser: Parser<number> = {
  parse: (s) => parseInt(s.replace(perimeterUnit, '')),
  serialize: (s) => `${s}${perimeterUnit}`,
};

export const upcomingBaseRoute = route(
  '',
  {},
  {
    terms: route('agb', {}, {}),
    login: route('login', {}, {}),
    about: route('ueber-uns', {}, {}),
    management: route(
      'management',
      {},
      {
        sources: route('sources', {}, {}),
        documents: route('documents', {}, {}),
      },
    ),
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
    path: upcomingBaseRoute.template,
    children: [
      {
        path: upcomingBaseRoute.children.about.template,
        loadComponent: () =>
          import('./about-us/about-us.page').then((m) => m.AboutUsPage),
      },
      {
        path: upcomingBaseRoute.children.terms.template,
        loadComponent: () =>
          import('./terms/terms.page').then((m) => m.TermsPage),
      },
      {
        path: upcomingBaseRoute.children.login.template,
        loadComponent: () =>
          import('../../pages/login/login.page').then((m) => m.LoginPage),
      },
      {
        path: upcomingBaseRoute.children.management.template,
        canActivate: [AuthGuardService],
        redirectTo: '/' + upcomingBaseRoute({}).management({}).sources({}).$,
      },
      {
        path: upcomingBaseRoute.children.management.children.sources.template,
        canActivate: [AuthGuardService],
        data: { sources: true },
        loadComponent: () =>
          import('./management/management.page').then((m) => m.ManagementPage),
      },
      {
        path: upcomingBaseRoute.children.management.children.documents.template,
        canActivate: [AuthGuardService],
        loadComponent: () =>
          import('./management/management.page').then((m) => m.ManagementPage),
      },
      // Most specific routes first to avoid redirect chains
      {
        path: upcomingBaseRoute.children.events.children.countryCode.children
          .region.children.place.children.dateTime.children.eventId.template,
        loadComponent: () =>
          import('./event/event.page').then((m) => m.EventPage),
      },
      {
        path: upcomingBaseRoute.children.events.children.countryCode.children
          .region.children.place.children.dateTime.template,
        loadComponent: () =>
          import('./events/events.page').then((m) => m.EventsPage),
      },
      {
        path: upcomingBaseRoute.children.events.children.countryCode.children
          .region.children.place.template,
        loadComponent: () =>
          import('./events/events.page').then((m) => m.EventsPage),
      },
      {
        path: upcomingBaseRoute.children.events.template,
        loadComponent: () =>
          import('./events/events.page').then((m) => m.EventsPage),
      },
      // Default route (least specific)
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () =>
          import('./events/events.page').then((m) => m.EventsPage),
      },
    ],
  },
  // {
  //   path: '**',
  //   redirectTo: ''
  // }
];
