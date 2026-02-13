import { ActivatedRoute, Params, Routes } from '@angular/router';
import dayjs, { Dayjs } from 'dayjs';
import { AuthGuardService } from '@feedless/components';
import { NamedLatLon, upperCaseStringParser } from '@feedless/core';
import {
  createRoutes,
  int,
  param,
  parsePath,
  str,
  template,
} from 'typesafe-routes';
import { OpenStreetMapService } from '@feedless/geo';

export const perimeterUnit = 'Km';

export type RelativeDate =
  | 'gestern'
  | 'heute'
  | 'morgen'
  | 'kommendes-wochenende';

export const relativeDateIncrement: Record<RelativeDate, number> = {
  gestern: -1,
  heute: 0,
  morgen: 1,
  'kommendes-wochenende': (6 - dayjs().day() + 7) % 7 || 7,
};

export function parseRelativeDate(keyword: RelativeDate): Dayjs {
  const today = dayjs().startOf('day');
  switch (keyword) {
    case 'gestern':
      return today.add(-1, 'day');
    case 'heute':
      return today.add(0, 'day');
    case 'morgen':
      return today.add(1, 'days');
    case 'kommendes-wochenende': {
      // Find next friday
      const daysUntilSaturday = (6 - today.day() + 7) % 7 || 7;
      return today.add(daysUntilSaturday, 'days');
    }
    default:
      return today;
  }
}

export function parseDateFromUrl(params: Params): {
  date: Dayjs;
  relative: boolean;
} {
  // Try to parse relative date first
  try {
    const { relativeDate } = parsePath(
      upcomingBaseRoute.events.countryCode.region.place.relativeDateTime,
      params,
    );
    if (relativeDate) {
      return {
        date: parseRelativeDate(relativeDate as RelativeDate),
        relative: true,
      };
    }
  } catch (e) {
    // ignore, try absolute date
  }

  // Try to parse absolute date
  try {
    const { year, month, day } = parsePath(
      upcomingBaseRoute.events.countryCode.region.place.dateTime,
      params,
    );
    const date = dayjs(`${year}/${month}/${day}`, 'YYYY/MM/DD');
    if (date?.isValid()) {
      return { date, relative: false };
    }
  } catch (e) {
    // ignore
  }
  return { date: dayjs(), relative: false };
}

export async function parseLocationFromUrl(
  activatedRoute: ActivatedRoute,
  openStreetMapService: OpenStreetMapService,
): Promise<NamedLatLon> {
  const err = Error('Cannot parse location from url');

  try {
    const { place, region, countryCode } = parsePath(
      upcomingBaseRoute.events.countryCode.region.place,
      activatedRoute.snapshot.params,
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
      throw err;
    }
    throw err;
  } catch (e) {
    // If parsePath fails due to missing params, throw our custom error
    throw err;
  }
}

export const perimeterParser = param<number>({
  // parse: (value: string) => parseInt(value.replace(perimeterUnit, '')),
  parse: (value: string) => 10,
  serialize: (value: number) => `${value}${perimeterUnit}`,
});

export const relativeDateParser = param<RelativeDate>({
  parse: (value: string) => {
    const relativeDates = Object.keys(relativeDateIncrement) as RelativeDate[];

    if (relativeDates.includes(value as RelativeDate)) {
      return value as RelativeDate;
    }
    throw new Error(`Invalid relative date keyword: ${value}`);
  },
  serialize: (value: RelativeDate) => value,
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
                      //   perimeter: {
                      //     path: ['innerhalb', perimeterParser('perimeter')],
                      //   },
                      eventId: {
                        path: [int('eventId')],
                      },
                    },
                  },
                  relativeDateTime: {
                    path: [relativeDateParser('relativeDate')],
                    children: {
                      // perimeter: {
                      //   path: ['innerhalb', perimeterParser('perimeter')],
                      // },
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

export function toPath(url: string): string {
  return url.substring(1);
}

export const UPCOMING_ROUTES: Routes = [
  // {
  //   path: '',
  //   outlet: 'sidemenu',
  //   component: FeedlessMenuComponent,
  // },
  {
    path: toPath(template(upcomingBaseRoute.about)),
    loadComponent: () =>
      import('./pages/about-us/about-us.page').then((m) => m.AboutUsPage),
  },
  {
    path: toPath(template(upcomingBaseRoute.terms)),
    loadComponent: () =>
      import('./pages/terms/terms.page').then((m) => m.TermsPage),
  },
  // {
  //   path: toPath(template(upcomingBaseRoute.login)),
  //   loadComponent: () =>
  //     import('../../pages/login/login.page').then((m) => m.LoginPage),
  // },
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./pages/events/events.page').then((m) => m.EventsPage),
  },
  {
    path: '',
    canActivate: [AuthGuardService],
    children: [
      {
        path: toPath(template(upcomingBaseRoute.management.sources)),
        data: { sources: true },
        loadComponent: () =>
          import('./pages/editor/editor.page').then((m) => m.EditorPage),
      },
      {
        path: toPath(template(upcomingBaseRoute.management.documents)),
        loadComponent: () =>
          import('./pages/editor/editor.page').then((m) => m.EditorPage),
      },
      {
        path: toPath(template(upcomingBaseRoute.management)),
        redirectTo: template(upcomingBaseRoute.management.sources),
      },
      {
        path: toPath(template(upcomingBaseRoute.management)),
        redirectTo: template(upcomingBaseRoute.management.sources),
      },
    ],
  },
  {
    path: toPath(template(upcomingBaseRoute.events.countryCode)),
    loadComponent: () =>
      import('./pages/events/events.page').then((m) => m.EventsPage),
  },
  {
    path: toPath(template(upcomingBaseRoute.events.countryCode.region)),
    loadComponent: () =>
      import('./pages/events/events.page').then((m) => m.EventsPage),
  },
  {
    path: toPath(template(upcomingBaseRoute.events.countryCode.region.place)),
    loadComponent: () =>
      import('./pages/events/events.page').then((m) => m.EventsPage),
  },
  {
    path: toPath(
      template(upcomingBaseRoute.events.countryCode.region.place.dateTime),
    ),
    loadComponent: () =>
      import('./pages/events/events.page').then((m) => m.EventsPage),
  },
  // {
  //   path: toPath(
  //     template(
  //       upcomingBaseRoute.events.countryCode.region.place.dateTime.perimeter,
  //     ),
  //   ),
  //   loadComponent: () =>
  //     import('./pages/events/events.page').then((m) => m.EventsPage),
  // },
  {
    path: toPath(
      template(
        upcomingBaseRoute.events.countryCode.region.place.relativeDateTime,
      ),
    ),
    loadComponent: () =>
      import('./pages/events/events.page').then((m) => m.EventsPage),
  },
  // {
  //   path: toPath(
  //     template(
  //       upcomingBaseRoute.events.countryCode.region.place.relativeDateTime
  //         .perimeter,
  //     ),
  //   ),
  //   loadComponent: () =>
  //     import('./pages/events/events.page').then((m) => m.EventsPage),
  // },
  {
    // event/in/CH/Zurich/Affoltern%2520a.A./am/2024/11/02/details/7f2bee6c-be92-49b3-bbbe-aab1e207fa5c
    path: toPath(
      template(
        upcomingBaseRoute.events.countryCode.region.place.dateTime.eventId,
      ),
    ),
    loadComponent: () =>
      import('./pages/event/event.page').then((m) => m.EventPage),
  },
];
