import { bool, createRoutes, param, str } from 'typesafe-routes';

export const standaloneV2WebToFeedRoute = createRoutes({
  feed: {
    path: ['api', 'w2f'],
    query: [
      str('url'),
      str('link'),
      str('context'),
      str('date'),
      bool('dateIsEvent'),
      str('q'),
      str('out'),
      str('token'),
    ],
  },
});

export const standaloneV1WebToFeedRoute = createRoutes({
  feed: {
    path: ['feed'],
    query: [str('url'), str('pLink'), str('pContext')],
  },
});

export const standaloneV2FeedTransformRoute = createRoutes({
  feed: {
    path: ['api', 'tf'],
    query: [str('url'), str('q'), str('out'), str('token')],
  },
});

export const upperCaseStringParser = param({
  serialize: (value: string) => value.toUpperCase(),
  parse: (value: string) => value.toUpperCase(),
});
