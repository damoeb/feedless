export type ArrayElement<ArrayType extends readonly unknown[]> =
  ArrayType extends readonly (infer ElementType)[] ? ElementType : never;

// export type GetElementType<T extends any[]> = T extends (infer U)[] ? U : never;
