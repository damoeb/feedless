export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = {
  [K in keyof T]: T[K];
};
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & {
  [SubKey in K]?: Maybe<T[SubKey]>;
};
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & {
  [SubKey in K]: Maybe<T[SubKey]>;
};
export type FieldWrapper<T> = T;
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
};

export type HttpGetResponse = {
  __typename?: 'HttpGetResponse';
  contentType: FieldWrapper<Scalars['String']>;
  correlationId: FieldWrapper<Scalars['String']>;
  responseBody: FieldWrapper<Scalars['String']>;
  statusCode: FieldWrapper<Scalars['Int']>;
  url: FieldWrapper<Scalars['String']>;
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

export type MqAskPrerendering = {
  __typename?: 'MqAskPrerendering';
  correlationId: FieldWrapper<Scalars['String']>;
  url: FieldWrapper<Scalars['String']>;
};

export enum MqOperation {
  ArticleChanged = 'articleChanged',
  AskArticleScore = 'askArticleScore',
  AskReadability = 'askReadability',
  Readability = 'readability',
}

export type MqPrerenderingResponse = {
  __typename?: 'MqPrerenderingResponse';
  correlationId: FieldWrapper<Scalars['String']>;
  data?: Maybe<FieldWrapper<Scalars['String']>>;
  error: FieldWrapper<Scalars['Boolean']>;
  errorMessage?: Maybe<FieldWrapper<Scalars['String']>>;
  url: FieldWrapper<Scalars['String']>;
};
