import { RenderMode, ServerRoute } from '@angular/ssr';
import { template } from 'typesafe-routes';
import { toPath, upcomingBaseRoute } from './upcoming-product-routes';

export const serverRoutes: ServerRoute[] = [
  {
    path: '',
    renderMode: RenderMode.Server,
  },
  {
    path: toPath(
      template(upcomingBaseRoute.events.countryCode.region.place.dateTime),
    ),
    renderMode: RenderMode.Server,
  },
  {
    path: toPath(
      template(
        upcomingBaseRoute.events.countryCode.region.place.relativeDateTime,
      ),
    ),
    renderMode: RenderMode.Server,
  },
  {
    path: 'agb',
    renderMode: RenderMode.Server,
  },
  {
    path: 'ueber-uns',
    renderMode: RenderMode.Server,
  },
  {
    path: '**',
    renderMode: RenderMode.Client,
  },
];
