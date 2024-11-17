import { NgModule } from '@angular/core';
import { ActivatedRoute, Params, RouterModule, Routes } from '@angular/router';
import dayjs, { Dayjs } from 'dayjs';
import { OpenStreetMapService } from '../../services/open-street-map.service';
import { NamedLatLon } from '../../types';
import { intParser, route } from 'typesafe-routes';
import { Parser } from 'typesafe-routes/build/parser';

export const perimeterUnit = 'Km';

export function parseDateFromUrl(params: Params): Dayjs {
  const { year, month, day } =
    homeRoute.children.countryCode.children.region.children.events.parseParams(
      params as any,
    );
  return dayjs(`${year}/${month}/${day}`, 'YYYY/MM/DD');
}

export async function parseLocationFromUrl(
  activatedRoute: ActivatedRoute,
  openStreetMapService: OpenStreetMapService,
): Promise<NamedLatLon> {
  const { countryCode } = homeRoute.children.countryCode.parseParams(
    activatedRoute.snapshot.params as any,
  );
  const { region } = homeRoute.children.countryCode.children.region.parseParams(
    activatedRoute.snapshot.params as any,
  );
  const { place } =
    homeRoute.children.countryCode.children.region.children.events.parseParams(
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
            events: route(
              ':place/am/:year/:month/:day/innerhalb/:perimeter',
              {
                place: strParser,
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
);

const routes: Routes = [
  {
    path: homeRoute.template,
    children: [
      // {
      //   path: '',
      // },
      {
        path: homeRoute.children.countryCode.template,
        children: [
          {
            path: '',
            loadChildren: () =>
              import('./events/events-page.module').then(
                (m) => m.EventsPageModule,
              ),
          },
          {
            // path: 'events/in/:state',
            path: homeRoute.children.countryCode.children.region.template,
            children: [
              {
                path: '',
                loadChildren: () =>
                  import('./events/events-page.module').then(
                    (m) => m.EventsPageModule,
                  ),
              },
              {
                // path: 'events/in/:state/:country/:place/am/:year/:month/:day/innerhalb/:perimeter',
                path: homeRoute.children.countryCode.children.region.children
                  .events.template,
                children: [
                  {
                    path: '',
                    loadChildren: () =>
                      import('./events/events-page.module').then(
                        (m) => m.EventsPageModule,
                      ),
                  },
                  {
                    // event/in/CH/Zurich/Affoltern%2520a.A./am/2024/11/02/details/7f2bee6c-be92-49b3-bbbe-aab1e207fa5c
                    path: homeRoute.children.countryCode.children.region
                      .children.events.children.eventId.template,
                    loadChildren: () =>
                      import('./event/event-page.module').then(
                        (m) => m.EventPageModule,
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
  {
    path: '**',
    redirectTo: '',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UpcomingProductRoutingModule {}
