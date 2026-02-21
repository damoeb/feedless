import { RenderMode, ServerRoute } from '@angular/ssr';
import { template } from 'typesafe-routes';
import { upcomingBaseRoute } from './upcoming-product-routes';

export const serverRoutes: ServerRoute[] = [
  {
    path: '',
    renderMode: RenderMode.Server,
  },
  {
    path: template(upcomingBaseRoute._.events.countryCode),
    renderMode: RenderMode.Server,
  },
  {
    path: template(upcomingBaseRoute._.events.countryCode.region),
    renderMode: RenderMode.Server,
  },
  {
    path: template(upcomingBaseRoute._.events.countryCode.region.place),
    renderMode: RenderMode.Server,
  },
  {
    path: template(
      upcomingBaseRoute._.events.countryCode.region.place.dateTime,
    ),
    renderMode: RenderMode.Server,
  },
  {
    path: template(
      upcomingBaseRoute._.events.countryCode.region.place.relativeDateTime,
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
