import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { isNull, isUndefined } from 'lodash-es';
import {
  GqlBoundingBoxInput,
  GqlUpsertOrderMutation,
  GqlVertical,
  GqlXyPosition,
} from '@feedless/graphql-api';

export type Nullable<T> = T | null | undefined;

export function isNonNull<T>(value: T): value is NonNullable<T> {
  return value !== null && value !== undefined;
}

export type ArrayElement<ArrayType extends readonly unknown[]> =
  ArrayType extends readonly (infer ElementType)[] ? ElementType : never;

// export type GetElementType<T extends any[]> = T extends (infer U)[] ? U : never;

type AnyPrimitive = string | number | boolean;
export type TypedFormGroup<TYPE> = {
  [K in keyof TYPE]: TYPE[K] extends AnyPrimitive
    ? FormControl<TYPE[K] | null>
    : TYPE[K] extends Array<infer U>
      ? U extends AnyPrimitive
        ? FormArray<FormControl<U | null>>
        : FormArray<FormGroup<TypedFormGroup<U>>>
      : FormGroup<TypedFormGroup<TYPE[K]>>;
};

export type DeepPartial<T> = T extends object
  ? {
      [P in keyof T]?: DeepPartial<T[P]>;
    }
  : T;

export type NestedKeys<T, Prev extends string = ''> = {
  [K in keyof T]: T[K] extends object
    ?
        | `${Prev}${Extract<K, string>}`
        | NestedKeys<T[K], `${Prev}${Extract<K, string>}.`>
    : `${Prev}${Extract<K, string>}`;
}[keyof T];

type Split<S extends string, D extends string> = string extends S
  ? string[]
  : S extends ''
    ? []
    : S extends `${infer T}${D}${infer U}`
      ? [T, ...Split<U, D>]
      : [S];

export type TypeAtPath<T, P extends string> =
  Split<P, '.'> extends [infer Head extends keyof T, ...infer Tail]
    ? Tail extends []
      ? T[Head]
      : T[Head] extends object
        ? TypeAtPath<T[Head], Tail[number] & string>
        : never
    : never;

export function isDefined(v: any | undefined): boolean {
  return !isNull(v) && !isUndefined(v);
}

export type Order = GqlUpsertOrderMutation['upsertOrder'];

export type VerticalAppConfig = {
  apiUrl: string;
  attributionHtml: string;
  indexHtmlAddition?: string;
  product: GqlVertical;
  offlineSupport?: boolean;
  operatorName?: string;
  operatorEmail?: string;
  operatorAddress?: string;
  [key: string | GqlVertical]: any | GqlVertical;
};

export type NamedLatLon = {
  lat: number;
  lng: number;
  place: string;
  zip?: string;
  displayName: string;
  index?: string;
  area: string;
  countryCode: string;
};

export type LatLng = {
  lat: number;
  lng: number;
};

export type XyPosition = GqlXyPosition;

interface Viewport {
  width: number;
  height: number;
}

export interface Embeddable {
  mimeType: string;
  data: string;
  url: string;
  viewport?: Viewport;
}

export type BoundingBox = GqlBoundingBoxInput;

export type VerticalId =
  | 'changeTracker'
  | 'rss-proxy'
  | 'visual-diff'
  | 'reader'
  | 'upcoming'
  | 'digest'
  | 'untold'
  | 'feedless';

// export type AppStage = 'idea' | 'development' | 'alpha' | 'stable'

export type CostItem = {
  name: string;
  unit: string;
  value: {
    price?: number;
    discount?: number;
  };
};

export type AppLink = {
  url: string;
  allow: boolean;
};

export type VerticalSpec = {
  id: VerticalId;
  product: GqlVertical;
  domain?: string;
  localSetupBeforeMarkup?: string;
  localSetupBash: string;
  localSetupAfterMarkup?: string;
  title: string;
  titleHtml: string;
  pageTitle: string;
  offlineSupport?: boolean;
  subtitle: string;
  summary: string;
  // stage: AppStage;
  version?: (number | string)[];
  phase: 'planning' | 'design' | 'development' | 'alpha' | 'beta' | 'rc' | 'ga';
  descriptionMarkdown: string;
  descriptionHtml?: string;
  videoUrl?: string;
  costs?: CostItem[];
  links: AppLink[];
  features: string[];
};
