export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
export type FieldWrapper<T> = T;
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
};

export type MqArticleChange = {
  __typename?: 'MqArticleChange';
  correlationId: FieldWrapper<Scalars['String']>;
  reason: FieldWrapper<Scalars['String']>;
  url: FieldWrapper<Scalars['String']>;
};

export type MqAskArticleScore = {
  __typename?: 'MqAskArticleScore';
  articleUrl: FieldWrapper<Scalars['String']>;
  correlationId: FieldWrapper<Scalars['String']>;
  feedId: FieldWrapper<Scalars['String']>;
};

export type MqAskReadability = {
  __typename?: 'MqAskReadability';
  allowHarvestFailure: FieldWrapper<Scalars['Boolean']>;
  correlationId: FieldWrapper<Scalars['String']>;
  prerender: FieldWrapper<Scalars['Boolean']>;
  url: FieldWrapper<Scalars['String']>;
};

export enum MqOperation {
  ArticleChanged = 'articleChanged',
  AskArticleScore = 'askArticleScore',
  AskReadability = 'askReadability',
  Readability = 'readability'
}

export type MqReadability = {
  __typename?: 'MqReadability';
  allowHarvestFailure: FieldWrapper<Scalars['Boolean']>;
  contentRaw?: Maybe<FieldWrapper<Scalars['String']>>;
  contentRawMime?: Maybe<FieldWrapper<Scalars['String']>>;
  correlationId: FieldWrapper<Scalars['String']>;
  harvestFailed: FieldWrapper<Scalars['Boolean']>;
  prerender: FieldWrapper<Scalars['Boolean']>;
  readability?: Maybe<FieldWrapper<MqReadabilityData>>;
  readabilityFailed: FieldWrapper<Scalars['Boolean']>;
  url: FieldWrapper<Scalars['String']>;
};

export type MqReadabilityData = {
  __typename?: 'MqReadabilityData';
  byline?: Maybe<FieldWrapper<Scalars['String']>>;
  content?: Maybe<FieldWrapper<Scalars['String']>>;
  excerpt?: Maybe<FieldWrapper<Scalars['String']>>;
  textContent?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
};
