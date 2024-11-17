import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { isNull, isUndefined } from 'lodash-es';
import { GqlVertical, GqlUpsertOrderMutation } from '../generated/graphql';

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
  [key: string | GqlVertical]: any | GqlVertical;
};

export type NamedLatLon = {
  lat: number;
  lon: number;
  place: string;
  displayName: string;
  index?: string;
  area: string;
  countryCode: string;
};
