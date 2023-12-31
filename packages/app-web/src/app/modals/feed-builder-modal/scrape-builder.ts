import { isNull, isUndefined } from 'lodash-es';

export interface Field {
  type: 'text' | 'markup' | 'base64' | 'url' | 'date';
  name: string;
}

export function isDefined(v: any | undefined): boolean {
  return !isNull(v) && !isUndefined(v);
}

export type ResponseMapper =
  | 'nativeFeed'
  | 'readability'
  | 'feed'
  | 'fragment'
  | 'pageScreenshot'
  | 'pageMarkup';
