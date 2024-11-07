import { NgModule } from '@angular/core';
import { ActivatedRoute, RouterModule, Routes } from '@angular/router';
import dayjs, { Dayjs } from 'dayjs';
import {
  OpenStreetMapService,
  OsmMatch,
} from '../../services/open-street-map.service';
import { NamedLatLon } from './places';

export const perimeterUnit = 'Km';

export function parseDateFromUrl(activatedRoute: ActivatedRoute): Dayjs {
  const { year, month, day } = activatedRoute.snapshot.params;
  if (year && month && day) {
    return dayjs(`${year}/${month}/${day}`, 'YYYY/MM/DD');
  }
  throw Error();
}

export function parsePerimeterFromUrl(
  activatedRoute: ActivatedRoute,
  fallback: number = 10,
): number {
  const { perimeter } = activatedRoute.snapshot.params;
  if (perimeter) {
    return parseInt(perimeter.replace(perimeterUnit, ''));
  }
  return fallback;
}

export async function parseLocationFromUrl(
  activatedRoute: ActivatedRoute,
  openStreetMapService: OpenStreetMapService,
): Promise<NamedLatLon> {
  const { state, country, place } = activatedRoute.snapshot.params;
  if (state && country && place) {
    const results = await openStreetMapService.searchByObject({
      country: decodeURIComponent(country),
      state: decodeURIComponent(state),
      place: decodeURIComponent(place),
    });
    if (results.length > 0) {
      return results[0];
    }
    throw Error();
  }
  throw Error();
}

const routes: Routes = [
  {
    path: 'events/in/:state/:country/:place/am/:year/:month/:day/innerhalb/:perimeter',
    loadChildren: () =>
      import('./events/events-page.module').then((m) => m.EventsPageModule),
  },
  {
    path: 'events/in',
    loadChildren: () =>
      import('./events/events-page.module').then((m) => m.EventsPageModule),
  },
  {
    // event/in/CH/Zurich/Affoltern%2520a.A./am/2024/11/02/details/7f2bee6c-be92-49b3-bbbe-aab1e207fa5c
    path: 'events/in/:state/:country/:place/am/:year/:month/:day/innerhalb/:perimeter/:eventId',
    loadChildren: () =>
      import('./event/event-page.module').then((m) => m.EventPageModule),
  },
  {
    path: '**',
    redirectTo: 'events/in',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UpcomingProductRoutingModule {}
