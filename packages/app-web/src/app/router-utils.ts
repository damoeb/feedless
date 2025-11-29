// import { Parser } from 'typesafe-routes/build/parser';
//
// const parse =
//   (defaultValue = '') =>
//     (encoded: string): string =>
//       decodeURIComponent(encoded || defaultValue);
//
// const serialize = (defaultValue = '', encoding = false) =>
//   encoding
//     ? <Tv extends string>(value: Tv): string => encodeURIComponent(value || defaultValue)
//     : <Tv extends string>(value: Tv): string => value || defaultValue;
//
// export const strParser = (defaultValue = '', encoding = false): Parser<string> => ({
//   serialize: serialize(defaultValue, encoding),
//   parse: parse(defaultValue),
// });
//
// const serialize =
//   (defaultValue?: true | false | undefined) =>
//     (val: boolean = defaultValue): 'true' | 'false' | '' =>
//       val === true ? 'true' : val === false ? 'false' : '';
//
// const parse =
//   (defaultValue?: true | false | undefined) =>
//     (val: string): boolean =>
//       val === 'true' ? true : val === 'false' ? false : defaultValue;
//
// export const boolParser = (defaultValue: true | false | undefined = undefined): Parser<boolean> => ({
//   serialize: serialize(defaultValue),
//   parse: parse(defaultValue),
// });

import { bool, createRoutes, str } from 'typesafe-routes';

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
