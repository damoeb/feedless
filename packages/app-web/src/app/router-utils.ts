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

import { booleanParser, intParser, route } from 'typesafe-routes';
import { strParser } from './products/default-routes';

export const standaloneV2WebToFeedRoute = route(
  'api/w2f&:url&:link&:context&:q&:out&:date&:dateIsEvent&:ts',
  {
    url: strParser,
    link: strParser,
    context: strParser,
    date: strParser,
    dateIsEvent: booleanParser,
    q: strParser,
    out: strParser,
    ts: intParser
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
  'api/tf&:url&:q&:out&:ts',
  {
    url: strParser,
    q: strParser,
    out: strParser,
    ts: intParser
  },
  {},
);
