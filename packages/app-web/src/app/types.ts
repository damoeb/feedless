import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { isNull, isUndefined } from 'lodash-es';
import { GqlUpsertOrderMutation } from '../generated/graphql';

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
