import {
  ActivatedRouteSnapshot,
  Params,
  ResolveFn,
  Routes,
} from '@angular/router';
import dayjs, { Dayjs } from 'dayjs';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { AppConfigService, AuthGuardService } from '@feedless/components';
import { NamedLatLon, upperCaseStringParser } from '@feedless/core';
import {
  createRoutes,
  parsePath,
  renderPath,
  str,
  template,
} from 'typesafe-routes';
import { AdminGeoService, GeoSearchService } from '@feedless/geo';
import { inject } from '@angular/core';
import { EventService, LocalizedEvent } from './event.service';

export const perimeterUnit = 'Km';

export function parseDateFromQuery(queryParams: Params): {
  date: Dayjs;
  explicit: boolean;
} {
  const raw = queryParams['date'];
  if (typeof raw === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(raw)) {
    const parsed = dayjs(raw, 'YYYY-MM-DD');
    if (parsed.isValid()) {
      return { date: parsed, explicit: true };
    }
  }
  return { date: dayjs(), explicit: false };
}

export function renderPlaceUrl(
  countryCode: string,
  region: string,
  place: string,
): string {
  return renderPath(upcomingBaseRoute.events.countryCode.region.place, {
    countryCode,
    region,
    place,
  });
}

/**
 * Heute ist die nackte Ortsseite, jeder andere Tag hängt als `?date=` daran.
 * Der Canonical zeigt immer auf die parameterlose Form.
 */
export function renderDateUrl(
  countryCode: string,
  region: string,
  place: string,
  date: Dayjs | null | undefined,
): string {
  const base = renderPlaceUrl(countryCode, region, place);
  if (!date || date.isSame(dayjs(), 'day')) {
    return base;
  }
  return `${base}?date=${date.format('YYYY-MM-DD')}`;
}

export async function parseLocationFromUrl(
  activatedRoute: ActivatedRouteSnapshot,
  geoService: GeoSearchService,
): Promise<NamedLatLon> {
  const err = Error('Cannot parse location from url');

  try {
    const { place, region, countryCode } = parsePath(
      upcomingBaseRoute.events.countryCode.region.place,
      activatedRoute.params,
    );

    if (countryCode && region && place) {
      const results = await geoService.searchByObject({
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
  } catch (e) {
    // If parsePath fails due to missing params, throw our custom error
    throw err;
  }
}

export type EventsResolverData = {
  events: LocalizedEvent[];
  latlng: NamedLatLon;
  date: Dayjs;
};

/**
 * Never rejects. A rejected resolver aborts the navigation, so Angular SSR
 * renders nothing, `angularApp.handle()` resolves to null and express answers
 * its default 404 - an api outage would take every relative-date url in the
 * sitemap down with it. EventCalendarPage already tolerates missing data and
 * refetches on hydration, so degrade as far as the failure requires and let
 * the page render.
 */
export const eventsResolver: ResolveFn<
  Promise<EventsResolverData | null>
> = async (route): Promise<EventsResolverData | null> => {
  const eventService = inject(EventService);
  const geoService = inject(AdminGeoService);
  const appConfigService = inject(AppConfigService);

  const { date } = parseDateFromQuery(route.queryParams);

  let latlng: NamedLatLon;
  try {
    latlng = await parseLocationFromUrl(route, geoService);
  } catch (e) {
    // Unresolvable place, or the geo backend is down - either way there is
    // nothing to anchor the page to.
    console.error('eventsResolver: cannot resolve location', e);
    return null;
  }

  const repositoryId = appConfigService.customProperties[
    'eventRepositoryId'
  ] as any;

  try {
    const events = await eventService.fetchEventsBetweenDates(
      date,
      repositoryId,
      latlng.lat,
      latlng.lng,
    );
    return { events, latlng, date };
  } catch (e) {
    // The location is known, so the page still has its heading, meta tags and
    // canonical url. Only the event list is missing.
    console.error('eventsResolver: cannot fetch events', e);
    return { events: [], latlng, date };
  }
};

// const eventResolver: ResolveFn<LocalizedEvent> = (route) => {
//   const eventService = inject(EventService);
//   const id = route.paramMap.get('id')!;
//   return eventService.findById(id);
// };

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
  profile: {
    path: ['profile'],
    children: {
      sources: {
        path: ['sources'],
      },
      documents: {
        path: ['documents'],
      },
      account: {
        path: ['account'],
      },
      subscriptions: {
        path: ['subscriptions'],
      },
      security: {
        path: ['security'],
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
              },
            },
          },
        },
      },
    },
  },
});

export const UPCOMING_ROUTES: Routes = [
  // {
  //   path: '',
  //   outlet: 'sidemenu',
  //   component: FeedlessMenuComponent,
  // },
  {
    path: template(upcomingBaseRoute._.about),
    loadComponent: () =>
      import('./pages/about-us/about-us.page').then((m) => m.AboutUsPage),
  },
  {
    path: template(upcomingBaseRoute._.terms),
    loadComponent: () =>
      import('./pages/terms/terms.page').then((m) => m.TermsPage),
  },
  {
    path: template(upcomingBaseRoute._.login),
    loadComponent: () =>
      import('@feedless/components').then((m) => m.LoginPage),
  },
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./pages/event-calendar/event-calendar.page').then(
        (m) => m.EventCalendarPage,
      ),
  },
  {
    path: template(upcomingBaseRoute._.profile),
    canActivate: [AuthGuardService],
    loadComponent: () =>
      import('./pages/profile/profile.page').then((m) => m.ProfilePage),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'sources',
      },
      {
        path: template(upcomingBaseRoute.profile._.sources),
        data: { sources: true },
        loadComponent: () =>
          import('./pages/event-sources/event-sources.page').then(
            (m) => m.EventSourcesPage,
          ),
      },
      {
        path: template(upcomingBaseRoute.profile._.account),
        loadComponent: () =>
          import('./pages/account/account.page').then((m) => m.AccountPage),
      },
      {
        path: template(upcomingBaseRoute.profile._.security),
        loadComponent: () =>
          import('./pages/security/security.page').then((m) => m.SecurityPage),
      },
      {
        path: template(upcomingBaseRoute.profile._.subscriptions),
        loadComponent: () =>
          import('./pages/subscriptions/subscriptions.page').then(
            (m) => m.SubscriptionsPage,
          ),
      },
    ],
  },
  {
    path: template(upcomingBaseRoute._.events.countryCode),
    loadComponent: () =>
      import('./pages/country-hub/country-hub.page').then(
        (m) => m.CountryHubPage,
      ),
  },
  {
    path: template(upcomingBaseRoute._.events.countryCode.region),
    loadComponent: () =>
      import('./pages/region-hub/region-hub.page').then((m) => m.RegionHubPage),
  },
  {
    resolve: {
      events: eventsResolver,
    },
    path: template(upcomingBaseRoute._.events.countryCode.region.place),
    loadComponent: () =>
      import('./pages/event-calendar/event-calendar.page').then(
        (m) => m.EventCalendarPage,
      ),
  },
];
