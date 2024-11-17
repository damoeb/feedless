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
