import { Routes } from '@angular/router';
import { booleanParser, intParser, route } from 'typesafe-routes';
import { strParser } from '../../products/default-routes';

export const FEED_BUILDER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./feed-builder.page').then((m) => m.FeedBuilderPage),
  },
];

export const standaloneV2WebToFeedRoute = route(
  'api/w2f&:url&:link&:context&:q&:out&:date&:dateIsEvent',
  {
    url: strParser,
    link: strParser,
    context: strParser,
    date: strParser,
    dateIsEvent: booleanParser,
    q: strParser,
    out: strParser,
  },
  {},
);

export const standaloneV1WebToFeedRoute = route(
  'feed&:url&:pContext&:pLink',
  {
    url: strParser,
    pLink: strParser,
    pContext: strParser,
  },
  {},
);

export const standaloneV2FeedTransformRoute = route(
  'api/tf&:url&:q&:out',
  {
    url: strParser,
    q: strParser,
    out: strParser,
  },
  {},
);
