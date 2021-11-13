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

export type MqArticleScore = {
  __typename?: 'MqArticleScore';
  correlationId: FieldWrapper<Scalars['String']>;
  url: FieldWrapper<Scalars['String']>;
  error: FieldWrapper<Scalars['Boolean']>;
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
};

export type MqAskArticleScore = {
  __typename?: 'MqAskArticleScore';
  correlationId: FieldWrapper<Scalars['String']>;
  url: FieldWrapper<Scalars['String']>;
};

export type MqAskReadability = {
  __typename?: 'MqAskReadability';
  correlationId: FieldWrapper<Scalars['String']>;
  url: FieldWrapper<Scalars['String']>;
  prerender: FieldWrapper<Scalars['Boolean']>;
};

export enum MqOperation {
  AskArticleScore = 'askArticleScore',
  ArticleScore = 'articleScore',
  ArticleScored = 'articleScored',
  AskReadability = 'askReadability',
  Readability = 'readability',
  ArticleChanged = 'articleChanged'
}

export type MqReadability = {
  __typename?: 'MqReadability';
  correlationId: FieldWrapper<Scalars['String']>;
  url: FieldWrapper<Scalars['String']>;
  error: FieldWrapper<Scalars['Boolean']>;
  readability?: Maybe<FieldWrapper<MqReadabilityData>>;
};

export type MqReadabilityData = {
  __typename?: 'MqReadabilityData';
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  byline?: Maybe<FieldWrapper<Scalars['String']>>;
  content?: Maybe<FieldWrapper<Scalars['String']>>;
  textContent?: Maybe<FieldWrapper<Scalars['String']>>;
  excerpt?: Maybe<FieldWrapper<Scalars['String']>>;
};
