export type Maybe<T> = T | null;
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
  url: FieldWrapper<Scalars['String']>;
  reason: FieldWrapper<Scalars['String']>;
};

export type MqAskArticleScore = {
  __typename?: 'MqAskArticleScore';
  correlationId: FieldWrapper<Scalars['String']>;
  articleUrl: FieldWrapper<Scalars['String']>;
  feedId: FieldWrapper<Scalars['String']>;
};

export type MqAskReadability = {
  __typename?: 'MqAskReadability';
  correlationId: FieldWrapper<Scalars['String']>;
  url: FieldWrapper<Scalars['String']>;
  prerender: FieldWrapper<Scalars['Boolean']>;
  allowHarvestFailure: FieldWrapper<Scalars['Boolean']>;
};

export enum MqOperation {
  AskArticleScore = 'askArticleScore',
  AskReadability = 'askReadability',
  Readability = 'readability',
  ArticleChanged = 'articleChanged'
}

export type MqReadability = {
  __typename?: 'MqReadability';
  correlationId: FieldWrapper<Scalars['String']>;
  url: FieldWrapper<Scalars['String']>;
  readability?: Maybe<FieldWrapper<MqReadabilityData>>;
  allowHarvestFailure: FieldWrapper<Scalars['Boolean']>;
  harvestFailed: FieldWrapper<Scalars['Boolean']>;
  readabilityFailed: FieldWrapper<Scalars['Boolean']>;
  contentRaw?: Maybe<FieldWrapper<Scalars['String']>>;
  contentRawMime?: Maybe<FieldWrapper<Scalars['String']>>;
  prerender: FieldWrapper<Scalars['Boolean']>;
};

export type MqReadabilityData = {
  __typename?: 'MqReadabilityData';
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  byline?: Maybe<FieldWrapper<Scalars['String']>>;
  content?: Maybe<FieldWrapper<Scalars['String']>>;
  textContent?: Maybe<FieldWrapper<Scalars['String']>>;
  excerpt?: Maybe<FieldWrapper<Scalars['String']>>;
};
