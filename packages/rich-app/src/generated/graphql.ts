import { gql } from 'apollo-angular';
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
  /** The javascript `Date` as string. Type represents date and time as the ISO Date string. */
  DateTime: any;
  /** The `JSON` scalar type represents JSON values as specified by [ECMA-404](http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf). */
  JSON: any;
};



export type GqlAffectedRowsOutput = {
  __typename?: 'AffectedRowsOutput';
  count: FieldWrapper<Scalars['Int']>;
};

export type GqlAggregateArticle = {
  __typename?: 'AggregateArticle';
  _count?: Maybe<FieldWrapper<GqlArticleCountAggregate>>;
  _avg?: Maybe<FieldWrapper<GqlArticleAvgAggregate>>;
  _sum?: Maybe<FieldWrapper<GqlArticleSumAggregate>>;
  _min?: Maybe<FieldWrapper<GqlArticleMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlArticleMaxAggregate>>;
};

export type GqlAggregateArticleRef = {
  __typename?: 'AggregateArticleRef';
  _count?: Maybe<FieldWrapper<GqlArticleRefCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlArticleRefMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlArticleRefMaxAggregate>>;
};

export type GqlAggregateBucket = {
  __typename?: 'AggregateBucket';
  _count?: Maybe<FieldWrapper<GqlBucketCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlBucketMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlBucketMaxAggregate>>;
};

export type GqlAggregateEntryPostProcessor = {
  __typename?: 'AggregateEntryPostProcessor';
  _count?: Maybe<FieldWrapper<GqlEntryPostProcessorCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlEntryPostProcessorMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlEntryPostProcessorMaxAggregate>>;
};

export type GqlAggregateFeed = {
  __typename?: 'AggregateFeed';
  _count?: Maybe<FieldWrapper<GqlFeedCountAggregate>>;
  _avg?: Maybe<FieldWrapper<GqlFeedAvgAggregate>>;
  _sum?: Maybe<FieldWrapper<GqlFeedSumAggregate>>;
  _min?: Maybe<FieldWrapper<GqlFeedMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlFeedMaxAggregate>>;
};

export type GqlAggregateFeedEvent = {
  __typename?: 'AggregateFeedEvent';
  _count?: Maybe<FieldWrapper<GqlFeedEventCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlFeedEventMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlFeedEventMaxAggregate>>;
};

export type GqlAggregateReleaseThrottle = {
  __typename?: 'AggregateReleaseThrottle';
  _count?: Maybe<FieldWrapper<GqlReleaseThrottleCountAggregate>>;
  _avg?: Maybe<FieldWrapper<GqlReleaseThrottleAvgAggregate>>;
  _sum?: Maybe<FieldWrapper<GqlReleaseThrottleSumAggregate>>;
  _min?: Maybe<FieldWrapper<GqlReleaseThrottleMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlReleaseThrottleMaxAggregate>>;
};

export type GqlAggregateStream = {
  __typename?: 'AggregateStream';
  _count?: Maybe<FieldWrapper<GqlStreamCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlStreamMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlStreamMaxAggregate>>;
};

export type GqlAggregateSubscription = {
  __typename?: 'AggregateSubscription';
  _count?: Maybe<FieldWrapper<GqlSubscriptionCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlSubscriptionMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlSubscriptionMaxAggregate>>;
};

export type GqlAggregateUser = {
  __typename?: 'AggregateUser';
  _count?: Maybe<FieldWrapper<GqlUserCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlUserMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlUserMaxAggregate>>;
};

export type GqlAggregateUserArticle = {
  __typename?: 'AggregateUserArticle';
  _count?: Maybe<FieldWrapper<GqlUserArticleCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlUserArticleMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlUserArticleMaxAggregate>>;
};

export type GqlAggregateUserFeed = {
  __typename?: 'AggregateUserFeed';
  _count?: Maybe<FieldWrapper<GqlUserFeedCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlUserFeedMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlUserFeedMaxAggregate>>;
};

export type GqlArticle = {
  __typename?: 'Article';
  id: FieldWrapper<Scalars['String']>;
  date_published: FieldWrapper<Scalars['DateTime']>;
  date_modified?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  comment_feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  url: FieldWrapper<Scalars['String']>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title: FieldWrapper<Scalars['String']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
  content_text: FieldWrapper<Scalars['String']>;
  content_html?: Maybe<FieldWrapper<Scalars['String']>>;
  enclosure?: Maybe<FieldWrapper<Scalars['JSON']>>;
  articleRef: Array<FieldWrapper<GqlArticleRef>>;
};


export type GqlArticleArticleRefArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlArticleRefOrderByInput>>;
  cursor?: Maybe<GqlArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleRefScalarFieldEnum>>;
};

export type GqlArticleAvgAggregate = {
  __typename?: 'ArticleAvgAggregate';
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
};

export type GqlArticleCountAggregate = {
  __typename?: 'ArticleCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  date_published: FieldWrapper<Scalars['Int']>;
  date_modified: FieldWrapper<Scalars['Int']>;
  comment_feed_url: FieldWrapper<Scalars['Int']>;
  url: FieldWrapper<Scalars['Int']>;
  author: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  tags: FieldWrapper<Scalars['Int']>;
  score: FieldWrapper<Scalars['Int']>;
  content_text: FieldWrapper<Scalars['Int']>;
  content_html: FieldWrapper<Scalars['Int']>;
  enclosure: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlArticleCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  date_published?: Maybe<Scalars['DateTime']>;
  date_modified?: Maybe<Scalars['DateTime']>;
  comment_feed_url?: Maybe<Scalars['String']>;
  url: Scalars['String'];
  author?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  score?: Maybe<Scalars['Float']>;
  content_text: Scalars['String'];
  content_html?: Maybe<Scalars['String']>;
  enclosure?: Maybe<Scalars['JSON']>;
  articleRef?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleInput>;
};

export type GqlArticleCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  date_published?: Maybe<Scalars['DateTime']>;
  date_modified?: Maybe<Scalars['DateTime']>;
  comment_feed_url?: Maybe<Scalars['String']>;
  url: Scalars['String'];
  author?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  score?: Maybe<Scalars['Float']>;
  content_text: Scalars['String'];
  content_html?: Maybe<Scalars['String']>;
  enclosure?: Maybe<Scalars['JSON']>;
};

export type GqlArticleCreateNestedOneWithoutArticleRefInput = {
  create?: Maybe<GqlArticleCreateWithoutArticleRefInput>;
  connectOrCreate?: Maybe<GqlArticleCreateOrConnectWithoutArticleRefInput>;
  connect?: Maybe<GqlArticleWhereUniqueInput>;
};

export type GqlArticleCreateOrConnectWithoutArticleRefInput = {
  where: GqlArticleWhereUniqueInput;
  create: GqlArticleCreateWithoutArticleRefInput;
};

export type GqlArticleCreateWithoutArticleRefInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  date_published?: Maybe<Scalars['DateTime']>;
  date_modified?: Maybe<Scalars['DateTime']>;
  comment_feed_url?: Maybe<Scalars['String']>;
  url: Scalars['String'];
  author?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  score?: Maybe<Scalars['Float']>;
  content_text: Scalars['String'];
  content_html?: Maybe<Scalars['String']>;
  enclosure?: Maybe<Scalars['JSON']>;
};

export type GqlArticleGroupBy = {
  __typename?: 'ArticleGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  date_published: FieldWrapper<Scalars['DateTime']>;
  date_modified?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  comment_feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  url: FieldWrapper<Scalars['String']>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title: FieldWrapper<Scalars['String']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
  content_text: FieldWrapper<Scalars['String']>;
  content_html?: Maybe<FieldWrapper<Scalars['String']>>;
  enclosure?: Maybe<FieldWrapper<Scalars['JSON']>>;
  _count?: Maybe<FieldWrapper<GqlArticleCountAggregate>>;
  _avg?: Maybe<FieldWrapper<GqlArticleAvgAggregate>>;
  _sum?: Maybe<FieldWrapper<GqlArticleSumAggregate>>;
  _min?: Maybe<FieldWrapper<GqlArticleMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlArticleMaxAggregate>>;
};

export type GqlArticleMaxAggregate = {
  __typename?: 'ArticleMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  date_published?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  date_modified?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  comment_feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
  content_text?: Maybe<FieldWrapper<Scalars['String']>>;
  content_html?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleMinAggregate = {
  __typename?: 'ArticleMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  date_published?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  date_modified?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  comment_feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
  content_text?: Maybe<FieldWrapper<Scalars['String']>>;
  content_html?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  date_published?: Maybe<GqlSortOrder>;
  date_modified?: Maybe<GqlSortOrder>;
  comment_feed_url?: Maybe<GqlSortOrder>;
  url?: Maybe<GqlSortOrder>;
  author?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  tags?: Maybe<GqlSortOrder>;
  score?: Maybe<GqlSortOrder>;
  content_text?: Maybe<GqlSortOrder>;
  content_html?: Maybe<GqlSortOrder>;
  enclosure?: Maybe<GqlSortOrder>;
};

export type GqlArticleRef = {
  __typename?: 'ArticleRef';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  favored: FieldWrapper<Scalars['Boolean']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  articleId: FieldWrapper<Scalars['String']>;
  articleRefId?: Maybe<FieldWrapper<Scalars['String']>>;
  owner: FieldWrapper<GqlUser>;
  article: FieldWrapper<GqlArticle>;
  related: Array<FieldWrapper<GqlArticleRef>>;
  articleRef?: Maybe<FieldWrapper<GqlArticleRef>>;
  stream: Array<FieldWrapper<GqlStream>>;
};


export type GqlArticleRefRelatedArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlArticleRefOrderByInput>>;
  cursor?: Maybe<GqlArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleRefScalarFieldEnum>>;
};


export type GqlArticleRefStreamArgs = {
  where?: Maybe<GqlStreamWhereInput>;
  orderBy?: Maybe<Array<GqlStreamOrderByInput>>;
  cursor?: Maybe<GqlStreamWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlStreamScalarFieldEnum>>;
};

export type GqlArticleRefCountAggregate = {
  __typename?: 'ArticleRefCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  favored: FieldWrapper<Scalars['Int']>;
  tags: FieldWrapper<Scalars['Int']>;
  articleId: FieldWrapper<Scalars['Int']>;
  articleRefId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlArticleRefCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutArticleRefsInput>;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  related?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefCreateNestedOneWithoutRelatedInput>;
  stream?: Maybe<GqlStreamCreateNestedManyWithoutArticleRefsInput>;
};

export type GqlArticleRefCreateManyArticleInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  ownerId?: Maybe<Scalars['String']>;
  favored?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  articleRefId?: Maybe<Scalars['String']>;
};

export type GqlArticleRefCreateManyArticleInputEnvelope = {
  data: Array<GqlArticleRefCreateManyArticleInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlArticleRefCreateManyArticleRefInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  ownerId?: Maybe<Scalars['String']>;
  favored?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  articleId: Scalars['String'];
};

export type GqlArticleRefCreateManyArticleRefInputEnvelope = {
  data: Array<GqlArticleRefCreateManyArticleRefInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlArticleRefCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  ownerId?: Maybe<Scalars['String']>;
  favored?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  articleId: Scalars['String'];
  articleRefId?: Maybe<Scalars['String']>;
};

export type GqlArticleRefCreateManyOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  articleId: Scalars['String'];
  articleRefId?: Maybe<Scalars['String']>;
};

export type GqlArticleRefCreateManyOwnerInputEnvelope = {
  data: Array<GqlArticleRefCreateManyOwnerInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlArticleRefCreateNestedManyWithoutArticleInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutArticleInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutArticleInput>>;
  createMany?: Maybe<GqlArticleRefCreateManyArticleInputEnvelope>;
  connect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
};

export type GqlArticleRefCreateNestedManyWithoutArticleRefInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutArticleRefInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutArticleRefInput>>;
  createMany?: Maybe<GqlArticleRefCreateManyArticleRefInputEnvelope>;
  connect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
};

export type GqlArticleRefCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutOwnerInput>>;
  createMany?: Maybe<GqlArticleRefCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
};

export type GqlArticleRefCreateNestedManyWithoutStreamInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutStreamInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutStreamInput>>;
  connect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
};

export type GqlArticleRefCreateNestedOneWithoutRelatedInput = {
  create?: Maybe<GqlArticleRefCreateWithoutRelatedInput>;
  connectOrCreate?: Maybe<GqlArticleRefCreateOrConnectWithoutRelatedInput>;
  connect?: Maybe<GqlArticleRefWhereUniqueInput>;
};

export type GqlArticleRefCreateOrConnectWithoutArticleInput = {
  where: GqlArticleRefWhereUniqueInput;
  create: GqlArticleRefCreateWithoutArticleInput;
};

export type GqlArticleRefCreateOrConnectWithoutArticleRefInput = {
  where: GqlArticleRefWhereUniqueInput;
  create: GqlArticleRefCreateWithoutArticleRefInput;
};

export type GqlArticleRefCreateOrConnectWithoutOwnerInput = {
  where: GqlArticleRefWhereUniqueInput;
  create: GqlArticleRefCreateWithoutOwnerInput;
};

export type GqlArticleRefCreateOrConnectWithoutRelatedInput = {
  where: GqlArticleRefWhereUniqueInput;
  create: GqlArticleRefCreateWithoutRelatedInput;
};

export type GqlArticleRefCreateOrConnectWithoutStreamInput = {
  where: GqlArticleRefWhereUniqueInput;
  create: GqlArticleRefCreateWithoutStreamInput;
};

export type GqlArticleRefCreateWithoutArticleInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutArticleRefsInput>;
  related?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefCreateNestedOneWithoutRelatedInput>;
  stream?: Maybe<GqlStreamCreateNestedManyWithoutArticleRefsInput>;
};

export type GqlArticleRefCreateWithoutArticleRefInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutArticleRefsInput>;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  related?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleRefInput>;
  stream?: Maybe<GqlStreamCreateNestedManyWithoutArticleRefsInput>;
};

export type GqlArticleRefCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  related?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefCreateNestedOneWithoutRelatedInput>;
  stream?: Maybe<GqlStreamCreateNestedManyWithoutArticleRefsInput>;
};

export type GqlArticleRefCreateWithoutRelatedInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutArticleRefsInput>;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  articleRef?: Maybe<GqlArticleRefCreateNestedOneWithoutRelatedInput>;
  stream?: Maybe<GqlStreamCreateNestedManyWithoutArticleRefsInput>;
};

export type GqlArticleRefCreateWithoutStreamInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutArticleRefsInput>;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  related?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefCreateNestedOneWithoutRelatedInput>;
};

export type GqlArticleRefGroupBy = {
  __typename?: 'ArticleRefGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  favored: FieldWrapper<Scalars['Boolean']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  articleId: FieldWrapper<Scalars['String']>;
  articleRefId?: Maybe<FieldWrapper<Scalars['String']>>;
  _count?: Maybe<FieldWrapper<GqlArticleRefCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlArticleRefMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlArticleRefMaxAggregate>>;
};

export type GqlArticleRefListRelationFilter = {
  every?: Maybe<GqlArticleRefWhereInput>;
  some?: Maybe<GqlArticleRefWhereInput>;
  none?: Maybe<GqlArticleRefWhereInput>;
};

export type GqlArticleRefMaxAggregate = {
  __typename?: 'ArticleRefMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  favored?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  articleId?: Maybe<FieldWrapper<Scalars['String']>>;
  articleRefId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleRefMinAggregate = {
  __typename?: 'ArticleRefMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  favored?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  articleId?: Maybe<FieldWrapper<Scalars['String']>>;
  articleRefId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleRefOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  favored?: Maybe<GqlSortOrder>;
  tags?: Maybe<GqlSortOrder>;
  articleId?: Maybe<GqlSortOrder>;
  articleRefId?: Maybe<GqlSortOrder>;
};

export type GqlArticleRefRelationFilter = {
  is?: Maybe<GqlArticleRefWhereInput>;
  isNot?: Maybe<GqlArticleRefWhereInput>;
};

export enum GqlArticleRefScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  OwnerId = 'ownerId',
  Favored = 'favored',
  Tags = 'tags',
  ArticleId = 'articleId',
  ArticleRefId = 'articleRefId'
}

export type GqlArticleRefScalarWhereInput = {
  AND?: Maybe<Array<GqlArticleRefScalarWhereInput>>;
  OR?: Maybe<Array<GqlArticleRefScalarWhereInput>>;
  NOT?: Maybe<Array<GqlArticleRefScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  favored?: Maybe<GqlBoolFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  articleId?: Maybe<GqlStringFilter>;
  articleRefId?: Maybe<GqlStringNullableFilter>;
};

export type GqlArticleRefScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlArticleRefScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlArticleRefScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlArticleRefScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
  favored?: Maybe<GqlBoolWithAggregatesFilter>;
  tags?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  articleId?: Maybe<GqlStringWithAggregatesFilter>;
  articleRefId?: Maybe<GqlStringNullableWithAggregatesFilter>;
};

export type GqlArticleRefUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  related?: Maybe<GqlArticleRefUpdateManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefUpdateOneWithoutRelatedInput>;
  stream?: Maybe<GqlStreamUpdateManyWithoutArticleRefsInput>;
};

export type GqlArticleRefUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
};

export type GqlArticleRefUpdateManyWithWhereWithoutArticleInput = {
  where: GqlArticleRefScalarWhereInput;
  data: GqlArticleRefUpdateManyMutationInput;
};

export type GqlArticleRefUpdateManyWithWhereWithoutArticleRefInput = {
  where: GqlArticleRefScalarWhereInput;
  data: GqlArticleRefUpdateManyMutationInput;
};

export type GqlArticleRefUpdateManyWithWhereWithoutOwnerInput = {
  where: GqlArticleRefScalarWhereInput;
  data: GqlArticleRefUpdateManyMutationInput;
};

export type GqlArticleRefUpdateManyWithWhereWithoutStreamInput = {
  where: GqlArticleRefScalarWhereInput;
  data: GqlArticleRefUpdateManyMutationInput;
};

export type GqlArticleRefUpdateManyWithoutArticleInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutArticleInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutArticleInput>>;
  upsert?: Maybe<Array<GqlArticleRefUpsertWithWhereUniqueWithoutArticleInput>>;
  createMany?: Maybe<GqlArticleRefCreateManyArticleInputEnvelope>;
  connect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  set?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  delete?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  update?: Maybe<Array<GqlArticleRefUpdateWithWhereUniqueWithoutArticleInput>>;
  updateMany?: Maybe<Array<GqlArticleRefUpdateManyWithWhereWithoutArticleInput>>;
  deleteMany?: Maybe<Array<GqlArticleRefScalarWhereInput>>;
};

export type GqlArticleRefUpdateManyWithoutArticleRefInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutArticleRefInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutArticleRefInput>>;
  upsert?: Maybe<Array<GqlArticleRefUpsertWithWhereUniqueWithoutArticleRefInput>>;
  createMany?: Maybe<GqlArticleRefCreateManyArticleRefInputEnvelope>;
  connect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  set?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  delete?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  update?: Maybe<Array<GqlArticleRefUpdateWithWhereUniqueWithoutArticleRefInput>>;
  updateMany?: Maybe<Array<GqlArticleRefUpdateManyWithWhereWithoutArticleRefInput>>;
  deleteMany?: Maybe<Array<GqlArticleRefScalarWhereInput>>;
};

export type GqlArticleRefUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<GqlArticleRefUpsertWithWhereUniqueWithoutOwnerInput>>;
  createMany?: Maybe<GqlArticleRefCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  set?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  delete?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  update?: Maybe<Array<GqlArticleRefUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<GqlArticleRefUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<GqlArticleRefScalarWhereInput>>;
};

export type GqlArticleRefUpdateManyWithoutStreamInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutStreamInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutStreamInput>>;
  upsert?: Maybe<Array<GqlArticleRefUpsertWithWhereUniqueWithoutStreamInput>>;
  connect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  set?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  delete?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  update?: Maybe<Array<GqlArticleRefUpdateWithWhereUniqueWithoutStreamInput>>;
  updateMany?: Maybe<Array<GqlArticleRefUpdateManyWithWhereWithoutStreamInput>>;
  deleteMany?: Maybe<Array<GqlArticleRefScalarWhereInput>>;
};

export type GqlArticleRefUpdateOneWithoutRelatedInput = {
  create?: Maybe<GqlArticleRefCreateWithoutRelatedInput>;
  connectOrCreate?: Maybe<GqlArticleRefCreateOrConnectWithoutRelatedInput>;
  upsert?: Maybe<GqlArticleRefUpsertWithoutRelatedInput>;
  connect?: Maybe<GqlArticleRefWhereUniqueInput>;
  disconnect?: Maybe<Scalars['Boolean']>;
  delete?: Maybe<Scalars['Boolean']>;
  update?: Maybe<GqlArticleRefUpdateWithoutRelatedInput>;
};

export type GqlArticleRefUpdateWithWhereUniqueWithoutArticleInput = {
  where: GqlArticleRefWhereUniqueInput;
  data: GqlArticleRefUpdateWithoutArticleInput;
};

export type GqlArticleRefUpdateWithWhereUniqueWithoutArticleRefInput = {
  where: GqlArticleRefWhereUniqueInput;
  data: GqlArticleRefUpdateWithoutArticleRefInput;
};

export type GqlArticleRefUpdateWithWhereUniqueWithoutOwnerInput = {
  where: GqlArticleRefWhereUniqueInput;
  data: GqlArticleRefUpdateWithoutOwnerInput;
};

export type GqlArticleRefUpdateWithWhereUniqueWithoutStreamInput = {
  where: GqlArticleRefWhereUniqueInput;
  data: GqlArticleRefUpdateWithoutStreamInput;
};

export type GqlArticleRefUpdateWithoutArticleInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  related?: Maybe<GqlArticleRefUpdateManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefUpdateOneWithoutRelatedInput>;
  stream?: Maybe<GqlStreamUpdateManyWithoutArticleRefsInput>;
};

export type GqlArticleRefUpdateWithoutArticleRefInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  related?: Maybe<GqlArticleRefUpdateManyWithoutArticleRefInput>;
  stream?: Maybe<GqlStreamUpdateManyWithoutArticleRefsInput>;
};

export type GqlArticleRefUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  related?: Maybe<GqlArticleRefUpdateManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefUpdateOneWithoutRelatedInput>;
  stream?: Maybe<GqlStreamUpdateManyWithoutArticleRefsInput>;
};

export type GqlArticleRefUpdateWithoutRelatedInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefUpdateOneWithoutRelatedInput>;
  stream?: Maybe<GqlStreamUpdateManyWithoutArticleRefsInput>;
};

export type GqlArticleRefUpdateWithoutStreamInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  related?: Maybe<GqlArticleRefUpdateManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefUpdateOneWithoutRelatedInput>;
};

export type GqlArticleRefUpsertWithWhereUniqueWithoutArticleInput = {
  where: GqlArticleRefWhereUniqueInput;
  update: GqlArticleRefUpdateWithoutArticleInput;
  create: GqlArticleRefCreateWithoutArticleInput;
};

export type GqlArticleRefUpsertWithWhereUniqueWithoutArticleRefInput = {
  where: GqlArticleRefWhereUniqueInput;
  update: GqlArticleRefUpdateWithoutArticleRefInput;
  create: GqlArticleRefCreateWithoutArticleRefInput;
};

export type GqlArticleRefUpsertWithWhereUniqueWithoutOwnerInput = {
  where: GqlArticleRefWhereUniqueInput;
  update: GqlArticleRefUpdateWithoutOwnerInput;
  create: GqlArticleRefCreateWithoutOwnerInput;
};

export type GqlArticleRefUpsertWithWhereUniqueWithoutStreamInput = {
  where: GqlArticleRefWhereUniqueInput;
  update: GqlArticleRefUpdateWithoutStreamInput;
  create: GqlArticleRefCreateWithoutStreamInput;
};

export type GqlArticleRefUpsertWithoutRelatedInput = {
  update: GqlArticleRefUpdateWithoutRelatedInput;
  create: GqlArticleRefCreateWithoutRelatedInput;
};

export type GqlArticleRefWhereInput = {
  AND?: Maybe<Array<GqlArticleRefWhereInput>>;
  OR?: Maybe<Array<GqlArticleRefWhereInput>>;
  NOT?: Maybe<Array<GqlArticleRefWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  owner?: Maybe<GqlUserRelationFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  favored?: Maybe<GqlBoolFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  article?: Maybe<GqlArticleRelationFilter>;
  articleId?: Maybe<GqlStringFilter>;
  related?: Maybe<GqlArticleRefListRelationFilter>;
  articleRef?: Maybe<GqlArticleRefRelationFilter>;
  articleRefId?: Maybe<GqlStringNullableFilter>;
  stream?: Maybe<GqlStreamListRelationFilter>;
};

export type GqlArticleRefWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlArticleRelationFilter = {
  is?: Maybe<GqlArticleWhereInput>;
  isNot?: Maybe<GqlArticleWhereInput>;
};

export enum GqlArticleScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  DatePublished = 'date_published',
  DateModified = 'date_modified',
  CommentFeedUrl = 'comment_feed_url',
  Url = 'url',
  Author = 'author',
  Title = 'title',
  Tags = 'tags',
  Score = 'score',
  ContentText = 'content_text',
  ContentHtml = 'content_html',
  Enclosure = 'enclosure'
}

export type GqlArticleScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlArticleScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlArticleScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlArticleScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  date_published?: Maybe<GqlDateTimeWithAggregatesFilter>;
  date_modified?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
  comment_feed_url?: Maybe<GqlStringNullableWithAggregatesFilter>;
  url?: Maybe<GqlStringWithAggregatesFilter>;
  author?: Maybe<GqlStringNullableWithAggregatesFilter>;
  title?: Maybe<GqlStringWithAggregatesFilter>;
  tags?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  score?: Maybe<GqlFloatNullableWithAggregatesFilter>;
  content_text?: Maybe<GqlStringWithAggregatesFilter>;
  content_html?: Maybe<GqlStringNullableWithAggregatesFilter>;
  enclosure?: Maybe<GqlJsonNullableWithAggregatesFilter>;
};

export type GqlArticleSumAggregate = {
  __typename?: 'ArticleSumAggregate';
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
};

export type GqlArticleUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_published?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_modified?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  comment_feed_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  score?: Maybe<GqlNullableFloatFieldUpdateOperationsInput>;
  content_text?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content_html?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  enclosure?: Maybe<Scalars['JSON']>;
  articleRef?: Maybe<GqlArticleRefUpdateManyWithoutArticleInput>;
};

export type GqlArticleUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_published?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_modified?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  comment_feed_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  score?: Maybe<GqlNullableFloatFieldUpdateOperationsInput>;
  content_text?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content_html?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  enclosure?: Maybe<Scalars['JSON']>;
};

export type GqlArticleUpdateOneRequiredWithoutArticleRefInput = {
  create?: Maybe<GqlArticleCreateWithoutArticleRefInput>;
  connectOrCreate?: Maybe<GqlArticleCreateOrConnectWithoutArticleRefInput>;
  upsert?: Maybe<GqlArticleUpsertWithoutArticleRefInput>;
  connect?: Maybe<GqlArticleWhereUniqueInput>;
  update?: Maybe<GqlArticleUpdateWithoutArticleRefInput>;
};

export type GqlArticleUpdateWithoutArticleRefInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_published?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_modified?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  comment_feed_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  score?: Maybe<GqlNullableFloatFieldUpdateOperationsInput>;
  content_text?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content_html?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  enclosure?: Maybe<Scalars['JSON']>;
};

export type GqlArticleUpsertWithoutArticleRefInput = {
  update: GqlArticleUpdateWithoutArticleRefInput;
  create: GqlArticleCreateWithoutArticleRefInput;
};

export type GqlArticleWhereInput = {
  AND?: Maybe<Array<GqlArticleWhereInput>>;
  OR?: Maybe<Array<GqlArticleWhereInput>>;
  NOT?: Maybe<Array<GqlArticleWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  date_published?: Maybe<GqlDateTimeFilter>;
  date_modified?: Maybe<GqlDateTimeNullableFilter>;
  comment_feed_url?: Maybe<GqlStringNullableFilter>;
  url?: Maybe<GqlStringFilter>;
  author?: Maybe<GqlStringNullableFilter>;
  title?: Maybe<GqlStringFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  score?: Maybe<GqlFloatNullableFilter>;
  content_text?: Maybe<GqlStringFilter>;
  content_html?: Maybe<GqlStringNullableFilter>;
  enclosure?: Maybe<GqlJsonNullableFilter>;
  articleRef?: Maybe<GqlArticleRefListRelationFilter>;
};

export type GqlArticleWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlBoolFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['Boolean']>;
};

export type GqlBoolFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<GqlNestedBoolFilter>;
};

export type GqlBoolWithAggregatesFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<GqlNestedBoolWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntFilter>;
  _min?: Maybe<GqlNestedBoolFilter>;
  _max?: Maybe<GqlNestedBoolFilter>;
};

export type GqlBucket = {
  __typename?: 'Bucket';
  id: FieldWrapper<Scalars['String']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed: FieldWrapper<Scalars['Boolean']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  title: FieldWrapper<Scalars['String']>;
  ownerId: FieldWrapper<Scalars['String']>;
  filter_expr?: Maybe<FieldWrapper<Scalars['JSON']>>;
  retention_policy?: Maybe<FieldWrapper<Scalars['JSON']>>;
  replay_policy?: Maybe<FieldWrapper<Scalars['JSON']>>;
  content_resolution?: Maybe<FieldWrapper<Scalars['JSON']>>;
  streamId: FieldWrapper<Scalars['String']>;
  owner: FieldWrapper<GqlUser>;
  subscriptions: Array<FieldWrapper<GqlSubscription>>;
  entryPostProcessors: Array<FieldWrapper<GqlEntryPostProcessor>>;
  stream: FieldWrapper<GqlStream>;
};


export type GqlBucketSubscriptionsArgs = {
  where?: Maybe<GqlSubscriptionWhereInput>;
  orderBy?: Maybe<Array<GqlSubscriptionOrderByInput>>;
  cursor?: Maybe<GqlSubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlSubscriptionScalarFieldEnum>>;
};


export type GqlBucketEntryPostProcessorsArgs = {
  where?: Maybe<GqlEntryPostProcessorWhereInput>;
  orderBy?: Maybe<Array<GqlEntryPostProcessorOrderByInput>>;
  cursor?: Maybe<GqlEntryPostProcessorWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlEntryPostProcessorScalarFieldEnum>>;
};

export type GqlBucketCountAggregate = {
  __typename?: 'BucketCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  description: FieldWrapper<Scalars['Int']>;
  listed: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  filter_expr: FieldWrapper<Scalars['Int']>;
  retention_policy: FieldWrapper<Scalars['Int']>;
  replay_policy: FieldWrapper<Scalars['Int']>;
  content_resolution: FieldWrapper<Scalars['Int']>;
  streamId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlBucketCreateInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutBucketInput>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorCreateNestedManyWithoutBucketInput>;
  stream: GqlStreamCreateNestedOneWithoutBucketInput;
};

export type GqlBucketCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  ownerId: Scalars['String'];
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  streamId: Scalars['String'];
};

export type GqlBucketCreateManyOwnerInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  streamId: Scalars['String'];
};

export type GqlBucketCreateManyOwnerInputEnvelope = {
  data: Array<GqlBucketCreateManyOwnerInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlBucketCreateManyStreamInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  ownerId: Scalars['String'];
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
};

export type GqlBucketCreateManyStreamInputEnvelope = {
  data: Array<GqlBucketCreateManyStreamInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlBucketCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlBucketCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlBucketCreateOrConnectWithoutOwnerInput>>;
  createMany?: Maybe<GqlBucketCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
};

export type GqlBucketCreateNestedManyWithoutStreamInput = {
  create?: Maybe<Array<GqlBucketCreateWithoutStreamInput>>;
  connectOrCreate?: Maybe<Array<GqlBucketCreateOrConnectWithoutStreamInput>>;
  createMany?: Maybe<GqlBucketCreateManyStreamInputEnvelope>;
  connect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
};

export type GqlBucketCreateNestedOneWithoutEntryPostProcessorsInput = {
  create?: Maybe<GqlBucketCreateWithoutEntryPostProcessorsInput>;
  connectOrCreate?: Maybe<GqlBucketCreateOrConnectWithoutEntryPostProcessorsInput>;
  connect?: Maybe<GqlBucketWhereUniqueInput>;
};

export type GqlBucketCreateNestedOneWithoutSubscriptionsInput = {
  create?: Maybe<GqlBucketCreateWithoutSubscriptionsInput>;
  connectOrCreate?: Maybe<GqlBucketCreateOrConnectWithoutSubscriptionsInput>;
  connect?: Maybe<GqlBucketWhereUniqueInput>;
};

export type GqlBucketCreateOrConnectWithoutEntryPostProcessorsInput = {
  where: GqlBucketWhereUniqueInput;
  create: GqlBucketCreateWithoutEntryPostProcessorsInput;
};

export type GqlBucketCreateOrConnectWithoutOwnerInput = {
  where: GqlBucketWhereUniqueInput;
  create: GqlBucketCreateWithoutOwnerInput;
};

export type GqlBucketCreateOrConnectWithoutStreamInput = {
  where: GqlBucketWhereUniqueInput;
  create: GqlBucketCreateWithoutStreamInput;
};

export type GqlBucketCreateOrConnectWithoutSubscriptionsInput = {
  where: GqlBucketWhereUniqueInput;
  create: GqlBucketCreateWithoutSubscriptionsInput;
};

export type GqlBucketCreateWithoutEntryPostProcessorsInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutBucketInput>;
  stream: GqlStreamCreateNestedOneWithoutBucketInput;
};

export type GqlBucketCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutBucketInput>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorCreateNestedManyWithoutBucketInput>;
  stream: GqlStreamCreateNestedOneWithoutBucketInput;
};

export type GqlBucketCreateWithoutStreamInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutBucketInput>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorCreateNestedManyWithoutBucketInput>;
};

export type GqlBucketCreateWithoutSubscriptionsInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorCreateNestedManyWithoutBucketInput>;
  stream: GqlStreamCreateNestedOneWithoutBucketInput;
};

export type GqlBucketGroupBy = {
  __typename?: 'BucketGroupBy';
  id: FieldWrapper<Scalars['String']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed: FieldWrapper<Scalars['Boolean']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  title: FieldWrapper<Scalars['String']>;
  ownerId: FieldWrapper<Scalars['String']>;
  filter_expr?: Maybe<FieldWrapper<Scalars['JSON']>>;
  retention_policy?: Maybe<FieldWrapper<Scalars['JSON']>>;
  replay_policy?: Maybe<FieldWrapper<Scalars['JSON']>>;
  content_resolution?: Maybe<FieldWrapper<Scalars['JSON']>>;
  streamId: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlBucketCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlBucketMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlBucketMaxAggregate>>;
};

export type GqlBucketListRelationFilter = {
  every?: Maybe<GqlBucketWhereInput>;
  some?: Maybe<GqlBucketWhereInput>;
  none?: Maybe<GqlBucketWhereInput>;
};

export type GqlBucketMaxAggregate = {
  __typename?: 'BucketMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  streamId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlBucketMinAggregate = {
  __typename?: 'BucketMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  streamId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlBucketOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  description?: Maybe<GqlSortOrder>;
  listed?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  filter_expr?: Maybe<GqlSortOrder>;
  retention_policy?: Maybe<GqlSortOrder>;
  replay_policy?: Maybe<GqlSortOrder>;
  content_resolution?: Maybe<GqlSortOrder>;
  streamId?: Maybe<GqlSortOrder>;
};

export type GqlBucketRelationFilter = {
  is?: Maybe<GqlBucketWhereInput>;
  isNot?: Maybe<GqlBucketWhereInput>;
};

export enum GqlBucketScalarFieldEnum {
  Id = 'id',
  Description = 'description',
  Listed = 'listed',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Title = 'title',
  OwnerId = 'ownerId',
  FilterExpr = 'filter_expr',
  RetentionPolicy = 'retention_policy',
  ReplayPolicy = 'replay_policy',
  ContentResolution = 'content_resolution',
  StreamId = 'streamId'
}

export type GqlBucketScalarWhereInput = {
  AND?: Maybe<Array<GqlBucketScalarWhereInput>>;
  OR?: Maybe<Array<GqlBucketScalarWhereInput>>;
  NOT?: Maybe<Array<GqlBucketScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  description?: Maybe<GqlStringNullableFilter>;
  listed?: Maybe<GqlBoolFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  title?: Maybe<GqlStringFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  filter_expr?: Maybe<GqlJsonNullableFilter>;
  retention_policy?: Maybe<GqlJsonNullableFilter>;
  replay_policy?: Maybe<GqlJsonNullableFilter>;
  content_resolution?: Maybe<GqlJsonNullableFilter>;
  streamId?: Maybe<GqlStringFilter>;
};

export type GqlBucketScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlBucketScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlBucketScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlBucketScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  description?: Maybe<GqlStringNullableWithAggregatesFilter>;
  listed?: Maybe<GqlBoolWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  title?: Maybe<GqlStringWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
  filter_expr?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  retention_policy?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  replay_policy?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  content_resolution?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  streamId?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlBucketUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutBucketInput>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorUpdateManyWithoutBucketInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutBucketInput>;
};

export type GqlBucketUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
};

export type GqlBucketUpdateManyWithWhereWithoutOwnerInput = {
  where: GqlBucketScalarWhereInput;
  data: GqlBucketUpdateManyMutationInput;
};

export type GqlBucketUpdateManyWithWhereWithoutStreamInput = {
  where: GqlBucketScalarWhereInput;
  data: GqlBucketUpdateManyMutationInput;
};

export type GqlBucketUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlBucketCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlBucketCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<GqlBucketUpsertWithWhereUniqueWithoutOwnerInput>>;
  createMany?: Maybe<GqlBucketCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  set?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  delete?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  update?: Maybe<Array<GqlBucketUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<GqlBucketUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<GqlBucketScalarWhereInput>>;
};

export type GqlBucketUpdateManyWithoutStreamInput = {
  create?: Maybe<Array<GqlBucketCreateWithoutStreamInput>>;
  connectOrCreate?: Maybe<Array<GqlBucketCreateOrConnectWithoutStreamInput>>;
  upsert?: Maybe<Array<GqlBucketUpsertWithWhereUniqueWithoutStreamInput>>;
  createMany?: Maybe<GqlBucketCreateManyStreamInputEnvelope>;
  connect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  set?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  delete?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  update?: Maybe<Array<GqlBucketUpdateWithWhereUniqueWithoutStreamInput>>;
  updateMany?: Maybe<Array<GqlBucketUpdateManyWithWhereWithoutStreamInput>>;
  deleteMany?: Maybe<Array<GqlBucketScalarWhereInput>>;
};

export type GqlBucketUpdateOneRequiredWithoutEntryPostProcessorsInput = {
  create?: Maybe<GqlBucketCreateWithoutEntryPostProcessorsInput>;
  connectOrCreate?: Maybe<GqlBucketCreateOrConnectWithoutEntryPostProcessorsInput>;
  upsert?: Maybe<GqlBucketUpsertWithoutEntryPostProcessorsInput>;
  connect?: Maybe<GqlBucketWhereUniqueInput>;
  update?: Maybe<GqlBucketUpdateWithoutEntryPostProcessorsInput>;
};

export type GqlBucketUpdateOneRequiredWithoutSubscriptionsInput = {
  create?: Maybe<GqlBucketCreateWithoutSubscriptionsInput>;
  connectOrCreate?: Maybe<GqlBucketCreateOrConnectWithoutSubscriptionsInput>;
  upsert?: Maybe<GqlBucketUpsertWithoutSubscriptionsInput>;
  connect?: Maybe<GqlBucketWhereUniqueInput>;
  update?: Maybe<GqlBucketUpdateWithoutSubscriptionsInput>;
};

export type GqlBucketUpdateWithWhereUniqueWithoutOwnerInput = {
  where: GqlBucketWhereUniqueInput;
  data: GqlBucketUpdateWithoutOwnerInput;
};

export type GqlBucketUpdateWithWhereUniqueWithoutStreamInput = {
  where: GqlBucketWhereUniqueInput;
  data: GqlBucketUpdateWithoutStreamInput;
};

export type GqlBucketUpdateWithoutEntryPostProcessorsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutBucketInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutBucketInput>;
};

export type GqlBucketUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutBucketInput>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorUpdateManyWithoutBucketInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutBucketInput>;
};

export type GqlBucketUpdateWithoutStreamInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutBucketInput>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorUpdateManyWithoutBucketInput>;
};

export type GqlBucketUpdateWithoutSubscriptionsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  filter_expr?: Maybe<Scalars['JSON']>;
  retention_policy?: Maybe<Scalars['JSON']>;
  replay_policy?: Maybe<Scalars['JSON']>;
  content_resolution?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorUpdateManyWithoutBucketInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutBucketInput>;
};

export type GqlBucketUpsertWithWhereUniqueWithoutOwnerInput = {
  where: GqlBucketWhereUniqueInput;
  update: GqlBucketUpdateWithoutOwnerInput;
  create: GqlBucketCreateWithoutOwnerInput;
};

export type GqlBucketUpsertWithWhereUniqueWithoutStreamInput = {
  where: GqlBucketWhereUniqueInput;
  update: GqlBucketUpdateWithoutStreamInput;
  create: GqlBucketCreateWithoutStreamInput;
};

export type GqlBucketUpsertWithoutEntryPostProcessorsInput = {
  update: GqlBucketUpdateWithoutEntryPostProcessorsInput;
  create: GqlBucketCreateWithoutEntryPostProcessorsInput;
};

export type GqlBucketUpsertWithoutSubscriptionsInput = {
  update: GqlBucketUpdateWithoutSubscriptionsInput;
  create: GqlBucketCreateWithoutSubscriptionsInput;
};

export type GqlBucketWhereInput = {
  AND?: Maybe<Array<GqlBucketWhereInput>>;
  OR?: Maybe<Array<GqlBucketWhereInput>>;
  NOT?: Maybe<Array<GqlBucketWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  description?: Maybe<GqlStringNullableFilter>;
  listed?: Maybe<GqlBoolFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  title?: Maybe<GqlStringFilter>;
  owner?: Maybe<GqlUserRelationFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  subscriptions?: Maybe<GqlSubscriptionListRelationFilter>;
  filter_expr?: Maybe<GqlJsonNullableFilter>;
  retention_policy?: Maybe<GqlJsonNullableFilter>;
  replay_policy?: Maybe<GqlJsonNullableFilter>;
  content_resolution?: Maybe<GqlJsonNullableFilter>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorListRelationFilter>;
  stream?: Maybe<GqlStreamRelationFilter>;
  streamId?: Maybe<GqlStringFilter>;
};

export type GqlBucketWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};


export type GqlDateTimeFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['DateTime']>;
};

export type GqlDateTimeFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<GqlNestedDateTimeFilter>;
};

export type GqlDateTimeNullableFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<GqlNestedDateTimeNullableFilter>;
};

export type GqlDateTimeNullableWithAggregatesFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<GqlNestedDateTimeNullableWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntNullableFilter>;
  _min?: Maybe<GqlNestedDateTimeNullableFilter>;
  _max?: Maybe<GqlNestedDateTimeNullableFilter>;
};

export type GqlDateTimeWithAggregatesFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<GqlNestedDateTimeWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntFilter>;
  _min?: Maybe<GqlNestedDateTimeFilter>;
  _max?: Maybe<GqlNestedDateTimeFilter>;
};

export type GqlDiscoveredFeed = {
  __typename?: 'DiscoveredFeed';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  url: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  type: FieldWrapper<Scalars['String']>;
};

export type GqlEntryPostProcessor = {
  __typename?: 'EntryPostProcessor';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  type: FieldWrapper<Scalars['String']>;
  bucketId: FieldWrapper<Scalars['String']>;
  bucket: FieldWrapper<GqlBucket>;
};

export type GqlEntryPostProcessorCountAggregate = {
  __typename?: 'EntryPostProcessorCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  type: FieldWrapper<Scalars['Int']>;
  bucketId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlEntryPostProcessorCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  type: Scalars['String'];
  bucket: GqlBucketCreateNestedOneWithoutEntryPostProcessorsInput;
};

export type GqlEntryPostProcessorCreateManyBucketInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  type: Scalars['String'];
};

export type GqlEntryPostProcessorCreateManyBucketInputEnvelope = {
  data: Array<GqlEntryPostProcessorCreateManyBucketInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlEntryPostProcessorCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  type: Scalars['String'];
  bucketId: Scalars['String'];
};

export type GqlEntryPostProcessorCreateNestedManyWithoutBucketInput = {
  create?: Maybe<Array<GqlEntryPostProcessorCreateWithoutBucketInput>>;
  connectOrCreate?: Maybe<Array<GqlEntryPostProcessorCreateOrConnectWithoutBucketInput>>;
  createMany?: Maybe<GqlEntryPostProcessorCreateManyBucketInputEnvelope>;
  connect?: Maybe<Array<GqlEntryPostProcessorWhereUniqueInput>>;
};

export type GqlEntryPostProcessorCreateOrConnectWithoutBucketInput = {
  where: GqlEntryPostProcessorWhereUniqueInput;
  create: GqlEntryPostProcessorCreateWithoutBucketInput;
};

export type GqlEntryPostProcessorCreateWithoutBucketInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  type: Scalars['String'];
};

export type GqlEntryPostProcessorGroupBy = {
  __typename?: 'EntryPostProcessorGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  type: FieldWrapper<Scalars['String']>;
  bucketId: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlEntryPostProcessorCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlEntryPostProcessorMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlEntryPostProcessorMaxAggregate>>;
};

export type GqlEntryPostProcessorListRelationFilter = {
  every?: Maybe<GqlEntryPostProcessorWhereInput>;
  some?: Maybe<GqlEntryPostProcessorWhereInput>;
  none?: Maybe<GqlEntryPostProcessorWhereInput>;
};

export type GqlEntryPostProcessorMaxAggregate = {
  __typename?: 'EntryPostProcessorMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlEntryPostProcessorMinAggregate = {
  __typename?: 'EntryPostProcessorMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlEntryPostProcessorOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  type?: Maybe<GqlSortOrder>;
  bucketId?: Maybe<GqlSortOrder>;
};

export enum GqlEntryPostProcessorScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Type = 'type',
  BucketId = 'bucketId'
}

export type GqlEntryPostProcessorScalarWhereInput = {
  AND?: Maybe<Array<GqlEntryPostProcessorScalarWhereInput>>;
  OR?: Maybe<Array<GqlEntryPostProcessorScalarWhereInput>>;
  NOT?: Maybe<Array<GqlEntryPostProcessorScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  type?: Maybe<GqlStringFilter>;
  bucketId?: Maybe<GqlStringFilter>;
};

export type GqlEntryPostProcessorScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlEntryPostProcessorScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlEntryPostProcessorScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlEntryPostProcessorScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  type?: Maybe<GqlStringWithAggregatesFilter>;
  bucketId?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlEntryPostProcessorUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  bucket?: Maybe<GqlBucketUpdateOneRequiredWithoutEntryPostProcessorsInput>;
};

export type GqlEntryPostProcessorUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
};

export type GqlEntryPostProcessorUpdateManyWithWhereWithoutBucketInput = {
  where: GqlEntryPostProcessorScalarWhereInput;
  data: GqlEntryPostProcessorUpdateManyMutationInput;
};

export type GqlEntryPostProcessorUpdateManyWithoutBucketInput = {
  create?: Maybe<Array<GqlEntryPostProcessorCreateWithoutBucketInput>>;
  connectOrCreate?: Maybe<Array<GqlEntryPostProcessorCreateOrConnectWithoutBucketInput>>;
  upsert?: Maybe<Array<GqlEntryPostProcessorUpsertWithWhereUniqueWithoutBucketInput>>;
  createMany?: Maybe<GqlEntryPostProcessorCreateManyBucketInputEnvelope>;
  connect?: Maybe<Array<GqlEntryPostProcessorWhereUniqueInput>>;
  set?: Maybe<Array<GqlEntryPostProcessorWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlEntryPostProcessorWhereUniqueInput>>;
  delete?: Maybe<Array<GqlEntryPostProcessorWhereUniqueInput>>;
  update?: Maybe<Array<GqlEntryPostProcessorUpdateWithWhereUniqueWithoutBucketInput>>;
  updateMany?: Maybe<Array<GqlEntryPostProcessorUpdateManyWithWhereWithoutBucketInput>>;
  deleteMany?: Maybe<Array<GqlEntryPostProcessorScalarWhereInput>>;
};

export type GqlEntryPostProcessorUpdateWithWhereUniqueWithoutBucketInput = {
  where: GqlEntryPostProcessorWhereUniqueInput;
  data: GqlEntryPostProcessorUpdateWithoutBucketInput;
};

export type GqlEntryPostProcessorUpdateWithoutBucketInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
};

export type GqlEntryPostProcessorUpsertWithWhereUniqueWithoutBucketInput = {
  where: GqlEntryPostProcessorWhereUniqueInput;
  update: GqlEntryPostProcessorUpdateWithoutBucketInput;
  create: GqlEntryPostProcessorCreateWithoutBucketInput;
};

export type GqlEntryPostProcessorWhereInput = {
  AND?: Maybe<Array<GqlEntryPostProcessorWhereInput>>;
  OR?: Maybe<Array<GqlEntryPostProcessorWhereInput>>;
  NOT?: Maybe<Array<GqlEntryPostProcessorWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  type?: Maybe<GqlStringFilter>;
  bucket?: Maybe<GqlBucketRelationFilter>;
  bucketId?: Maybe<GqlStringFilter>;
};

export type GqlEntryPostProcessorWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlFeed = {
  __typename?: 'Feed';
  id: FieldWrapper<Scalars['String']>;
  feed_url: FieldWrapper<Scalars['String']>;
  home_page_url?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  lang?: Maybe<FieldWrapper<Scalars['String']>>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  enclosures?: Maybe<FieldWrapper<Scalars['JSON']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  expired: FieldWrapper<Scalars['Boolean']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  status: FieldWrapper<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  nextHarvestAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  streamId: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  stream: FieldWrapper<GqlStream>;
  subscriptions: Array<FieldWrapper<GqlSubscription>>;
  events: Array<FieldWrapper<GqlFeedEvent>>;
};


export type GqlFeedSubscriptionsArgs = {
  where?: Maybe<GqlSubscriptionWhereInput>;
  orderBy?: Maybe<Array<GqlSubscriptionOrderByInput>>;
  cursor?: Maybe<GqlSubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlSubscriptionScalarFieldEnum>>;
};


export type GqlFeedEventsArgs = {
  where?: Maybe<GqlFeedEventWhereInput>;
  orderBy?: Maybe<Array<GqlFeedEventOrderByInput>>;
  cursor?: Maybe<GqlFeedEventWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlFeedEventScalarFieldEnum>>;
};

export type GqlFeedAvgAggregate = {
  __typename?: 'FeedAvgAggregate';
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Float']>>;
};

export type GqlFeedCountAggregate = {
  __typename?: 'FeedCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  feed_url: FieldWrapper<Scalars['Int']>;
  home_page_url: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  lang: FieldWrapper<Scalars['Int']>;
  tags: FieldWrapper<Scalars['Int']>;
  enclosures: FieldWrapper<Scalars['Int']>;
  author: FieldWrapper<Scalars['Int']>;
  expired: FieldWrapper<Scalars['Int']>;
  description: FieldWrapper<Scalars['Int']>;
  status: FieldWrapper<Scalars['Int']>;
  harvestIntervalMinutes: FieldWrapper<Scalars['Int']>;
  nextHarvestAt: FieldWrapper<Scalars['Int']>;
  streamId: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlFeedCreateInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  enclosures?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  stream: GqlStreamCreateNestedOneWithoutFeedInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  enclosures?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  streamId: Scalars['String'];
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
};

export type GqlFeedCreateManyStreamInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  enclosures?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
};

export type GqlFeedCreateManyStreamInputEnvelope = {
  data: Array<GqlFeedCreateManyStreamInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlFeedCreateNestedManyWithoutStreamInput = {
  create?: Maybe<Array<GqlFeedCreateWithoutStreamInput>>;
  connectOrCreate?: Maybe<Array<GqlFeedCreateOrConnectWithoutStreamInput>>;
  createMany?: Maybe<GqlFeedCreateManyStreamInputEnvelope>;
  connect?: Maybe<Array<GqlFeedWhereUniqueInput>>;
};

export type GqlFeedCreateNestedOneWithoutEventsInput = {
  create?: Maybe<GqlFeedCreateWithoutEventsInput>;
  connectOrCreate?: Maybe<GqlFeedCreateOrConnectWithoutEventsInput>;
  connect?: Maybe<GqlFeedWhereUniqueInput>;
};

export type GqlFeedCreateNestedOneWithoutSubscriptionsInput = {
  create?: Maybe<GqlFeedCreateWithoutSubscriptionsInput>;
  connectOrCreate?: Maybe<GqlFeedCreateOrConnectWithoutSubscriptionsInput>;
  connect?: Maybe<GqlFeedWhereUniqueInput>;
};

export type GqlFeedCreateOrConnectWithoutEventsInput = {
  where: GqlFeedWhereUniqueInput;
  create: GqlFeedCreateWithoutEventsInput;
};

export type GqlFeedCreateOrConnectWithoutStreamInput = {
  where: GqlFeedWhereUniqueInput;
  create: GqlFeedCreateWithoutStreamInput;
};

export type GqlFeedCreateOrConnectWithoutSubscriptionsInput = {
  where: GqlFeedWhereUniqueInput;
  create: GqlFeedCreateWithoutSubscriptionsInput;
};

export type GqlFeedCreateWithoutEventsInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  enclosures?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  stream: GqlStreamCreateNestedOneWithoutFeedInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedCreateWithoutStreamInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  enclosures?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedCreateWithoutSubscriptionsInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  enclosures?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  stream: GqlStreamCreateNestedOneWithoutFeedInput;
  events?: Maybe<GqlFeedEventCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedEvent = {
  __typename?: 'FeedEvent';
  id: FieldWrapper<Scalars['String']>;
  message: FieldWrapper<Scalars['JSON']>;
  feedId: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  is_error: FieldWrapper<Scalars['Boolean']>;
  feed: FieldWrapper<GqlFeed>;
};

export type GqlFeedEventCountAggregate = {
  __typename?: 'FeedEventCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  message: FieldWrapper<Scalars['Int']>;
  feedId: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  is_error: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlFeedEventCreateInput = {
  id?: Maybe<Scalars['String']>;
  message: Scalars['JSON'];
  createdAt?: Maybe<Scalars['DateTime']>;
  is_error?: Maybe<Scalars['Boolean']>;
  feed: GqlFeedCreateNestedOneWithoutEventsInput;
};

export type GqlFeedEventCreateManyFeedInput = {
  id?: Maybe<Scalars['String']>;
  message: Scalars['JSON'];
  createdAt?: Maybe<Scalars['DateTime']>;
  is_error?: Maybe<Scalars['Boolean']>;
};

export type GqlFeedEventCreateManyFeedInputEnvelope = {
  data: Array<GqlFeedEventCreateManyFeedInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlFeedEventCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  message: Scalars['JSON'];
  feedId: Scalars['String'];
  createdAt?: Maybe<Scalars['DateTime']>;
  is_error?: Maybe<Scalars['Boolean']>;
};

export type GqlFeedEventCreateNestedManyWithoutFeedInput = {
  create?: Maybe<Array<GqlFeedEventCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<GqlFeedEventCreateOrConnectWithoutFeedInput>>;
  createMany?: Maybe<GqlFeedEventCreateManyFeedInputEnvelope>;
  connect?: Maybe<Array<GqlFeedEventWhereUniqueInput>>;
};

export type GqlFeedEventCreateOrConnectWithoutFeedInput = {
  where: GqlFeedEventWhereUniqueInput;
  create: GqlFeedEventCreateWithoutFeedInput;
};

export type GqlFeedEventCreateWithoutFeedInput = {
  id?: Maybe<Scalars['String']>;
  message: Scalars['JSON'];
  createdAt?: Maybe<Scalars['DateTime']>;
  is_error?: Maybe<Scalars['Boolean']>;
};

export type GqlFeedEventGroupBy = {
  __typename?: 'FeedEventGroupBy';
  id: FieldWrapper<Scalars['String']>;
  message: FieldWrapper<Scalars['JSON']>;
  feedId: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  is_error: FieldWrapper<Scalars['Boolean']>;
  _count?: Maybe<FieldWrapper<GqlFeedEventCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlFeedEventMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlFeedEventMaxAggregate>>;
};

export type GqlFeedEventListRelationFilter = {
  every?: Maybe<GqlFeedEventWhereInput>;
  some?: Maybe<GqlFeedEventWhereInput>;
  none?: Maybe<GqlFeedEventWhereInput>;
};

export type GqlFeedEventMaxAggregate = {
  __typename?: 'FeedEventMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  is_error?: Maybe<FieldWrapper<Scalars['Boolean']>>;
};

export type GqlFeedEventMinAggregate = {
  __typename?: 'FeedEventMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  is_error?: Maybe<FieldWrapper<Scalars['Boolean']>>;
};

export type GqlFeedEventOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  message?: Maybe<GqlSortOrder>;
  feedId?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  is_error?: Maybe<GqlSortOrder>;
};

export enum GqlFeedEventScalarFieldEnum {
  Id = 'id',
  Message = 'message',
  FeedId = 'feedId',
  CreatedAt = 'createdAt',
  IsError = 'is_error'
}

export type GqlFeedEventScalarWhereInput = {
  AND?: Maybe<Array<GqlFeedEventScalarWhereInput>>;
  OR?: Maybe<Array<GqlFeedEventScalarWhereInput>>;
  NOT?: Maybe<Array<GqlFeedEventScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  message?: Maybe<GqlJsonFilter>;
  feedId?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  is_error?: Maybe<GqlBoolFilter>;
};

export type GqlFeedEventScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlFeedEventScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlFeedEventScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlFeedEventScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  message?: Maybe<GqlJsonWithAggregatesFilter>;
  feedId?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  is_error?: Maybe<GqlBoolWithAggregatesFilter>;
};

export type GqlFeedEventUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  message?: Maybe<Scalars['JSON']>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  is_error?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  feed?: Maybe<GqlFeedUpdateOneRequiredWithoutEventsInput>;
};

export type GqlFeedEventUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  message?: Maybe<Scalars['JSON']>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  is_error?: Maybe<GqlBoolFieldUpdateOperationsInput>;
};

export type GqlFeedEventUpdateManyWithWhereWithoutFeedInput = {
  where: GqlFeedEventScalarWhereInput;
  data: GqlFeedEventUpdateManyMutationInput;
};

export type GqlFeedEventUpdateManyWithoutFeedInput = {
  create?: Maybe<Array<GqlFeedEventCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<GqlFeedEventCreateOrConnectWithoutFeedInput>>;
  upsert?: Maybe<Array<GqlFeedEventUpsertWithWhereUniqueWithoutFeedInput>>;
  createMany?: Maybe<GqlFeedEventCreateManyFeedInputEnvelope>;
  connect?: Maybe<Array<GqlFeedEventWhereUniqueInput>>;
  set?: Maybe<Array<GqlFeedEventWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlFeedEventWhereUniqueInput>>;
  delete?: Maybe<Array<GqlFeedEventWhereUniqueInput>>;
  update?: Maybe<Array<GqlFeedEventUpdateWithWhereUniqueWithoutFeedInput>>;
  updateMany?: Maybe<Array<GqlFeedEventUpdateManyWithWhereWithoutFeedInput>>;
  deleteMany?: Maybe<Array<GqlFeedEventScalarWhereInput>>;
};

export type GqlFeedEventUpdateWithWhereUniqueWithoutFeedInput = {
  where: GqlFeedEventWhereUniqueInput;
  data: GqlFeedEventUpdateWithoutFeedInput;
};

export type GqlFeedEventUpdateWithoutFeedInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  message?: Maybe<Scalars['JSON']>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  is_error?: Maybe<GqlBoolFieldUpdateOperationsInput>;
};

export type GqlFeedEventUpsertWithWhereUniqueWithoutFeedInput = {
  where: GqlFeedEventWhereUniqueInput;
  update: GqlFeedEventUpdateWithoutFeedInput;
  create: GqlFeedEventCreateWithoutFeedInput;
};

export type GqlFeedEventWhereInput = {
  AND?: Maybe<Array<GqlFeedEventWhereInput>>;
  OR?: Maybe<Array<GqlFeedEventWhereInput>>;
  NOT?: Maybe<Array<GqlFeedEventWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  message?: Maybe<GqlJsonFilter>;
  feed?: Maybe<GqlFeedRelationFilter>;
  feedId?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  is_error?: Maybe<GqlBoolFilter>;
};

export type GqlFeedEventWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlFeedGroupBy = {
  __typename?: 'FeedGroupBy';
  id: FieldWrapper<Scalars['String']>;
  feed_url: FieldWrapper<Scalars['String']>;
  home_page_url?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  lang?: Maybe<FieldWrapper<Scalars['String']>>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  enclosures?: Maybe<FieldWrapper<Scalars['JSON']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  expired: FieldWrapper<Scalars['Boolean']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  status: FieldWrapper<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  nextHarvestAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  streamId: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  _count?: Maybe<FieldWrapper<GqlFeedCountAggregate>>;
  _avg?: Maybe<FieldWrapper<GqlFeedAvgAggregate>>;
  _sum?: Maybe<FieldWrapper<GqlFeedSumAggregate>>;
  _min?: Maybe<FieldWrapper<GqlFeedMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlFeedMaxAggregate>>;
};

export type GqlFeedListRelationFilter = {
  every?: Maybe<GqlFeedWhereInput>;
  some?: Maybe<GqlFeedWhereInput>;
  none?: Maybe<GqlFeedWhereInput>;
};

export type GqlFeedMaxAggregate = {
  __typename?: 'FeedMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  home_page_url?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  lang?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  expired?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  status?: Maybe<FieldWrapper<Scalars['String']>>;
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  nextHarvestAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  streamId?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlFeedMinAggregate = {
  __typename?: 'FeedMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  home_page_url?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  lang?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  expired?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  status?: Maybe<FieldWrapper<Scalars['String']>>;
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  nextHarvestAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  streamId?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlFeedOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  feed_url?: Maybe<GqlSortOrder>;
  home_page_url?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  lang?: Maybe<GqlSortOrder>;
  tags?: Maybe<GqlSortOrder>;
  enclosures?: Maybe<GqlSortOrder>;
  author?: Maybe<GqlSortOrder>;
  expired?: Maybe<GqlSortOrder>;
  description?: Maybe<GqlSortOrder>;
  status?: Maybe<GqlSortOrder>;
  harvestIntervalMinutes?: Maybe<GqlSortOrder>;
  nextHarvestAt?: Maybe<GqlSortOrder>;
  streamId?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
};

export type GqlFeedRelationFilter = {
  is?: Maybe<GqlFeedWhereInput>;
  isNot?: Maybe<GqlFeedWhereInput>;
};

export enum GqlFeedScalarFieldEnum {
  Id = 'id',
  FeedUrl = 'feed_url',
  HomePageUrl = 'home_page_url',
  Title = 'title',
  Lang = 'lang',
  Tags = 'tags',
  Enclosures = 'enclosures',
  Author = 'author',
  Expired = 'expired',
  Description = 'description',
  Status = 'status',
  HarvestIntervalMinutes = 'harvestIntervalMinutes',
  NextHarvestAt = 'nextHarvestAt',
  StreamId = 'streamId',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt'
}

export type GqlFeedScalarWhereInput = {
  AND?: Maybe<Array<GqlFeedScalarWhereInput>>;
  OR?: Maybe<Array<GqlFeedScalarWhereInput>>;
  NOT?: Maybe<Array<GqlFeedScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  feed_url?: Maybe<GqlStringFilter>;
  home_page_url?: Maybe<GqlStringNullableFilter>;
  title?: Maybe<GqlStringNullableFilter>;
  lang?: Maybe<GqlStringNullableFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  enclosures?: Maybe<GqlJsonNullableFilter>;
  author?: Maybe<GqlStringNullableFilter>;
  expired?: Maybe<GqlBoolFilter>;
  description?: Maybe<GqlStringNullableFilter>;
  status?: Maybe<GqlStringFilter>;
  harvestIntervalMinutes?: Maybe<GqlIntNullableFilter>;
  nextHarvestAt?: Maybe<GqlDateTimeNullableFilter>;
  streamId?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
};

export type GqlFeedScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlFeedScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlFeedScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlFeedScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  feed_url?: Maybe<GqlStringWithAggregatesFilter>;
  home_page_url?: Maybe<GqlStringNullableWithAggregatesFilter>;
  title?: Maybe<GqlStringNullableWithAggregatesFilter>;
  lang?: Maybe<GqlStringNullableWithAggregatesFilter>;
  tags?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  enclosures?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  author?: Maybe<GqlStringNullableWithAggregatesFilter>;
  expired?: Maybe<GqlBoolWithAggregatesFilter>;
  description?: Maybe<GqlStringNullableWithAggregatesFilter>;
  status?: Maybe<GqlStringWithAggregatesFilter>;
  harvestIntervalMinutes?: Maybe<GqlIntNullableWithAggregatesFilter>;
  nextHarvestAt?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
  streamId?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
};

export type GqlFeedSumAggregate = {
  __typename?: 'FeedSumAggregate';
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
};

export type GqlFeedUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  enclosures?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutFeedInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  enclosures?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
};

export type GqlFeedUpdateManyWithWhereWithoutStreamInput = {
  where: GqlFeedScalarWhereInput;
  data: GqlFeedUpdateManyMutationInput;
};

export type GqlFeedUpdateManyWithoutStreamInput = {
  create?: Maybe<Array<GqlFeedCreateWithoutStreamInput>>;
  connectOrCreate?: Maybe<Array<GqlFeedCreateOrConnectWithoutStreamInput>>;
  upsert?: Maybe<Array<GqlFeedUpsertWithWhereUniqueWithoutStreamInput>>;
  createMany?: Maybe<GqlFeedCreateManyStreamInputEnvelope>;
  connect?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  set?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  delete?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  update?: Maybe<Array<GqlFeedUpdateWithWhereUniqueWithoutStreamInput>>;
  updateMany?: Maybe<Array<GqlFeedUpdateManyWithWhereWithoutStreamInput>>;
  deleteMany?: Maybe<Array<GqlFeedScalarWhereInput>>;
};

export type GqlFeedUpdateOneRequiredWithoutEventsInput = {
  create?: Maybe<GqlFeedCreateWithoutEventsInput>;
  connectOrCreate?: Maybe<GqlFeedCreateOrConnectWithoutEventsInput>;
  upsert?: Maybe<GqlFeedUpsertWithoutEventsInput>;
  connect?: Maybe<GqlFeedWhereUniqueInput>;
  update?: Maybe<GqlFeedUpdateWithoutEventsInput>;
};

export type GqlFeedUpdateOneRequiredWithoutSubscriptionsInput = {
  create?: Maybe<GqlFeedCreateWithoutSubscriptionsInput>;
  connectOrCreate?: Maybe<GqlFeedCreateOrConnectWithoutSubscriptionsInput>;
  upsert?: Maybe<GqlFeedUpsertWithoutSubscriptionsInput>;
  connect?: Maybe<GqlFeedWhereUniqueInput>;
  update?: Maybe<GqlFeedUpdateWithoutSubscriptionsInput>;
};

export type GqlFeedUpdateWithWhereUniqueWithoutStreamInput = {
  where: GqlFeedWhereUniqueInput;
  data: GqlFeedUpdateWithoutStreamInput;
};

export type GqlFeedUpdateWithoutEventsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  enclosures?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutFeedInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpdateWithoutStreamInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  enclosures?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpdateWithoutSubscriptionsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  enclosures?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutFeedInput>;
  events?: Maybe<GqlFeedEventUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpsertWithWhereUniqueWithoutStreamInput = {
  where: GqlFeedWhereUniqueInput;
  update: GqlFeedUpdateWithoutStreamInput;
  create: GqlFeedCreateWithoutStreamInput;
};

export type GqlFeedUpsertWithoutEventsInput = {
  update: GqlFeedUpdateWithoutEventsInput;
  create: GqlFeedCreateWithoutEventsInput;
};

export type GqlFeedUpsertWithoutSubscriptionsInput = {
  update: GqlFeedUpdateWithoutSubscriptionsInput;
  create: GqlFeedCreateWithoutSubscriptionsInput;
};

export type GqlFeedWhereInput = {
  AND?: Maybe<Array<GqlFeedWhereInput>>;
  OR?: Maybe<Array<GqlFeedWhereInput>>;
  NOT?: Maybe<Array<GqlFeedWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  feed_url?: Maybe<GqlStringFilter>;
  home_page_url?: Maybe<GqlStringNullableFilter>;
  title?: Maybe<GqlStringNullableFilter>;
  lang?: Maybe<GqlStringNullableFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  enclosures?: Maybe<GqlJsonNullableFilter>;
  author?: Maybe<GqlStringNullableFilter>;
  expired?: Maybe<GqlBoolFilter>;
  description?: Maybe<GqlStringNullableFilter>;
  status?: Maybe<GqlStringFilter>;
  harvestIntervalMinutes?: Maybe<GqlIntNullableFilter>;
  nextHarvestAt?: Maybe<GqlDateTimeNullableFilter>;
  stream?: Maybe<GqlStreamRelationFilter>;
  streamId?: Maybe<GqlStringFilter>;
  subscriptions?: Maybe<GqlSubscriptionListRelationFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  events?: Maybe<GqlFeedEventListRelationFilter>;
};

export type GqlFeedWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
  feed_url?: Maybe<Scalars['String']>;
};

export type GqlFloatNullableFilter = {
  equals?: Maybe<Scalars['Float']>;
  in?: Maybe<Array<Scalars['Float']>>;
  notIn?: Maybe<Array<Scalars['Float']>>;
  lt?: Maybe<Scalars['Float']>;
  lte?: Maybe<Scalars['Float']>;
  gt?: Maybe<Scalars['Float']>;
  gte?: Maybe<Scalars['Float']>;
  not?: Maybe<GqlNestedFloatNullableFilter>;
};

export type GqlFloatNullableWithAggregatesFilter = {
  equals?: Maybe<Scalars['Float']>;
  in?: Maybe<Array<Scalars['Float']>>;
  notIn?: Maybe<Array<Scalars['Float']>>;
  lt?: Maybe<Scalars['Float']>;
  lte?: Maybe<Scalars['Float']>;
  gt?: Maybe<Scalars['Float']>;
  gte?: Maybe<Scalars['Float']>;
  not?: Maybe<GqlNestedFloatNullableWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntNullableFilter>;
  _avg?: Maybe<GqlNestedFloatNullableFilter>;
  _sum?: Maybe<GqlNestedFloatNullableFilter>;
  _min?: Maybe<GqlNestedFloatNullableFilter>;
  _max?: Maybe<GqlNestedFloatNullableFilter>;
};

export type GqlIntFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['Int']>;
  increment?: Maybe<Scalars['Int']>;
  decrement?: Maybe<Scalars['Int']>;
  multiply?: Maybe<Scalars['Int']>;
  divide?: Maybe<Scalars['Int']>;
};

export type GqlIntFilter = {
  equals?: Maybe<Scalars['Int']>;
  in?: Maybe<Array<Scalars['Int']>>;
  notIn?: Maybe<Array<Scalars['Int']>>;
  lt?: Maybe<Scalars['Int']>;
  lte?: Maybe<Scalars['Int']>;
  gt?: Maybe<Scalars['Int']>;
  gte?: Maybe<Scalars['Int']>;
  not?: Maybe<GqlNestedIntFilter>;
};

export type GqlIntNullableFilter = {
  equals?: Maybe<Scalars['Int']>;
  in?: Maybe<Array<Scalars['Int']>>;
  notIn?: Maybe<Array<Scalars['Int']>>;
  lt?: Maybe<Scalars['Int']>;
  lte?: Maybe<Scalars['Int']>;
  gt?: Maybe<Scalars['Int']>;
  gte?: Maybe<Scalars['Int']>;
  not?: Maybe<GqlNestedIntNullableFilter>;
};

export type GqlIntNullableWithAggregatesFilter = {
  equals?: Maybe<Scalars['Int']>;
  in?: Maybe<Array<Scalars['Int']>>;
  notIn?: Maybe<Array<Scalars['Int']>>;
  lt?: Maybe<Scalars['Int']>;
  lte?: Maybe<Scalars['Int']>;
  gt?: Maybe<Scalars['Int']>;
  gte?: Maybe<Scalars['Int']>;
  not?: Maybe<GqlNestedIntNullableWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntNullableFilter>;
  _avg?: Maybe<GqlNestedFloatNullableFilter>;
  _sum?: Maybe<GqlNestedIntNullableFilter>;
  _min?: Maybe<GqlNestedIntNullableFilter>;
  _max?: Maybe<GqlNestedIntNullableFilter>;
};

export type GqlIntWithAggregatesFilter = {
  equals?: Maybe<Scalars['Int']>;
  in?: Maybe<Array<Scalars['Int']>>;
  notIn?: Maybe<Array<Scalars['Int']>>;
  lt?: Maybe<Scalars['Int']>;
  lte?: Maybe<Scalars['Int']>;
  gt?: Maybe<Scalars['Int']>;
  gte?: Maybe<Scalars['Int']>;
  not?: Maybe<GqlNestedIntWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntFilter>;
  _avg?: Maybe<GqlNestedFloatFilter>;
  _sum?: Maybe<GqlNestedIntFilter>;
  _min?: Maybe<GqlNestedIntFilter>;
  _max?: Maybe<GqlNestedIntFilter>;
};


export type GqlJsonFilter = {
  equals?: Maybe<Scalars['JSON']>;
  not?: Maybe<Scalars['JSON']>;
};

export type GqlJsonNullableFilter = {
  equals?: Maybe<Scalars['JSON']>;
  not?: Maybe<Scalars['JSON']>;
};

export type GqlJsonNullableWithAggregatesFilter = {
  equals?: Maybe<Scalars['JSON']>;
  not?: Maybe<Scalars['JSON']>;
  _count?: Maybe<GqlNestedIntNullableFilter>;
  _min?: Maybe<GqlNestedJsonNullableFilter>;
  _max?: Maybe<GqlNestedJsonNullableFilter>;
};

export type GqlJsonWithAggregatesFilter = {
  equals?: Maybe<Scalars['JSON']>;
  not?: Maybe<Scalars['JSON']>;
  _count?: Maybe<GqlNestedIntFilter>;
  _min?: Maybe<GqlNestedJsonFilter>;
  _max?: Maybe<GqlNestedJsonFilter>;
};

export type GqlMutation = {
  __typename?: 'Mutation';
  createArticle: FieldWrapper<GqlArticle>;
  createManyArticle: FieldWrapper<GqlAffectedRowsOutput>;
  deleteArticle?: Maybe<FieldWrapper<GqlArticle>>;
  updateArticle?: Maybe<FieldWrapper<GqlArticle>>;
  deleteManyArticle: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyArticle: FieldWrapper<GqlAffectedRowsOutput>;
  upsertArticle: FieldWrapper<GqlArticle>;
  createArticleRef: FieldWrapper<GqlArticleRef>;
  createManyArticleRef: FieldWrapper<GqlAffectedRowsOutput>;
  deleteArticleRef?: Maybe<FieldWrapper<GqlArticleRef>>;
  updateArticleRef?: Maybe<FieldWrapper<GqlArticleRef>>;
  deleteManyArticleRef: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyArticleRef: FieldWrapper<GqlAffectedRowsOutput>;
  upsertArticleRef: FieldWrapper<GqlArticleRef>;
  createBucket: FieldWrapper<GqlBucket>;
  createManyBucket: FieldWrapper<GqlAffectedRowsOutput>;
  deleteBucket?: Maybe<FieldWrapper<GqlBucket>>;
  updateBucket?: Maybe<FieldWrapper<GqlBucket>>;
  deleteManyBucket: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyBucket: FieldWrapper<GqlAffectedRowsOutput>;
  upsertBucket: FieldWrapper<GqlBucket>;
  createEntryPostProcessor: FieldWrapper<GqlEntryPostProcessor>;
  createManyEntryPostProcessor: FieldWrapper<GqlAffectedRowsOutput>;
  deleteEntryPostProcessor?: Maybe<FieldWrapper<GqlEntryPostProcessor>>;
  updateEntryPostProcessor?: Maybe<FieldWrapper<GqlEntryPostProcessor>>;
  deleteManyEntryPostProcessor: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyEntryPostProcessor: FieldWrapper<GqlAffectedRowsOutput>;
  upsertEntryPostProcessor: FieldWrapper<GqlEntryPostProcessor>;
  createFeed: FieldWrapper<GqlFeed>;
  createManyFeed: FieldWrapper<GqlAffectedRowsOutput>;
  deleteFeed?: Maybe<FieldWrapper<GqlFeed>>;
  updateFeed?: Maybe<FieldWrapper<GqlFeed>>;
  deleteManyFeed: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyFeed: FieldWrapper<GqlAffectedRowsOutput>;
  upsertFeed: FieldWrapper<GqlFeed>;
  createFeedEvent: FieldWrapper<GqlFeedEvent>;
  createManyFeedEvent: FieldWrapper<GqlAffectedRowsOutput>;
  deleteFeedEvent?: Maybe<FieldWrapper<GqlFeedEvent>>;
  updateFeedEvent?: Maybe<FieldWrapper<GqlFeedEvent>>;
  deleteManyFeedEvent: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyFeedEvent: FieldWrapper<GqlAffectedRowsOutput>;
  upsertFeedEvent: FieldWrapper<GqlFeedEvent>;
  createReleaseThrottle: FieldWrapper<GqlReleaseThrottle>;
  createManyReleaseThrottle: FieldWrapper<GqlAffectedRowsOutput>;
  deleteReleaseThrottle?: Maybe<FieldWrapper<GqlReleaseThrottle>>;
  updateReleaseThrottle?: Maybe<FieldWrapper<GqlReleaseThrottle>>;
  deleteManyReleaseThrottle: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyReleaseThrottle: FieldWrapper<GqlAffectedRowsOutput>;
  upsertReleaseThrottle: FieldWrapper<GqlReleaseThrottle>;
  createStream: FieldWrapper<GqlStream>;
  createManyStream: FieldWrapper<GqlAffectedRowsOutput>;
  deleteStream?: Maybe<FieldWrapper<GqlStream>>;
  updateStream?: Maybe<FieldWrapper<GqlStream>>;
  deleteManyStream: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyStream: FieldWrapper<GqlAffectedRowsOutput>;
  upsertStream: FieldWrapper<GqlStream>;
  createSubscription: FieldWrapper<GqlSubscription>;
  createManySubscription: FieldWrapper<GqlAffectedRowsOutput>;
  deleteSubscription?: Maybe<FieldWrapper<GqlSubscription>>;
  updateSubscription?: Maybe<FieldWrapper<GqlSubscription>>;
  deleteManySubscription: FieldWrapper<GqlAffectedRowsOutput>;
  updateManySubscription: FieldWrapper<GqlAffectedRowsOutput>;
  upsertSubscription: FieldWrapper<GqlSubscription>;
  createUser: FieldWrapper<GqlUser>;
  createManyUser: FieldWrapper<GqlAffectedRowsOutput>;
  deleteUser?: Maybe<FieldWrapper<GqlUser>>;
  updateUser?: Maybe<FieldWrapper<GqlUser>>;
  deleteManyUser: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyUser: FieldWrapper<GqlAffectedRowsOutput>;
  upsertUser: FieldWrapper<GqlUser>;
  createUserArticle: FieldWrapper<GqlUserArticle>;
  createManyUserArticle: FieldWrapper<GqlAffectedRowsOutput>;
  deleteUserArticle?: Maybe<FieldWrapper<GqlUserArticle>>;
  updateUserArticle?: Maybe<FieldWrapper<GqlUserArticle>>;
  deleteManyUserArticle: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyUserArticle: FieldWrapper<GqlAffectedRowsOutput>;
  upsertUserArticle: FieldWrapper<GqlUserArticle>;
  createUserFeed: FieldWrapper<GqlUserFeed>;
  createManyUserFeed: FieldWrapper<GqlAffectedRowsOutput>;
  deleteUserFeed?: Maybe<FieldWrapper<GqlUserFeed>>;
  updateUserFeed?: Maybe<FieldWrapper<GqlUserFeed>>;
  deleteManyUserFeed: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyUserFeed: FieldWrapper<GqlAffectedRowsOutput>;
  upsertUserFeed: FieldWrapper<GqlUserFeed>;
  subscribeToFeed: FieldWrapper<GqlSubscription>;
};


export type GqlMutationCreateArticleArgs = {
  data: GqlArticleCreateInput;
};


export type GqlMutationCreateManyArticleArgs = {
  data: Array<GqlArticleCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteArticleArgs = {
  where: GqlArticleWhereUniqueInput;
};


export type GqlMutationUpdateArticleArgs = {
  data: GqlArticleUpdateInput;
  where: GqlArticleWhereUniqueInput;
};


export type GqlMutationDeleteManyArticleArgs = {
  where?: Maybe<GqlArticleWhereInput>;
};


export type GqlMutationUpdateManyArticleArgs = {
  data: GqlArticleUpdateManyMutationInput;
  where?: Maybe<GqlArticleWhereInput>;
};


export type GqlMutationUpsertArticleArgs = {
  where: GqlArticleWhereUniqueInput;
  create: GqlArticleCreateInput;
  update: GqlArticleUpdateInput;
};


export type GqlMutationCreateArticleRefArgs = {
  data: GqlArticleRefCreateInput;
};


export type GqlMutationCreateManyArticleRefArgs = {
  data: Array<GqlArticleRefCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteArticleRefArgs = {
  where: GqlArticleRefWhereUniqueInput;
};


export type GqlMutationUpdateArticleRefArgs = {
  data: GqlArticleRefUpdateInput;
  where: GqlArticleRefWhereUniqueInput;
};


export type GqlMutationDeleteManyArticleRefArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
};


export type GqlMutationUpdateManyArticleRefArgs = {
  data: GqlArticleRefUpdateManyMutationInput;
  where?: Maybe<GqlArticleRefWhereInput>;
};


export type GqlMutationUpsertArticleRefArgs = {
  where: GqlArticleRefWhereUniqueInput;
  create: GqlArticleRefCreateInput;
  update: GqlArticleRefUpdateInput;
};


export type GqlMutationCreateBucketArgs = {
  data: GqlBucketCreateInput;
};


export type GqlMutationCreateManyBucketArgs = {
  data: Array<GqlBucketCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteBucketArgs = {
  where: GqlBucketWhereUniqueInput;
};


export type GqlMutationUpdateBucketArgs = {
  data: GqlBucketUpdateInput;
  where: GqlBucketWhereUniqueInput;
};


export type GqlMutationDeleteManyBucketArgs = {
  where?: Maybe<GqlBucketWhereInput>;
};


export type GqlMutationUpdateManyBucketArgs = {
  data: GqlBucketUpdateManyMutationInput;
  where?: Maybe<GqlBucketWhereInput>;
};


export type GqlMutationUpsertBucketArgs = {
  where: GqlBucketWhereUniqueInput;
  create: GqlBucketCreateInput;
  update: GqlBucketUpdateInput;
};


export type GqlMutationCreateEntryPostProcessorArgs = {
  data: GqlEntryPostProcessorCreateInput;
};


export type GqlMutationCreateManyEntryPostProcessorArgs = {
  data: Array<GqlEntryPostProcessorCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteEntryPostProcessorArgs = {
  where: GqlEntryPostProcessorWhereUniqueInput;
};


export type GqlMutationUpdateEntryPostProcessorArgs = {
  data: GqlEntryPostProcessorUpdateInput;
  where: GqlEntryPostProcessorWhereUniqueInput;
};


export type GqlMutationDeleteManyEntryPostProcessorArgs = {
  where?: Maybe<GqlEntryPostProcessorWhereInput>;
};


export type GqlMutationUpdateManyEntryPostProcessorArgs = {
  data: GqlEntryPostProcessorUpdateManyMutationInput;
  where?: Maybe<GqlEntryPostProcessorWhereInput>;
};


export type GqlMutationUpsertEntryPostProcessorArgs = {
  where: GqlEntryPostProcessorWhereUniqueInput;
  create: GqlEntryPostProcessorCreateInput;
  update: GqlEntryPostProcessorUpdateInput;
};


export type GqlMutationCreateFeedArgs = {
  data: GqlFeedCreateInput;
};


export type GqlMutationCreateManyFeedArgs = {
  data: Array<GqlFeedCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteFeedArgs = {
  where: GqlFeedWhereUniqueInput;
};


export type GqlMutationUpdateFeedArgs = {
  data: GqlFeedUpdateInput;
  where: GqlFeedWhereUniqueInput;
};


export type GqlMutationDeleteManyFeedArgs = {
  where?: Maybe<GqlFeedWhereInput>;
};


export type GqlMutationUpdateManyFeedArgs = {
  data: GqlFeedUpdateManyMutationInput;
  where?: Maybe<GqlFeedWhereInput>;
};


export type GqlMutationUpsertFeedArgs = {
  where: GqlFeedWhereUniqueInput;
  create: GqlFeedCreateInput;
  update: GqlFeedUpdateInput;
};


export type GqlMutationCreateFeedEventArgs = {
  data: GqlFeedEventCreateInput;
};


export type GqlMutationCreateManyFeedEventArgs = {
  data: Array<GqlFeedEventCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteFeedEventArgs = {
  where: GqlFeedEventWhereUniqueInput;
};


export type GqlMutationUpdateFeedEventArgs = {
  data: GqlFeedEventUpdateInput;
  where: GqlFeedEventWhereUniqueInput;
};


export type GqlMutationDeleteManyFeedEventArgs = {
  where?: Maybe<GqlFeedEventWhereInput>;
};


export type GqlMutationUpdateManyFeedEventArgs = {
  data: GqlFeedEventUpdateManyMutationInput;
  where?: Maybe<GqlFeedEventWhereInput>;
};


export type GqlMutationUpsertFeedEventArgs = {
  where: GqlFeedEventWhereUniqueInput;
  create: GqlFeedEventCreateInput;
  update: GqlFeedEventUpdateInput;
};


export type GqlMutationCreateReleaseThrottleArgs = {
  data: GqlReleaseThrottleCreateInput;
};


export type GqlMutationCreateManyReleaseThrottleArgs = {
  data: Array<GqlReleaseThrottleCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteReleaseThrottleArgs = {
  where: GqlReleaseThrottleWhereUniqueInput;
};


export type GqlMutationUpdateReleaseThrottleArgs = {
  data: GqlReleaseThrottleUpdateInput;
  where: GqlReleaseThrottleWhereUniqueInput;
};


export type GqlMutationDeleteManyReleaseThrottleArgs = {
  where?: Maybe<GqlReleaseThrottleWhereInput>;
};


export type GqlMutationUpdateManyReleaseThrottleArgs = {
  data: GqlReleaseThrottleUpdateManyMutationInput;
  where?: Maybe<GqlReleaseThrottleWhereInput>;
};


export type GqlMutationUpsertReleaseThrottleArgs = {
  where: GqlReleaseThrottleWhereUniqueInput;
  create: GqlReleaseThrottleCreateInput;
  update: GqlReleaseThrottleUpdateInput;
};


export type GqlMutationCreateStreamArgs = {
  data: GqlStreamCreateInput;
};


export type GqlMutationCreateManyStreamArgs = {
  data: Array<GqlStreamCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteStreamArgs = {
  where: GqlStreamWhereUniqueInput;
};


export type GqlMutationUpdateStreamArgs = {
  data: GqlStreamUpdateInput;
  where: GqlStreamWhereUniqueInput;
};


export type GqlMutationDeleteManyStreamArgs = {
  where?: Maybe<GqlStreamWhereInput>;
};


export type GqlMutationUpdateManyStreamArgs = {
  data: GqlStreamUpdateManyMutationInput;
  where?: Maybe<GqlStreamWhereInput>;
};


export type GqlMutationUpsertStreamArgs = {
  where: GqlStreamWhereUniqueInput;
  create: GqlStreamCreateInput;
  update: GqlStreamUpdateInput;
};


export type GqlMutationCreateSubscriptionArgs = {
  data: GqlSubscriptionCreateInput;
};


export type GqlMutationCreateManySubscriptionArgs = {
  data: Array<GqlSubscriptionCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteSubscriptionArgs = {
  where: GqlSubscriptionWhereUniqueInput;
};


export type GqlMutationUpdateSubscriptionArgs = {
  data: GqlSubscriptionUpdateInput;
  where: GqlSubscriptionWhereUniqueInput;
};


export type GqlMutationDeleteManySubscriptionArgs = {
  where?: Maybe<GqlSubscriptionWhereInput>;
};


export type GqlMutationUpdateManySubscriptionArgs = {
  data: GqlSubscriptionUpdateManyMutationInput;
  where?: Maybe<GqlSubscriptionWhereInput>;
};


export type GqlMutationUpsertSubscriptionArgs = {
  where: GqlSubscriptionWhereUniqueInput;
  create: GqlSubscriptionCreateInput;
  update: GqlSubscriptionUpdateInput;
};


export type GqlMutationCreateUserArgs = {
  data: GqlUserCreateInput;
};


export type GqlMutationCreateManyUserArgs = {
  data: Array<GqlUserCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteUserArgs = {
  where: GqlUserWhereUniqueInput;
};


export type GqlMutationUpdateUserArgs = {
  data: GqlUserUpdateInput;
  where: GqlUserWhereUniqueInput;
};


export type GqlMutationDeleteManyUserArgs = {
  where?: Maybe<GqlUserWhereInput>;
};


export type GqlMutationUpdateManyUserArgs = {
  data: GqlUserUpdateManyMutationInput;
  where?: Maybe<GqlUserWhereInput>;
};


export type GqlMutationUpsertUserArgs = {
  where: GqlUserWhereUniqueInput;
  create: GqlUserCreateInput;
  update: GqlUserUpdateInput;
};


export type GqlMutationCreateUserArticleArgs = {
  data: GqlUserArticleCreateInput;
};


export type GqlMutationCreateManyUserArticleArgs = {
  data: Array<GqlUserArticleCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteUserArticleArgs = {
  where: GqlUserArticleWhereUniqueInput;
};


export type GqlMutationUpdateUserArticleArgs = {
  data: GqlUserArticleUpdateInput;
  where: GqlUserArticleWhereUniqueInput;
};


export type GqlMutationDeleteManyUserArticleArgs = {
  where?: Maybe<GqlUserArticleWhereInput>;
};


export type GqlMutationUpdateManyUserArticleArgs = {
  data: GqlUserArticleUpdateManyMutationInput;
  where?: Maybe<GqlUserArticleWhereInput>;
};


export type GqlMutationUpsertUserArticleArgs = {
  where: GqlUserArticleWhereUniqueInput;
  create: GqlUserArticleCreateInput;
  update: GqlUserArticleUpdateInput;
};


export type GqlMutationCreateUserFeedArgs = {
  data: GqlUserFeedCreateInput;
};


export type GqlMutationCreateManyUserFeedArgs = {
  data: Array<GqlUserFeedCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteUserFeedArgs = {
  where: GqlUserFeedWhereUniqueInput;
};


export type GqlMutationUpdateUserFeedArgs = {
  data: GqlUserFeedUpdateInput;
  where: GqlUserFeedWhereUniqueInput;
};


export type GqlMutationDeleteManyUserFeedArgs = {
  where?: Maybe<GqlUserFeedWhereInput>;
};


export type GqlMutationUpdateManyUserFeedArgs = {
  data: GqlUserFeedUpdateManyMutationInput;
  where?: Maybe<GqlUserFeedWhereInput>;
};


export type GqlMutationUpsertUserFeedArgs = {
  where: GqlUserFeedWhereUniqueInput;
  create: GqlUserFeedCreateInput;
  update: GqlUserFeedUpdateInput;
};


export type GqlMutationSubscribeToFeedArgs = {
  email: Scalars['String'];
  bucketId: Scalars['String'];
  feedUrl: Scalars['String'];
};

export type GqlNestedBoolFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<GqlNestedBoolFilter>;
};

export type GqlNestedBoolWithAggregatesFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<GqlNestedBoolWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntFilter>;
  _min?: Maybe<GqlNestedBoolFilter>;
  _max?: Maybe<GqlNestedBoolFilter>;
};

export type GqlNestedDateTimeFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<GqlNestedDateTimeFilter>;
};

export type GqlNestedDateTimeNullableFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<GqlNestedDateTimeNullableFilter>;
};

export type GqlNestedDateTimeNullableWithAggregatesFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<GqlNestedDateTimeNullableWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntNullableFilter>;
  _min?: Maybe<GqlNestedDateTimeNullableFilter>;
  _max?: Maybe<GqlNestedDateTimeNullableFilter>;
};

export type GqlNestedDateTimeWithAggregatesFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<GqlNestedDateTimeWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntFilter>;
  _min?: Maybe<GqlNestedDateTimeFilter>;
  _max?: Maybe<GqlNestedDateTimeFilter>;
};

export type GqlNestedFloatFilter = {
  equals?: Maybe<Scalars['Float']>;
  in?: Maybe<Array<Scalars['Float']>>;
  notIn?: Maybe<Array<Scalars['Float']>>;
  lt?: Maybe<Scalars['Float']>;
  lte?: Maybe<Scalars['Float']>;
  gt?: Maybe<Scalars['Float']>;
  gte?: Maybe<Scalars['Float']>;
  not?: Maybe<GqlNestedFloatFilter>;
};

export type GqlNestedFloatNullableFilter = {
  equals?: Maybe<Scalars['Float']>;
  in?: Maybe<Array<Scalars['Float']>>;
  notIn?: Maybe<Array<Scalars['Float']>>;
  lt?: Maybe<Scalars['Float']>;
  lte?: Maybe<Scalars['Float']>;
  gt?: Maybe<Scalars['Float']>;
  gte?: Maybe<Scalars['Float']>;
  not?: Maybe<GqlNestedFloatNullableFilter>;
};

export type GqlNestedFloatNullableWithAggregatesFilter = {
  equals?: Maybe<Scalars['Float']>;
  in?: Maybe<Array<Scalars['Float']>>;
  notIn?: Maybe<Array<Scalars['Float']>>;
  lt?: Maybe<Scalars['Float']>;
  lte?: Maybe<Scalars['Float']>;
  gt?: Maybe<Scalars['Float']>;
  gte?: Maybe<Scalars['Float']>;
  not?: Maybe<GqlNestedFloatNullableWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntNullableFilter>;
  _avg?: Maybe<GqlNestedFloatNullableFilter>;
  _sum?: Maybe<GqlNestedFloatNullableFilter>;
  _min?: Maybe<GqlNestedFloatNullableFilter>;
  _max?: Maybe<GqlNestedFloatNullableFilter>;
};

export type GqlNestedIntFilter = {
  equals?: Maybe<Scalars['Int']>;
  in?: Maybe<Array<Scalars['Int']>>;
  notIn?: Maybe<Array<Scalars['Int']>>;
  lt?: Maybe<Scalars['Int']>;
  lte?: Maybe<Scalars['Int']>;
  gt?: Maybe<Scalars['Int']>;
  gte?: Maybe<Scalars['Int']>;
  not?: Maybe<GqlNestedIntFilter>;
};

export type GqlNestedIntNullableFilter = {
  equals?: Maybe<Scalars['Int']>;
  in?: Maybe<Array<Scalars['Int']>>;
  notIn?: Maybe<Array<Scalars['Int']>>;
  lt?: Maybe<Scalars['Int']>;
  lte?: Maybe<Scalars['Int']>;
  gt?: Maybe<Scalars['Int']>;
  gte?: Maybe<Scalars['Int']>;
  not?: Maybe<GqlNestedIntNullableFilter>;
};

export type GqlNestedIntNullableWithAggregatesFilter = {
  equals?: Maybe<Scalars['Int']>;
  in?: Maybe<Array<Scalars['Int']>>;
  notIn?: Maybe<Array<Scalars['Int']>>;
  lt?: Maybe<Scalars['Int']>;
  lte?: Maybe<Scalars['Int']>;
  gt?: Maybe<Scalars['Int']>;
  gte?: Maybe<Scalars['Int']>;
  not?: Maybe<GqlNestedIntNullableWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntNullableFilter>;
  _avg?: Maybe<GqlNestedFloatNullableFilter>;
  _sum?: Maybe<GqlNestedIntNullableFilter>;
  _min?: Maybe<GqlNestedIntNullableFilter>;
  _max?: Maybe<GqlNestedIntNullableFilter>;
};

export type GqlNestedIntWithAggregatesFilter = {
  equals?: Maybe<Scalars['Int']>;
  in?: Maybe<Array<Scalars['Int']>>;
  notIn?: Maybe<Array<Scalars['Int']>>;
  lt?: Maybe<Scalars['Int']>;
  lte?: Maybe<Scalars['Int']>;
  gt?: Maybe<Scalars['Int']>;
  gte?: Maybe<Scalars['Int']>;
  not?: Maybe<GqlNestedIntWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntFilter>;
  _avg?: Maybe<GqlNestedFloatFilter>;
  _sum?: Maybe<GqlNestedIntFilter>;
  _min?: Maybe<GqlNestedIntFilter>;
  _max?: Maybe<GqlNestedIntFilter>;
};

export type GqlNestedJsonFilter = {
  equals?: Maybe<Scalars['JSON']>;
  not?: Maybe<Scalars['JSON']>;
};

export type GqlNestedJsonNullableFilter = {
  equals?: Maybe<Scalars['JSON']>;
  not?: Maybe<Scalars['JSON']>;
};

export type GqlNestedStringFilter = {
  equals?: Maybe<Scalars['String']>;
  in?: Maybe<Array<Scalars['String']>>;
  notIn?: Maybe<Array<Scalars['String']>>;
  lt?: Maybe<Scalars['String']>;
  lte?: Maybe<Scalars['String']>;
  gt?: Maybe<Scalars['String']>;
  gte?: Maybe<Scalars['String']>;
  contains?: Maybe<Scalars['String']>;
  startsWith?: Maybe<Scalars['String']>;
  endsWith?: Maybe<Scalars['String']>;
  not?: Maybe<GqlNestedStringFilter>;
};

export type GqlNestedStringNullableFilter = {
  equals?: Maybe<Scalars['String']>;
  in?: Maybe<Array<Scalars['String']>>;
  notIn?: Maybe<Array<Scalars['String']>>;
  lt?: Maybe<Scalars['String']>;
  lte?: Maybe<Scalars['String']>;
  gt?: Maybe<Scalars['String']>;
  gte?: Maybe<Scalars['String']>;
  contains?: Maybe<Scalars['String']>;
  startsWith?: Maybe<Scalars['String']>;
  endsWith?: Maybe<Scalars['String']>;
  not?: Maybe<GqlNestedStringNullableFilter>;
};

export type GqlNestedStringNullableWithAggregatesFilter = {
  equals?: Maybe<Scalars['String']>;
  in?: Maybe<Array<Scalars['String']>>;
  notIn?: Maybe<Array<Scalars['String']>>;
  lt?: Maybe<Scalars['String']>;
  lte?: Maybe<Scalars['String']>;
  gt?: Maybe<Scalars['String']>;
  gte?: Maybe<Scalars['String']>;
  contains?: Maybe<Scalars['String']>;
  startsWith?: Maybe<Scalars['String']>;
  endsWith?: Maybe<Scalars['String']>;
  not?: Maybe<GqlNestedStringNullableWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntNullableFilter>;
  _min?: Maybe<GqlNestedStringNullableFilter>;
  _max?: Maybe<GqlNestedStringNullableFilter>;
};

export type GqlNestedStringWithAggregatesFilter = {
  equals?: Maybe<Scalars['String']>;
  in?: Maybe<Array<Scalars['String']>>;
  notIn?: Maybe<Array<Scalars['String']>>;
  lt?: Maybe<Scalars['String']>;
  lte?: Maybe<Scalars['String']>;
  gt?: Maybe<Scalars['String']>;
  gte?: Maybe<Scalars['String']>;
  contains?: Maybe<Scalars['String']>;
  startsWith?: Maybe<Scalars['String']>;
  endsWith?: Maybe<Scalars['String']>;
  not?: Maybe<GqlNestedStringWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntFilter>;
  _min?: Maybe<GqlNestedStringFilter>;
  _max?: Maybe<GqlNestedStringFilter>;
};

export type GqlNullableDateTimeFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['DateTime']>;
};

export type GqlNullableFloatFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['Float']>;
  increment?: Maybe<Scalars['Float']>;
  decrement?: Maybe<Scalars['Float']>;
  multiply?: Maybe<Scalars['Float']>;
  divide?: Maybe<Scalars['Float']>;
};

export type GqlNullableIntFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['Int']>;
  increment?: Maybe<Scalars['Int']>;
  decrement?: Maybe<Scalars['Int']>;
  multiply?: Maybe<Scalars['Int']>;
  divide?: Maybe<Scalars['Int']>;
};

export type GqlNullableStringFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['String']>;
};

export type GqlQuery = {
  __typename?: 'Query';
  article?: Maybe<FieldWrapper<GqlArticle>>;
  findFirstArticle?: Maybe<FieldWrapper<GqlArticle>>;
  articles: Array<FieldWrapper<GqlArticle>>;
  aggregateArticle: FieldWrapper<GqlAggregateArticle>;
  groupByArticle: Array<FieldWrapper<GqlArticleGroupBy>>;
  articleRef?: Maybe<FieldWrapper<GqlArticleRef>>;
  findFirstArticleRef?: Maybe<FieldWrapper<GqlArticleRef>>;
  articleRefs: Array<FieldWrapper<GqlArticleRef>>;
  aggregateArticleRef: FieldWrapper<GqlAggregateArticleRef>;
  groupByArticleRef: Array<FieldWrapper<GqlArticleRefGroupBy>>;
  bucket?: Maybe<FieldWrapper<GqlBucket>>;
  findFirstBucket?: Maybe<FieldWrapper<GqlBucket>>;
  buckets: Array<FieldWrapper<GqlBucket>>;
  aggregateBucket: FieldWrapper<GqlAggregateBucket>;
  groupByBucket: Array<FieldWrapper<GqlBucketGroupBy>>;
  entryPostProcessor?: Maybe<FieldWrapper<GqlEntryPostProcessor>>;
  findFirstEntryPostProcessor?: Maybe<FieldWrapper<GqlEntryPostProcessor>>;
  entryPostProcessors: Array<FieldWrapper<GqlEntryPostProcessor>>;
  aggregateEntryPostProcessor: FieldWrapper<GqlAggregateEntryPostProcessor>;
  groupByEntryPostProcessor: Array<FieldWrapper<GqlEntryPostProcessorGroupBy>>;
  feed?: Maybe<FieldWrapper<GqlFeed>>;
  findFirstFeed?: Maybe<FieldWrapper<GqlFeed>>;
  feeds: Array<FieldWrapper<GqlFeed>>;
  aggregateFeed: FieldWrapper<GqlAggregateFeed>;
  groupByFeed: Array<FieldWrapper<GqlFeedGroupBy>>;
  feedEvent?: Maybe<FieldWrapper<GqlFeedEvent>>;
  findFirstFeedEvent?: Maybe<FieldWrapper<GqlFeedEvent>>;
  feedEvents: Array<FieldWrapper<GqlFeedEvent>>;
  aggregateFeedEvent: FieldWrapper<GqlAggregateFeedEvent>;
  groupByFeedEvent: Array<FieldWrapper<GqlFeedEventGroupBy>>;
  releaseThrottle?: Maybe<FieldWrapper<GqlReleaseThrottle>>;
  findFirstReleaseThrottle?: Maybe<FieldWrapper<GqlReleaseThrottle>>;
  releaseThrottles: Array<FieldWrapper<GqlReleaseThrottle>>;
  aggregateReleaseThrottle: FieldWrapper<GqlAggregateReleaseThrottle>;
  groupByReleaseThrottle: Array<FieldWrapper<GqlReleaseThrottleGroupBy>>;
  stream?: Maybe<FieldWrapper<GqlStream>>;
  findFirstStream?: Maybe<FieldWrapper<GqlStream>>;
  streams: Array<FieldWrapper<GqlStream>>;
  aggregateStream: FieldWrapper<GqlAggregateStream>;
  groupByStream: Array<FieldWrapper<GqlStreamGroupBy>>;
  subscription?: Maybe<FieldWrapper<GqlSubscription>>;
  findFirstSubscription?: Maybe<FieldWrapper<GqlSubscription>>;
  subscriptions: Array<FieldWrapper<GqlSubscription>>;
  aggregateSubscription: FieldWrapper<GqlAggregateSubscription>;
  groupBySubscription: Array<FieldWrapper<GqlSubscriptionGroupBy>>;
  user?: Maybe<FieldWrapper<GqlUser>>;
  findFirstUser?: Maybe<FieldWrapper<GqlUser>>;
  users: Array<FieldWrapper<GqlUser>>;
  aggregateUser: FieldWrapper<GqlAggregateUser>;
  groupByUser: Array<FieldWrapper<GqlUserGroupBy>>;
  userArticle?: Maybe<FieldWrapper<GqlUserArticle>>;
  findFirstUserArticle?: Maybe<FieldWrapper<GqlUserArticle>>;
  userArticles: Array<FieldWrapper<GqlUserArticle>>;
  aggregateUserArticle: FieldWrapper<GqlAggregateUserArticle>;
  groupByUserArticle: Array<FieldWrapper<GqlUserArticleGroupBy>>;
  userFeed?: Maybe<FieldWrapper<GqlUserFeed>>;
  findFirstUserFeed?: Maybe<FieldWrapper<GqlUserFeed>>;
  userFeeds: Array<FieldWrapper<GqlUserFeed>>;
  aggregateUserFeed: FieldWrapper<GqlAggregateUserFeed>;
  groupByUserFeed: Array<FieldWrapper<GqlUserFeedGroupBy>>;
  discoverFeedsByQuery: Array<FieldWrapper<GqlDiscoveredFeed>>;
  articlesForFeedUrl: Array<FieldWrapper<GqlArticleRef>>;
};


export type GqlQueryArticleArgs = {
  where: GqlArticleWhereUniqueInput;
};


export type GqlQueryFindFirstArticleArgs = {
  where?: Maybe<GqlArticleWhereInput>;
  orderBy?: Maybe<Array<GqlArticleOrderByInput>>;
  cursor?: Maybe<GqlArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleScalarFieldEnum>>;
};


export type GqlQueryArticlesArgs = {
  where?: Maybe<GqlArticleWhereInput>;
  orderBy?: Maybe<Array<GqlArticleOrderByInput>>;
  cursor?: Maybe<GqlArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleScalarFieldEnum>>;
};


export type GqlQueryAggregateArticleArgs = {
  where?: Maybe<GqlArticleWhereInput>;
  orderBy?: Maybe<Array<GqlArticleOrderByInput>>;
  cursor?: Maybe<GqlArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByArticleArgs = {
  where?: Maybe<GqlArticleWhereInput>;
  orderBy?: Maybe<Array<GqlArticleOrderByInput>>;
  by: Array<GqlArticleScalarFieldEnum>;
  having?: Maybe<GqlArticleScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryArticleRefArgs = {
  where: GqlArticleRefWhereUniqueInput;
};


export type GqlQueryFindFirstArticleRefArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlArticleRefOrderByInput>>;
  cursor?: Maybe<GqlArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleRefScalarFieldEnum>>;
};


export type GqlQueryArticleRefsArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlArticleRefOrderByInput>>;
  cursor?: Maybe<GqlArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleRefScalarFieldEnum>>;
};


export type GqlQueryAggregateArticleRefArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlArticleRefOrderByInput>>;
  cursor?: Maybe<GqlArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByArticleRefArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlArticleRefOrderByInput>>;
  by: Array<GqlArticleRefScalarFieldEnum>;
  having?: Maybe<GqlArticleRefScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryBucketArgs = {
  where: GqlBucketWhereUniqueInput;
};


export type GqlQueryFindFirstBucketArgs = {
  where?: Maybe<GqlBucketWhereInput>;
  orderBy?: Maybe<Array<GqlBucketOrderByInput>>;
  cursor?: Maybe<GqlBucketWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlBucketScalarFieldEnum>>;
};


export type GqlQueryBucketsArgs = {
  where?: Maybe<GqlBucketWhereInput>;
  orderBy?: Maybe<Array<GqlBucketOrderByInput>>;
  cursor?: Maybe<GqlBucketWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlBucketScalarFieldEnum>>;
};


export type GqlQueryAggregateBucketArgs = {
  where?: Maybe<GqlBucketWhereInput>;
  orderBy?: Maybe<Array<GqlBucketOrderByInput>>;
  cursor?: Maybe<GqlBucketWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByBucketArgs = {
  where?: Maybe<GqlBucketWhereInput>;
  orderBy?: Maybe<Array<GqlBucketOrderByInput>>;
  by: Array<GqlBucketScalarFieldEnum>;
  having?: Maybe<GqlBucketScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryEntryPostProcessorArgs = {
  where: GqlEntryPostProcessorWhereUniqueInput;
};


export type GqlQueryFindFirstEntryPostProcessorArgs = {
  where?: Maybe<GqlEntryPostProcessorWhereInput>;
  orderBy?: Maybe<Array<GqlEntryPostProcessorOrderByInput>>;
  cursor?: Maybe<GqlEntryPostProcessorWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlEntryPostProcessorScalarFieldEnum>>;
};


export type GqlQueryEntryPostProcessorsArgs = {
  where?: Maybe<GqlEntryPostProcessorWhereInput>;
  orderBy?: Maybe<Array<GqlEntryPostProcessorOrderByInput>>;
  cursor?: Maybe<GqlEntryPostProcessorWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlEntryPostProcessorScalarFieldEnum>>;
};


export type GqlQueryAggregateEntryPostProcessorArgs = {
  where?: Maybe<GqlEntryPostProcessorWhereInput>;
  orderBy?: Maybe<Array<GqlEntryPostProcessorOrderByInput>>;
  cursor?: Maybe<GqlEntryPostProcessorWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByEntryPostProcessorArgs = {
  where?: Maybe<GqlEntryPostProcessorWhereInput>;
  orderBy?: Maybe<Array<GqlEntryPostProcessorOrderByInput>>;
  by: Array<GqlEntryPostProcessorScalarFieldEnum>;
  having?: Maybe<GqlEntryPostProcessorScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryFeedArgs = {
  where: GqlFeedWhereUniqueInput;
};


export type GqlQueryFindFirstFeedArgs = {
  where?: Maybe<GqlFeedWhereInput>;
  orderBy?: Maybe<Array<GqlFeedOrderByInput>>;
  cursor?: Maybe<GqlFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlFeedScalarFieldEnum>>;
};


export type GqlQueryFeedsArgs = {
  where?: Maybe<GqlFeedWhereInput>;
  orderBy?: Maybe<Array<GqlFeedOrderByInput>>;
  cursor?: Maybe<GqlFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlFeedScalarFieldEnum>>;
};


export type GqlQueryAggregateFeedArgs = {
  where?: Maybe<GqlFeedWhereInput>;
  orderBy?: Maybe<Array<GqlFeedOrderByInput>>;
  cursor?: Maybe<GqlFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByFeedArgs = {
  where?: Maybe<GqlFeedWhereInput>;
  orderBy?: Maybe<Array<GqlFeedOrderByInput>>;
  by: Array<GqlFeedScalarFieldEnum>;
  having?: Maybe<GqlFeedScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryFeedEventArgs = {
  where: GqlFeedEventWhereUniqueInput;
};


export type GqlQueryFindFirstFeedEventArgs = {
  where?: Maybe<GqlFeedEventWhereInput>;
  orderBy?: Maybe<Array<GqlFeedEventOrderByInput>>;
  cursor?: Maybe<GqlFeedEventWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlFeedEventScalarFieldEnum>>;
};


export type GqlQueryFeedEventsArgs = {
  where?: Maybe<GqlFeedEventWhereInput>;
  orderBy?: Maybe<Array<GqlFeedEventOrderByInput>>;
  cursor?: Maybe<GqlFeedEventWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlFeedEventScalarFieldEnum>>;
};


export type GqlQueryAggregateFeedEventArgs = {
  where?: Maybe<GqlFeedEventWhereInput>;
  orderBy?: Maybe<Array<GqlFeedEventOrderByInput>>;
  cursor?: Maybe<GqlFeedEventWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByFeedEventArgs = {
  where?: Maybe<GqlFeedEventWhereInput>;
  orderBy?: Maybe<Array<GqlFeedEventOrderByInput>>;
  by: Array<GqlFeedEventScalarFieldEnum>;
  having?: Maybe<GqlFeedEventScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryReleaseThrottleArgs = {
  where: GqlReleaseThrottleWhereUniqueInput;
};


export type GqlQueryFindFirstReleaseThrottleArgs = {
  where?: Maybe<GqlReleaseThrottleWhereInput>;
  orderBy?: Maybe<Array<GqlReleaseThrottleOrderByInput>>;
  cursor?: Maybe<GqlReleaseThrottleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlReleaseThrottleScalarFieldEnum>>;
};


export type GqlQueryReleaseThrottlesArgs = {
  where?: Maybe<GqlReleaseThrottleWhereInput>;
  orderBy?: Maybe<Array<GqlReleaseThrottleOrderByInput>>;
  cursor?: Maybe<GqlReleaseThrottleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlReleaseThrottleScalarFieldEnum>>;
};


export type GqlQueryAggregateReleaseThrottleArgs = {
  where?: Maybe<GqlReleaseThrottleWhereInput>;
  orderBy?: Maybe<Array<GqlReleaseThrottleOrderByInput>>;
  cursor?: Maybe<GqlReleaseThrottleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByReleaseThrottleArgs = {
  where?: Maybe<GqlReleaseThrottleWhereInput>;
  orderBy?: Maybe<Array<GqlReleaseThrottleOrderByInput>>;
  by: Array<GqlReleaseThrottleScalarFieldEnum>;
  having?: Maybe<GqlReleaseThrottleScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryStreamArgs = {
  where: GqlStreamWhereUniqueInput;
};


export type GqlQueryFindFirstStreamArgs = {
  where?: Maybe<GqlStreamWhereInput>;
  orderBy?: Maybe<Array<GqlStreamOrderByInput>>;
  cursor?: Maybe<GqlStreamWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlStreamScalarFieldEnum>>;
};


export type GqlQueryStreamsArgs = {
  where?: Maybe<GqlStreamWhereInput>;
  orderBy?: Maybe<Array<GqlStreamOrderByInput>>;
  cursor?: Maybe<GqlStreamWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlStreamScalarFieldEnum>>;
};


export type GqlQueryAggregateStreamArgs = {
  where?: Maybe<GqlStreamWhereInput>;
  orderBy?: Maybe<Array<GqlStreamOrderByInput>>;
  cursor?: Maybe<GqlStreamWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByStreamArgs = {
  where?: Maybe<GqlStreamWhereInput>;
  orderBy?: Maybe<Array<GqlStreamOrderByInput>>;
  by: Array<GqlStreamScalarFieldEnum>;
  having?: Maybe<GqlStreamScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQuerySubscriptionArgs = {
  where: GqlSubscriptionWhereUniqueInput;
};


export type GqlQueryFindFirstSubscriptionArgs = {
  where?: Maybe<GqlSubscriptionWhereInput>;
  orderBy?: Maybe<Array<GqlSubscriptionOrderByInput>>;
  cursor?: Maybe<GqlSubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlSubscriptionScalarFieldEnum>>;
};


export type GqlQuerySubscriptionsArgs = {
  where?: Maybe<GqlSubscriptionWhereInput>;
  orderBy?: Maybe<Array<GqlSubscriptionOrderByInput>>;
  cursor?: Maybe<GqlSubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlSubscriptionScalarFieldEnum>>;
};


export type GqlQueryAggregateSubscriptionArgs = {
  where?: Maybe<GqlSubscriptionWhereInput>;
  orderBy?: Maybe<Array<GqlSubscriptionOrderByInput>>;
  cursor?: Maybe<GqlSubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupBySubscriptionArgs = {
  where?: Maybe<GqlSubscriptionWhereInput>;
  orderBy?: Maybe<Array<GqlSubscriptionOrderByInput>>;
  by: Array<GqlSubscriptionScalarFieldEnum>;
  having?: Maybe<GqlSubscriptionScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryUserArgs = {
  where: GqlUserWhereUniqueInput;
};


export type GqlQueryFindFirstUserArgs = {
  where?: Maybe<GqlUserWhereInput>;
  orderBy?: Maybe<Array<GqlUserOrderByInput>>;
  cursor?: Maybe<GqlUserWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlUserScalarFieldEnum>>;
};


export type GqlQueryUsersArgs = {
  where?: Maybe<GqlUserWhereInput>;
  orderBy?: Maybe<Array<GqlUserOrderByInput>>;
  cursor?: Maybe<GqlUserWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlUserScalarFieldEnum>>;
};


export type GqlQueryAggregateUserArgs = {
  where?: Maybe<GqlUserWhereInput>;
  orderBy?: Maybe<Array<GqlUserOrderByInput>>;
  cursor?: Maybe<GqlUserWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByUserArgs = {
  where?: Maybe<GqlUserWhereInput>;
  orderBy?: Maybe<Array<GqlUserOrderByInput>>;
  by: Array<GqlUserScalarFieldEnum>;
  having?: Maybe<GqlUserScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryUserArticleArgs = {
  where: GqlUserArticleWhereUniqueInput;
};


export type GqlQueryFindFirstUserArticleArgs = {
  where?: Maybe<GqlUserArticleWhereInput>;
  orderBy?: Maybe<Array<GqlUserArticleOrderByInput>>;
  cursor?: Maybe<GqlUserArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlUserArticleScalarFieldEnum>>;
};


export type GqlQueryUserArticlesArgs = {
  where?: Maybe<GqlUserArticleWhereInput>;
  orderBy?: Maybe<Array<GqlUserArticleOrderByInput>>;
  cursor?: Maybe<GqlUserArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlUserArticleScalarFieldEnum>>;
};


export type GqlQueryAggregateUserArticleArgs = {
  where?: Maybe<GqlUserArticleWhereInput>;
  orderBy?: Maybe<Array<GqlUserArticleOrderByInput>>;
  cursor?: Maybe<GqlUserArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByUserArticleArgs = {
  where?: Maybe<GqlUserArticleWhereInput>;
  orderBy?: Maybe<Array<GqlUserArticleOrderByInput>>;
  by: Array<GqlUserArticleScalarFieldEnum>;
  having?: Maybe<GqlUserArticleScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryUserFeedArgs = {
  where: GqlUserFeedWhereUniqueInput;
};


export type GqlQueryFindFirstUserFeedArgs = {
  where?: Maybe<GqlUserFeedWhereInput>;
  orderBy?: Maybe<Array<GqlUserFeedOrderByInput>>;
  cursor?: Maybe<GqlUserFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlUserFeedScalarFieldEnum>>;
};


export type GqlQueryUserFeedsArgs = {
  where?: Maybe<GqlUserFeedWhereInput>;
  orderBy?: Maybe<Array<GqlUserFeedOrderByInput>>;
  cursor?: Maybe<GqlUserFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlUserFeedScalarFieldEnum>>;
};


export type GqlQueryAggregateUserFeedArgs = {
  where?: Maybe<GqlUserFeedWhereInput>;
  orderBy?: Maybe<Array<GqlUserFeedOrderByInput>>;
  cursor?: Maybe<GqlUserFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByUserFeedArgs = {
  where?: Maybe<GqlUserFeedWhereInput>;
  orderBy?: Maybe<Array<GqlUserFeedOrderByInput>>;
  by: Array<GqlUserFeedScalarFieldEnum>;
  having?: Maybe<GqlUserFeedScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryDiscoverFeedsByQueryArgs = {
  query: Scalars['String'];
};


export type GqlQueryArticlesForFeedUrlArgs = {
  feedUrl: Scalars['String'];
};

export type GqlReleaseThrottle = {
  __typename?: 'ReleaseThrottle';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  take: FieldWrapper<Scalars['Int']>;
  window: FieldWrapper<Scalars['String']>;
  scoreCriteria?: Maybe<FieldWrapper<Scalars['String']>>;
  nextReleaseAt: FieldWrapper<Scalars['DateTime']>;
  Subscription: Array<FieldWrapper<GqlSubscription>>;
};


export type GqlReleaseThrottleSubscriptionArgs = {
  where?: Maybe<GqlSubscriptionWhereInput>;
  orderBy?: Maybe<Array<GqlSubscriptionOrderByInput>>;
  cursor?: Maybe<GqlSubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlSubscriptionScalarFieldEnum>>;
};

export type GqlReleaseThrottleAvgAggregate = {
  __typename?: 'ReleaseThrottleAvgAggregate';
  take?: Maybe<FieldWrapper<Scalars['Float']>>;
};

export type GqlReleaseThrottleCountAggregate = {
  __typename?: 'ReleaseThrottleCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  take: FieldWrapper<Scalars['Int']>;
  window: FieldWrapper<Scalars['Int']>;
  scoreCriteria: FieldWrapper<Scalars['Int']>;
  nextReleaseAt: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlReleaseThrottleCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  take?: Maybe<Scalars['Int']>;
  window?: Maybe<Scalars['String']>;
  scoreCriteria?: Maybe<Scalars['String']>;
  nextReleaseAt?: Maybe<Scalars['DateTime']>;
  Subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutThrottleInput>;
};

export type GqlReleaseThrottleCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  take?: Maybe<Scalars['Int']>;
  window?: Maybe<Scalars['String']>;
  scoreCriteria?: Maybe<Scalars['String']>;
  nextReleaseAt?: Maybe<Scalars['DateTime']>;
};

export type GqlReleaseThrottleCreateNestedOneWithoutSubscriptionInput = {
  create?: Maybe<GqlReleaseThrottleCreateWithoutSubscriptionInput>;
  connectOrCreate?: Maybe<GqlReleaseThrottleCreateOrConnectWithoutSubscriptionInput>;
  connect?: Maybe<GqlReleaseThrottleWhereUniqueInput>;
};

export type GqlReleaseThrottleCreateOrConnectWithoutSubscriptionInput = {
  where: GqlReleaseThrottleWhereUniqueInput;
  create: GqlReleaseThrottleCreateWithoutSubscriptionInput;
};

export type GqlReleaseThrottleCreateWithoutSubscriptionInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  take?: Maybe<Scalars['Int']>;
  window?: Maybe<Scalars['String']>;
  scoreCriteria?: Maybe<Scalars['String']>;
  nextReleaseAt?: Maybe<Scalars['DateTime']>;
};

export type GqlReleaseThrottleGroupBy = {
  __typename?: 'ReleaseThrottleGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  take: FieldWrapper<Scalars['Int']>;
  window: FieldWrapper<Scalars['String']>;
  scoreCriteria?: Maybe<FieldWrapper<Scalars['String']>>;
  nextReleaseAt: FieldWrapper<Scalars['DateTime']>;
  _count?: Maybe<FieldWrapper<GqlReleaseThrottleCountAggregate>>;
  _avg?: Maybe<FieldWrapper<GqlReleaseThrottleAvgAggregate>>;
  _sum?: Maybe<FieldWrapper<GqlReleaseThrottleSumAggregate>>;
  _min?: Maybe<FieldWrapper<GqlReleaseThrottleMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlReleaseThrottleMaxAggregate>>;
};

export type GqlReleaseThrottleMaxAggregate = {
  __typename?: 'ReleaseThrottleMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  take?: Maybe<FieldWrapper<Scalars['Int']>>;
  window?: Maybe<FieldWrapper<Scalars['String']>>;
  scoreCriteria?: Maybe<FieldWrapper<Scalars['String']>>;
  nextReleaseAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlReleaseThrottleMinAggregate = {
  __typename?: 'ReleaseThrottleMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  take?: Maybe<FieldWrapper<Scalars['Int']>>;
  window?: Maybe<FieldWrapper<Scalars['String']>>;
  scoreCriteria?: Maybe<FieldWrapper<Scalars['String']>>;
  nextReleaseAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlReleaseThrottleOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  take?: Maybe<GqlSortOrder>;
  window?: Maybe<GqlSortOrder>;
  scoreCriteria?: Maybe<GqlSortOrder>;
  nextReleaseAt?: Maybe<GqlSortOrder>;
};

export type GqlReleaseThrottleRelationFilter = {
  is?: Maybe<GqlReleaseThrottleWhereInput>;
  isNot?: Maybe<GqlReleaseThrottleWhereInput>;
};

export enum GqlReleaseThrottleScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Take = 'take',
  Window = 'window',
  ScoreCriteria = 'scoreCriteria',
  NextReleaseAt = 'nextReleaseAt'
}

export type GqlReleaseThrottleScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlReleaseThrottleScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlReleaseThrottleScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlReleaseThrottleScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  take?: Maybe<GqlIntWithAggregatesFilter>;
  window?: Maybe<GqlStringWithAggregatesFilter>;
  scoreCriteria?: Maybe<GqlStringNullableWithAggregatesFilter>;
  nextReleaseAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
};

export type GqlReleaseThrottleSumAggregate = {
  __typename?: 'ReleaseThrottleSumAggregate';
  take?: Maybe<FieldWrapper<Scalars['Int']>>;
};

export type GqlReleaseThrottleUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  take?: Maybe<GqlIntFieldUpdateOperationsInput>;
  window?: Maybe<GqlStringFieldUpdateOperationsInput>;
  scoreCriteria?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  nextReleaseAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  Subscription?: Maybe<GqlSubscriptionUpdateManyWithoutThrottleInput>;
};

export type GqlReleaseThrottleUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  take?: Maybe<GqlIntFieldUpdateOperationsInput>;
  window?: Maybe<GqlStringFieldUpdateOperationsInput>;
  scoreCriteria?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  nextReleaseAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
};

export type GqlReleaseThrottleUpdateOneWithoutSubscriptionInput = {
  create?: Maybe<GqlReleaseThrottleCreateWithoutSubscriptionInput>;
  connectOrCreate?: Maybe<GqlReleaseThrottleCreateOrConnectWithoutSubscriptionInput>;
  upsert?: Maybe<GqlReleaseThrottleUpsertWithoutSubscriptionInput>;
  connect?: Maybe<GqlReleaseThrottleWhereUniqueInput>;
  disconnect?: Maybe<Scalars['Boolean']>;
  delete?: Maybe<Scalars['Boolean']>;
  update?: Maybe<GqlReleaseThrottleUpdateWithoutSubscriptionInput>;
};

export type GqlReleaseThrottleUpdateWithoutSubscriptionInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  take?: Maybe<GqlIntFieldUpdateOperationsInput>;
  window?: Maybe<GqlStringFieldUpdateOperationsInput>;
  scoreCriteria?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  nextReleaseAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
};

export type GqlReleaseThrottleUpsertWithoutSubscriptionInput = {
  update: GqlReleaseThrottleUpdateWithoutSubscriptionInput;
  create: GqlReleaseThrottleCreateWithoutSubscriptionInput;
};

export type GqlReleaseThrottleWhereInput = {
  AND?: Maybe<Array<GqlReleaseThrottleWhereInput>>;
  OR?: Maybe<Array<GqlReleaseThrottleWhereInput>>;
  NOT?: Maybe<Array<GqlReleaseThrottleWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  take?: Maybe<GqlIntFilter>;
  window?: Maybe<GqlStringFilter>;
  scoreCriteria?: Maybe<GqlStringNullableFilter>;
  nextReleaseAt?: Maybe<GqlDateTimeFilter>;
  Subscription?: Maybe<GqlSubscriptionListRelationFilter>;
};

export type GqlReleaseThrottleWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export enum GqlSortOrder {
  Asc = 'asc',
  Desc = 'desc'
}

export type GqlStream = {
  __typename?: 'Stream';
  id: FieldWrapper<Scalars['String']>;
  articleRefs: Array<FieldWrapper<GqlArticleRef>>;
  Feed: Array<FieldWrapper<GqlFeed>>;
  Bucket: Array<FieldWrapper<GqlBucket>>;
};


export type GqlStreamArticleRefsArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlArticleRefOrderByInput>>;
  cursor?: Maybe<GqlArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleRefScalarFieldEnum>>;
};


export type GqlStreamFeedArgs = {
  where?: Maybe<GqlFeedWhereInput>;
  orderBy?: Maybe<Array<GqlFeedOrderByInput>>;
  cursor?: Maybe<GqlFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlFeedScalarFieldEnum>>;
};


export type GqlStreamBucketArgs = {
  where?: Maybe<GqlBucketWhereInput>;
  orderBy?: Maybe<Array<GqlBucketOrderByInput>>;
  cursor?: Maybe<GqlBucketWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlBucketScalarFieldEnum>>;
};

export type GqlStreamCountAggregate = {
  __typename?: 'StreamCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlStreamCreateInput = {
  id?: Maybe<Scalars['String']>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutStreamInput>;
  Feed?: Maybe<GqlFeedCreateNestedManyWithoutStreamInput>;
  Bucket?: Maybe<GqlBucketCreateNestedManyWithoutStreamInput>;
};

export type GqlStreamCreateManyInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlStreamCreateNestedManyWithoutArticleRefsInput = {
  create?: Maybe<Array<GqlStreamCreateWithoutArticleRefsInput>>;
  connectOrCreate?: Maybe<Array<GqlStreamCreateOrConnectWithoutArticleRefsInput>>;
  connect?: Maybe<Array<GqlStreamWhereUniqueInput>>;
};

export type GqlStreamCreateNestedOneWithoutBucketInput = {
  create?: Maybe<GqlStreamCreateWithoutBucketInput>;
  connectOrCreate?: Maybe<GqlStreamCreateOrConnectWithoutBucketInput>;
  connect?: Maybe<GqlStreamWhereUniqueInput>;
};

export type GqlStreamCreateNestedOneWithoutFeedInput = {
  create?: Maybe<GqlStreamCreateWithoutFeedInput>;
  connectOrCreate?: Maybe<GqlStreamCreateOrConnectWithoutFeedInput>;
  connect?: Maybe<GqlStreamWhereUniqueInput>;
};

export type GqlStreamCreateOrConnectWithoutArticleRefsInput = {
  where: GqlStreamWhereUniqueInput;
  create: GqlStreamCreateWithoutArticleRefsInput;
};

export type GqlStreamCreateOrConnectWithoutBucketInput = {
  where: GqlStreamWhereUniqueInput;
  create: GqlStreamCreateWithoutBucketInput;
};

export type GqlStreamCreateOrConnectWithoutFeedInput = {
  where: GqlStreamWhereUniqueInput;
  create: GqlStreamCreateWithoutFeedInput;
};

export type GqlStreamCreateWithoutArticleRefsInput = {
  id?: Maybe<Scalars['String']>;
  Feed?: Maybe<GqlFeedCreateNestedManyWithoutStreamInput>;
  Bucket?: Maybe<GqlBucketCreateNestedManyWithoutStreamInput>;
};

export type GqlStreamCreateWithoutBucketInput = {
  id?: Maybe<Scalars['String']>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutStreamInput>;
  Feed?: Maybe<GqlFeedCreateNestedManyWithoutStreamInput>;
};

export type GqlStreamCreateWithoutFeedInput = {
  id?: Maybe<Scalars['String']>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutStreamInput>;
  Bucket?: Maybe<GqlBucketCreateNestedManyWithoutStreamInput>;
};

export type GqlStreamGroupBy = {
  __typename?: 'StreamGroupBy';
  id: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlStreamCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlStreamMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlStreamMaxAggregate>>;
};

export type GqlStreamListRelationFilter = {
  every?: Maybe<GqlStreamWhereInput>;
  some?: Maybe<GqlStreamWhereInput>;
  none?: Maybe<GqlStreamWhereInput>;
};

export type GqlStreamMaxAggregate = {
  __typename?: 'StreamMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlStreamMinAggregate = {
  __typename?: 'StreamMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlStreamOrderByInput = {
  id?: Maybe<GqlSortOrder>;
};

export type GqlStreamRelationFilter = {
  is?: Maybe<GqlStreamWhereInput>;
  isNot?: Maybe<GqlStreamWhereInput>;
};

export enum GqlStreamScalarFieldEnum {
  Id = 'id'
}

export type GqlStreamScalarWhereInput = {
  AND?: Maybe<Array<GqlStreamScalarWhereInput>>;
  OR?: Maybe<Array<GqlStreamScalarWhereInput>>;
  NOT?: Maybe<Array<GqlStreamScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
};

export type GqlStreamScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlStreamScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlStreamScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlStreamScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlStreamUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutStreamInput>;
  Feed?: Maybe<GqlFeedUpdateManyWithoutStreamInput>;
  Bucket?: Maybe<GqlBucketUpdateManyWithoutStreamInput>;
};

export type GqlStreamUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
};

export type GqlStreamUpdateManyWithWhereWithoutArticleRefsInput = {
  where: GqlStreamScalarWhereInput;
  data: GqlStreamUpdateManyMutationInput;
};

export type GqlStreamUpdateManyWithoutArticleRefsInput = {
  create?: Maybe<Array<GqlStreamCreateWithoutArticleRefsInput>>;
  connectOrCreate?: Maybe<Array<GqlStreamCreateOrConnectWithoutArticleRefsInput>>;
  upsert?: Maybe<Array<GqlStreamUpsertWithWhereUniqueWithoutArticleRefsInput>>;
  connect?: Maybe<Array<GqlStreamWhereUniqueInput>>;
  set?: Maybe<Array<GqlStreamWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlStreamWhereUniqueInput>>;
  delete?: Maybe<Array<GqlStreamWhereUniqueInput>>;
  update?: Maybe<Array<GqlStreamUpdateWithWhereUniqueWithoutArticleRefsInput>>;
  updateMany?: Maybe<Array<GqlStreamUpdateManyWithWhereWithoutArticleRefsInput>>;
  deleteMany?: Maybe<Array<GqlStreamScalarWhereInput>>;
};

export type GqlStreamUpdateOneRequiredWithoutBucketInput = {
  create?: Maybe<GqlStreamCreateWithoutBucketInput>;
  connectOrCreate?: Maybe<GqlStreamCreateOrConnectWithoutBucketInput>;
  upsert?: Maybe<GqlStreamUpsertWithoutBucketInput>;
  connect?: Maybe<GqlStreamWhereUniqueInput>;
  update?: Maybe<GqlStreamUpdateWithoutBucketInput>;
};

export type GqlStreamUpdateOneRequiredWithoutFeedInput = {
  create?: Maybe<GqlStreamCreateWithoutFeedInput>;
  connectOrCreate?: Maybe<GqlStreamCreateOrConnectWithoutFeedInput>;
  upsert?: Maybe<GqlStreamUpsertWithoutFeedInput>;
  connect?: Maybe<GqlStreamWhereUniqueInput>;
  update?: Maybe<GqlStreamUpdateWithoutFeedInput>;
};

export type GqlStreamUpdateWithWhereUniqueWithoutArticleRefsInput = {
  where: GqlStreamWhereUniqueInput;
  data: GqlStreamUpdateWithoutArticleRefsInput;
};

export type GqlStreamUpdateWithoutArticleRefsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  Feed?: Maybe<GqlFeedUpdateManyWithoutStreamInput>;
  Bucket?: Maybe<GqlBucketUpdateManyWithoutStreamInput>;
};

export type GqlStreamUpdateWithoutBucketInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutStreamInput>;
  Feed?: Maybe<GqlFeedUpdateManyWithoutStreamInput>;
};

export type GqlStreamUpdateWithoutFeedInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutStreamInput>;
  Bucket?: Maybe<GqlBucketUpdateManyWithoutStreamInput>;
};

export type GqlStreamUpsertWithWhereUniqueWithoutArticleRefsInput = {
  where: GqlStreamWhereUniqueInput;
  update: GqlStreamUpdateWithoutArticleRefsInput;
  create: GqlStreamCreateWithoutArticleRefsInput;
};

export type GqlStreamUpsertWithoutBucketInput = {
  update: GqlStreamUpdateWithoutBucketInput;
  create: GqlStreamCreateWithoutBucketInput;
};

export type GqlStreamUpsertWithoutFeedInput = {
  update: GqlStreamUpdateWithoutFeedInput;
  create: GqlStreamCreateWithoutFeedInput;
};

export type GqlStreamWhereInput = {
  AND?: Maybe<Array<GqlStreamWhereInput>>;
  OR?: Maybe<Array<GqlStreamWhereInput>>;
  NOT?: Maybe<Array<GqlStreamWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  articleRefs?: Maybe<GqlArticleRefListRelationFilter>;
  Feed?: Maybe<GqlFeedListRelationFilter>;
  Bucket?: Maybe<GqlBucketListRelationFilter>;
};

export type GqlStreamWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlStringFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['String']>;
};

export type GqlStringFilter = {
  equals?: Maybe<Scalars['String']>;
  in?: Maybe<Array<Scalars['String']>>;
  notIn?: Maybe<Array<Scalars['String']>>;
  lt?: Maybe<Scalars['String']>;
  lte?: Maybe<Scalars['String']>;
  gt?: Maybe<Scalars['String']>;
  gte?: Maybe<Scalars['String']>;
  contains?: Maybe<Scalars['String']>;
  startsWith?: Maybe<Scalars['String']>;
  endsWith?: Maybe<Scalars['String']>;
  not?: Maybe<GqlNestedStringFilter>;
};

export type GqlStringNullableFilter = {
  equals?: Maybe<Scalars['String']>;
  in?: Maybe<Array<Scalars['String']>>;
  notIn?: Maybe<Array<Scalars['String']>>;
  lt?: Maybe<Scalars['String']>;
  lte?: Maybe<Scalars['String']>;
  gt?: Maybe<Scalars['String']>;
  gte?: Maybe<Scalars['String']>;
  contains?: Maybe<Scalars['String']>;
  startsWith?: Maybe<Scalars['String']>;
  endsWith?: Maybe<Scalars['String']>;
  not?: Maybe<GqlNestedStringNullableFilter>;
};

export type GqlStringNullableWithAggregatesFilter = {
  equals?: Maybe<Scalars['String']>;
  in?: Maybe<Array<Scalars['String']>>;
  notIn?: Maybe<Array<Scalars['String']>>;
  lt?: Maybe<Scalars['String']>;
  lte?: Maybe<Scalars['String']>;
  gt?: Maybe<Scalars['String']>;
  gte?: Maybe<Scalars['String']>;
  contains?: Maybe<Scalars['String']>;
  startsWith?: Maybe<Scalars['String']>;
  endsWith?: Maybe<Scalars['String']>;
  not?: Maybe<GqlNestedStringNullableWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntNullableFilter>;
  _min?: Maybe<GqlNestedStringNullableFilter>;
  _max?: Maybe<GqlNestedStringNullableFilter>;
};

export type GqlStringWithAggregatesFilter = {
  equals?: Maybe<Scalars['String']>;
  in?: Maybe<Array<Scalars['String']>>;
  notIn?: Maybe<Array<Scalars['String']>>;
  lt?: Maybe<Scalars['String']>;
  lte?: Maybe<Scalars['String']>;
  gt?: Maybe<Scalars['String']>;
  gte?: Maybe<Scalars['String']>;
  contains?: Maybe<Scalars['String']>;
  startsWith?: Maybe<Scalars['String']>;
  endsWith?: Maybe<Scalars['String']>;
  not?: Maybe<GqlNestedStringWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntFilter>;
  _min?: Maybe<GqlNestedStringFilter>;
  _max?: Maybe<GqlNestedStringFilter>;
};

export type GqlSubscription = {
  __typename?: 'Subscription';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  title: FieldWrapper<Scalars['String']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  throttleId?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId: FieldWrapper<Scalars['String']>;
  ownerId: FieldWrapper<Scalars['String']>;
  bucketId: FieldWrapper<Scalars['String']>;
  throttle?: Maybe<FieldWrapper<GqlReleaseThrottle>>;
  feed: FieldWrapper<GqlFeed>;
  owner: FieldWrapper<GqlUser>;
  bucket: FieldWrapper<GqlBucket>;
};

export type GqlSubscriptionCountAggregate = {
  __typename?: 'SubscriptionCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  description: FieldWrapper<Scalars['Int']>;
  tags: FieldWrapper<Scalars['Int']>;
  throttleId: FieldWrapper<Scalars['Int']>;
  feedId: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  bucketId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlSubscriptionCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  throttle?: Maybe<GqlReleaseThrottleCreateNestedOneWithoutSubscriptionInput>;
  feed: GqlFeedCreateNestedOneWithoutSubscriptionsInput;
  owner: GqlUserCreateNestedOneWithoutSubscriptionInput;
  bucket: GqlBucketCreateNestedOneWithoutSubscriptionsInput;
};

export type GqlSubscriptionCreateManyBucketInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  throttleId?: Maybe<Scalars['String']>;
  feedId: Scalars['String'];
  ownerId: Scalars['String'];
};

export type GqlSubscriptionCreateManyBucketInputEnvelope = {
  data: Array<GqlSubscriptionCreateManyBucketInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlSubscriptionCreateManyFeedInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  throttleId?: Maybe<Scalars['String']>;
  ownerId: Scalars['String'];
  bucketId: Scalars['String'];
};

export type GqlSubscriptionCreateManyFeedInputEnvelope = {
  data: Array<GqlSubscriptionCreateManyFeedInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlSubscriptionCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  throttleId?: Maybe<Scalars['String']>;
  feedId: Scalars['String'];
  ownerId: Scalars['String'];
  bucketId: Scalars['String'];
};

export type GqlSubscriptionCreateManyOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  throttleId?: Maybe<Scalars['String']>;
  feedId: Scalars['String'];
  bucketId: Scalars['String'];
};

export type GqlSubscriptionCreateManyOwnerInputEnvelope = {
  data: Array<GqlSubscriptionCreateManyOwnerInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlSubscriptionCreateManyThrottleInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  feedId: Scalars['String'];
  ownerId: Scalars['String'];
  bucketId: Scalars['String'];
};

export type GqlSubscriptionCreateManyThrottleInputEnvelope = {
  data: Array<GqlSubscriptionCreateManyThrottleInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlSubscriptionCreateNestedManyWithoutBucketInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutBucketInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutBucketInput>>;
  createMany?: Maybe<GqlSubscriptionCreateManyBucketInputEnvelope>;
  connect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
};

export type GqlSubscriptionCreateNestedManyWithoutFeedInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutFeedInput>>;
  createMany?: Maybe<GqlSubscriptionCreateManyFeedInputEnvelope>;
  connect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
};

export type GqlSubscriptionCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutOwnerInput>>;
  createMany?: Maybe<GqlSubscriptionCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
};

export type GqlSubscriptionCreateNestedManyWithoutThrottleInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutThrottleInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutThrottleInput>>;
  createMany?: Maybe<GqlSubscriptionCreateManyThrottleInputEnvelope>;
  connect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
};

export type GqlSubscriptionCreateOrConnectWithoutBucketInput = {
  where: GqlSubscriptionWhereUniqueInput;
  create: GqlSubscriptionCreateWithoutBucketInput;
};

export type GqlSubscriptionCreateOrConnectWithoutFeedInput = {
  where: GqlSubscriptionWhereUniqueInput;
  create: GqlSubscriptionCreateWithoutFeedInput;
};

export type GqlSubscriptionCreateOrConnectWithoutOwnerInput = {
  where: GqlSubscriptionWhereUniqueInput;
  create: GqlSubscriptionCreateWithoutOwnerInput;
};

export type GqlSubscriptionCreateOrConnectWithoutThrottleInput = {
  where: GqlSubscriptionWhereUniqueInput;
  create: GqlSubscriptionCreateWithoutThrottleInput;
};

export type GqlSubscriptionCreateWithoutBucketInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  throttle?: Maybe<GqlReleaseThrottleCreateNestedOneWithoutSubscriptionInput>;
  feed: GqlFeedCreateNestedOneWithoutSubscriptionsInput;
  owner: GqlUserCreateNestedOneWithoutSubscriptionInput;
};

export type GqlSubscriptionCreateWithoutFeedInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  throttle?: Maybe<GqlReleaseThrottleCreateNestedOneWithoutSubscriptionInput>;
  owner: GqlUserCreateNestedOneWithoutSubscriptionInput;
  bucket: GqlBucketCreateNestedOneWithoutSubscriptionsInput;
};

export type GqlSubscriptionCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  throttle?: Maybe<GqlReleaseThrottleCreateNestedOneWithoutSubscriptionInput>;
  feed: GqlFeedCreateNestedOneWithoutSubscriptionsInput;
  bucket: GqlBucketCreateNestedOneWithoutSubscriptionsInput;
};

export type GqlSubscriptionCreateWithoutThrottleInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  feed: GqlFeedCreateNestedOneWithoutSubscriptionsInput;
  owner: GqlUserCreateNestedOneWithoutSubscriptionInput;
  bucket: GqlBucketCreateNestedOneWithoutSubscriptionsInput;
};

export type GqlSubscriptionGroupBy = {
  __typename?: 'SubscriptionGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  title: FieldWrapper<Scalars['String']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  throttleId?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId: FieldWrapper<Scalars['String']>;
  ownerId: FieldWrapper<Scalars['String']>;
  bucketId: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlSubscriptionCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlSubscriptionMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlSubscriptionMaxAggregate>>;
};

export type GqlSubscriptionListRelationFilter = {
  every?: Maybe<GqlSubscriptionWhereInput>;
  some?: Maybe<GqlSubscriptionWhereInput>;
  none?: Maybe<GqlSubscriptionWhereInput>;
};

export type GqlSubscriptionMaxAggregate = {
  __typename?: 'SubscriptionMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  throttleId?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlSubscriptionMinAggregate = {
  __typename?: 'SubscriptionMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  throttleId?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlSubscriptionOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  description?: Maybe<GqlSortOrder>;
  tags?: Maybe<GqlSortOrder>;
  throttleId?: Maybe<GqlSortOrder>;
  feedId?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  bucketId?: Maybe<GqlSortOrder>;
};

export enum GqlSubscriptionScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Title = 'title',
  Description = 'description',
  Tags = 'tags',
  ThrottleId = 'throttleId',
  FeedId = 'feedId',
  OwnerId = 'ownerId',
  BucketId = 'bucketId'
}

export type GqlSubscriptionScalarWhereInput = {
  AND?: Maybe<Array<GqlSubscriptionScalarWhereInput>>;
  OR?: Maybe<Array<GqlSubscriptionScalarWhereInput>>;
  NOT?: Maybe<Array<GqlSubscriptionScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  title?: Maybe<GqlStringFilter>;
  description?: Maybe<GqlStringNullableFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  throttleId?: Maybe<GqlStringNullableFilter>;
  feedId?: Maybe<GqlStringFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  bucketId?: Maybe<GqlStringFilter>;
};

export type GqlSubscriptionScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlSubscriptionScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlSubscriptionScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlSubscriptionScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  title?: Maybe<GqlStringWithAggregatesFilter>;
  description?: Maybe<GqlStringNullableWithAggregatesFilter>;
  tags?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  throttleId?: Maybe<GqlStringNullableWithAggregatesFilter>;
  feedId?: Maybe<GqlStringWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
  bucketId?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlSubscriptionUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  throttle?: Maybe<GqlReleaseThrottleUpdateOneWithoutSubscriptionInput>;
  feed?: Maybe<GqlFeedUpdateOneRequiredWithoutSubscriptionsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutSubscriptionInput>;
  bucket?: Maybe<GqlBucketUpdateOneRequiredWithoutSubscriptionsInput>;
};

export type GqlSubscriptionUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
};

export type GqlSubscriptionUpdateManyWithWhereWithoutBucketInput = {
  where: GqlSubscriptionScalarWhereInput;
  data: GqlSubscriptionUpdateManyMutationInput;
};

export type GqlSubscriptionUpdateManyWithWhereWithoutFeedInput = {
  where: GqlSubscriptionScalarWhereInput;
  data: GqlSubscriptionUpdateManyMutationInput;
};

export type GqlSubscriptionUpdateManyWithWhereWithoutOwnerInput = {
  where: GqlSubscriptionScalarWhereInput;
  data: GqlSubscriptionUpdateManyMutationInput;
};

export type GqlSubscriptionUpdateManyWithWhereWithoutThrottleInput = {
  where: GqlSubscriptionScalarWhereInput;
  data: GqlSubscriptionUpdateManyMutationInput;
};

export type GqlSubscriptionUpdateManyWithoutBucketInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutBucketInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutBucketInput>>;
  upsert?: Maybe<Array<GqlSubscriptionUpsertWithWhereUniqueWithoutBucketInput>>;
  createMany?: Maybe<GqlSubscriptionCreateManyBucketInputEnvelope>;
  connect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  set?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  delete?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  update?: Maybe<Array<GqlSubscriptionUpdateWithWhereUniqueWithoutBucketInput>>;
  updateMany?: Maybe<Array<GqlSubscriptionUpdateManyWithWhereWithoutBucketInput>>;
  deleteMany?: Maybe<Array<GqlSubscriptionScalarWhereInput>>;
};

export type GqlSubscriptionUpdateManyWithoutFeedInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutFeedInput>>;
  upsert?: Maybe<Array<GqlSubscriptionUpsertWithWhereUniqueWithoutFeedInput>>;
  createMany?: Maybe<GqlSubscriptionCreateManyFeedInputEnvelope>;
  connect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  set?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  delete?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  update?: Maybe<Array<GqlSubscriptionUpdateWithWhereUniqueWithoutFeedInput>>;
  updateMany?: Maybe<Array<GqlSubscriptionUpdateManyWithWhereWithoutFeedInput>>;
  deleteMany?: Maybe<Array<GqlSubscriptionScalarWhereInput>>;
};

export type GqlSubscriptionUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<GqlSubscriptionUpsertWithWhereUniqueWithoutOwnerInput>>;
  createMany?: Maybe<GqlSubscriptionCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  set?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  delete?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  update?: Maybe<Array<GqlSubscriptionUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<GqlSubscriptionUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<GqlSubscriptionScalarWhereInput>>;
};

export type GqlSubscriptionUpdateManyWithoutThrottleInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutThrottleInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutThrottleInput>>;
  upsert?: Maybe<Array<GqlSubscriptionUpsertWithWhereUniqueWithoutThrottleInput>>;
  createMany?: Maybe<GqlSubscriptionCreateManyThrottleInputEnvelope>;
  connect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  set?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  delete?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  update?: Maybe<Array<GqlSubscriptionUpdateWithWhereUniqueWithoutThrottleInput>>;
  updateMany?: Maybe<Array<GqlSubscriptionUpdateManyWithWhereWithoutThrottleInput>>;
  deleteMany?: Maybe<Array<GqlSubscriptionScalarWhereInput>>;
};

export type GqlSubscriptionUpdateWithWhereUniqueWithoutBucketInput = {
  where: GqlSubscriptionWhereUniqueInput;
  data: GqlSubscriptionUpdateWithoutBucketInput;
};

export type GqlSubscriptionUpdateWithWhereUniqueWithoutFeedInput = {
  where: GqlSubscriptionWhereUniqueInput;
  data: GqlSubscriptionUpdateWithoutFeedInput;
};

export type GqlSubscriptionUpdateWithWhereUniqueWithoutOwnerInput = {
  where: GqlSubscriptionWhereUniqueInput;
  data: GqlSubscriptionUpdateWithoutOwnerInput;
};

export type GqlSubscriptionUpdateWithWhereUniqueWithoutThrottleInput = {
  where: GqlSubscriptionWhereUniqueInput;
  data: GqlSubscriptionUpdateWithoutThrottleInput;
};

export type GqlSubscriptionUpdateWithoutBucketInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  throttle?: Maybe<GqlReleaseThrottleUpdateOneWithoutSubscriptionInput>;
  feed?: Maybe<GqlFeedUpdateOneRequiredWithoutSubscriptionsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutSubscriptionInput>;
};

export type GqlSubscriptionUpdateWithoutFeedInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  throttle?: Maybe<GqlReleaseThrottleUpdateOneWithoutSubscriptionInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutSubscriptionInput>;
  bucket?: Maybe<GqlBucketUpdateOneRequiredWithoutSubscriptionsInput>;
};

export type GqlSubscriptionUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  throttle?: Maybe<GqlReleaseThrottleUpdateOneWithoutSubscriptionInput>;
  feed?: Maybe<GqlFeedUpdateOneRequiredWithoutSubscriptionsInput>;
  bucket?: Maybe<GqlBucketUpdateOneRequiredWithoutSubscriptionsInput>;
};

export type GqlSubscriptionUpdateWithoutThrottleInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  feed?: Maybe<GqlFeedUpdateOneRequiredWithoutSubscriptionsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutSubscriptionInput>;
  bucket?: Maybe<GqlBucketUpdateOneRequiredWithoutSubscriptionsInput>;
};

export type GqlSubscriptionUpsertWithWhereUniqueWithoutBucketInput = {
  where: GqlSubscriptionWhereUniqueInput;
  update: GqlSubscriptionUpdateWithoutBucketInput;
  create: GqlSubscriptionCreateWithoutBucketInput;
};

export type GqlSubscriptionUpsertWithWhereUniqueWithoutFeedInput = {
  where: GqlSubscriptionWhereUniqueInput;
  update: GqlSubscriptionUpdateWithoutFeedInput;
  create: GqlSubscriptionCreateWithoutFeedInput;
};

export type GqlSubscriptionUpsertWithWhereUniqueWithoutOwnerInput = {
  where: GqlSubscriptionWhereUniqueInput;
  update: GqlSubscriptionUpdateWithoutOwnerInput;
  create: GqlSubscriptionCreateWithoutOwnerInput;
};

export type GqlSubscriptionUpsertWithWhereUniqueWithoutThrottleInput = {
  where: GqlSubscriptionWhereUniqueInput;
  update: GqlSubscriptionUpdateWithoutThrottleInput;
  create: GqlSubscriptionCreateWithoutThrottleInput;
};

export type GqlSubscriptionWhereInput = {
  AND?: Maybe<Array<GqlSubscriptionWhereInput>>;
  OR?: Maybe<Array<GqlSubscriptionWhereInput>>;
  NOT?: Maybe<Array<GqlSubscriptionWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  title?: Maybe<GqlStringFilter>;
  description?: Maybe<GqlStringNullableFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  throttle?: Maybe<GqlReleaseThrottleRelationFilter>;
  throttleId?: Maybe<GqlStringNullableFilter>;
  feed?: Maybe<GqlFeedRelationFilter>;
  feedId?: Maybe<GqlStringFilter>;
  owner?: Maybe<GqlUserRelationFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  bucket?: Maybe<GqlBucketRelationFilter>;
  bucketId?: Maybe<GqlStringFilter>;
};

export type GqlSubscriptionWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlUser = {
  __typename?: 'User';
  id: FieldWrapper<Scalars['String']>;
  email: FieldWrapper<Scalars['String']>;
  name: FieldWrapper<Scalars['String']>;
  buckets: Array<FieldWrapper<GqlBucket>>;
  articleRefs: Array<FieldWrapper<GqlArticleRef>>;
  feeds: Array<FieldWrapper<GqlUserFeed>>;
  subscription: Array<FieldWrapper<GqlSubscription>>;
};


export type GqlUserBucketsArgs = {
  where?: Maybe<GqlBucketWhereInput>;
  orderBy?: Maybe<Array<GqlBucketOrderByInput>>;
  cursor?: Maybe<GqlBucketWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlBucketScalarFieldEnum>>;
};


export type GqlUserArticleRefsArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlArticleRefOrderByInput>>;
  cursor?: Maybe<GqlArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleRefScalarFieldEnum>>;
};


export type GqlUserFeedsArgs = {
  where?: Maybe<GqlUserFeedWhereInput>;
  orderBy?: Maybe<Array<GqlUserFeedOrderByInput>>;
  cursor?: Maybe<GqlUserFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlUserFeedScalarFieldEnum>>;
};


export type GqlUserSubscriptionArgs = {
  where?: Maybe<GqlSubscriptionWhereInput>;
  orderBy?: Maybe<Array<GqlSubscriptionOrderByInput>>;
  cursor?: Maybe<GqlSubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlSubscriptionScalarFieldEnum>>;
};

export type GqlUserArticle = {
  __typename?: 'UserArticle';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  harvested: FieldWrapper<Scalars['Boolean']>;
  harvestUrl?: Maybe<FieldWrapper<Scalars['String']>>;
  source: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  content: FieldWrapper<Scalars['String']>;
  contentHtml: FieldWrapper<Scalars['String']>;
  userFeedId: FieldWrapper<Scalars['String']>;
  userFeed: FieldWrapper<GqlUserFeed>;
};

export type GqlUserArticleCountAggregate = {
  __typename?: 'UserArticleCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  harvested: FieldWrapper<Scalars['Int']>;
  harvestUrl: FieldWrapper<Scalars['Int']>;
  source: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  content: FieldWrapper<Scalars['Int']>;
  contentHtml: FieldWrapper<Scalars['Int']>;
  userFeedId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlUserArticleCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  harvested?: Maybe<Scalars['Boolean']>;
  harvestUrl?: Maybe<Scalars['String']>;
  source: Scalars['String'];
  title: Scalars['String'];
  content: Scalars['String'];
  contentHtml: Scalars['String'];
  userFeed: GqlUserFeedCreateNestedOneWithoutArticlesInput;
};

export type GqlUserArticleCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  harvested?: Maybe<Scalars['Boolean']>;
  harvestUrl?: Maybe<Scalars['String']>;
  source: Scalars['String'];
  title: Scalars['String'];
  content: Scalars['String'];
  contentHtml: Scalars['String'];
  userFeedId: Scalars['String'];
};

export type GqlUserArticleCreateManyUserFeedInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  harvested?: Maybe<Scalars['Boolean']>;
  harvestUrl?: Maybe<Scalars['String']>;
  source: Scalars['String'];
  title: Scalars['String'];
  content: Scalars['String'];
  contentHtml: Scalars['String'];
};

export type GqlUserArticleCreateManyUserFeedInputEnvelope = {
  data: Array<GqlUserArticleCreateManyUserFeedInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlUserArticleCreateNestedManyWithoutUserFeedInput = {
  create?: Maybe<Array<GqlUserArticleCreateWithoutUserFeedInput>>;
  connectOrCreate?: Maybe<Array<GqlUserArticleCreateOrConnectWithoutUserFeedInput>>;
  createMany?: Maybe<GqlUserArticleCreateManyUserFeedInputEnvelope>;
  connect?: Maybe<Array<GqlUserArticleWhereUniqueInput>>;
};

export type GqlUserArticleCreateOrConnectWithoutUserFeedInput = {
  where: GqlUserArticleWhereUniqueInput;
  create: GqlUserArticleCreateWithoutUserFeedInput;
};

export type GqlUserArticleCreateWithoutUserFeedInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  harvested?: Maybe<Scalars['Boolean']>;
  harvestUrl?: Maybe<Scalars['String']>;
  source: Scalars['String'];
  title: Scalars['String'];
  content: Scalars['String'];
  contentHtml: Scalars['String'];
};

export type GqlUserArticleGroupBy = {
  __typename?: 'UserArticleGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  harvested: FieldWrapper<Scalars['Boolean']>;
  harvestUrl?: Maybe<FieldWrapper<Scalars['String']>>;
  source: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  content: FieldWrapper<Scalars['String']>;
  contentHtml: FieldWrapper<Scalars['String']>;
  userFeedId: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlUserArticleCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlUserArticleMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlUserArticleMaxAggregate>>;
};

export type GqlUserArticleListRelationFilter = {
  every?: Maybe<GqlUserArticleWhereInput>;
  some?: Maybe<GqlUserArticleWhereInput>;
  none?: Maybe<GqlUserArticleWhereInput>;
};

export type GqlUserArticleMaxAggregate = {
  __typename?: 'UserArticleMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  harvested?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  harvestUrl?: Maybe<FieldWrapper<Scalars['String']>>;
  source?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  content?: Maybe<FieldWrapper<Scalars['String']>>;
  contentHtml?: Maybe<FieldWrapper<Scalars['String']>>;
  userFeedId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlUserArticleMinAggregate = {
  __typename?: 'UserArticleMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  harvested?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  harvestUrl?: Maybe<FieldWrapper<Scalars['String']>>;
  source?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  content?: Maybe<FieldWrapper<Scalars['String']>>;
  contentHtml?: Maybe<FieldWrapper<Scalars['String']>>;
  userFeedId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlUserArticleOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  harvested?: Maybe<GqlSortOrder>;
  harvestUrl?: Maybe<GqlSortOrder>;
  source?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  content?: Maybe<GqlSortOrder>;
  contentHtml?: Maybe<GqlSortOrder>;
  userFeedId?: Maybe<GqlSortOrder>;
};

export enum GqlUserArticleScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Harvested = 'harvested',
  HarvestUrl = 'harvestUrl',
  Source = 'source',
  Title = 'title',
  Content = 'content',
  ContentHtml = 'contentHtml',
  UserFeedId = 'userFeedId'
}

export type GqlUserArticleScalarWhereInput = {
  AND?: Maybe<Array<GqlUserArticleScalarWhereInput>>;
  OR?: Maybe<Array<GqlUserArticleScalarWhereInput>>;
  NOT?: Maybe<Array<GqlUserArticleScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  harvested?: Maybe<GqlBoolFilter>;
  harvestUrl?: Maybe<GqlStringNullableFilter>;
  source?: Maybe<GqlStringFilter>;
  title?: Maybe<GqlStringFilter>;
  content?: Maybe<GqlStringFilter>;
  contentHtml?: Maybe<GqlStringFilter>;
  userFeedId?: Maybe<GqlStringFilter>;
};

export type GqlUserArticleScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlUserArticleScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlUserArticleScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlUserArticleScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  harvested?: Maybe<GqlBoolWithAggregatesFilter>;
  harvestUrl?: Maybe<GqlStringNullableWithAggregatesFilter>;
  source?: Maybe<GqlStringWithAggregatesFilter>;
  title?: Maybe<GqlStringWithAggregatesFilter>;
  content?: Maybe<GqlStringWithAggregatesFilter>;
  contentHtml?: Maybe<GqlStringWithAggregatesFilter>;
  userFeedId?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlUserArticleUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  harvested?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  harvestUrl?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  source?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content?: Maybe<GqlStringFieldUpdateOperationsInput>;
  contentHtml?: Maybe<GqlStringFieldUpdateOperationsInput>;
  userFeed?: Maybe<GqlUserFeedUpdateOneRequiredWithoutArticlesInput>;
};

export type GqlUserArticleUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  harvested?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  harvestUrl?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  source?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content?: Maybe<GqlStringFieldUpdateOperationsInput>;
  contentHtml?: Maybe<GqlStringFieldUpdateOperationsInput>;
};

export type GqlUserArticleUpdateManyWithWhereWithoutUserFeedInput = {
  where: GqlUserArticleScalarWhereInput;
  data: GqlUserArticleUpdateManyMutationInput;
};

export type GqlUserArticleUpdateManyWithoutUserFeedInput = {
  create?: Maybe<Array<GqlUserArticleCreateWithoutUserFeedInput>>;
  connectOrCreate?: Maybe<Array<GqlUserArticleCreateOrConnectWithoutUserFeedInput>>;
  upsert?: Maybe<Array<GqlUserArticleUpsertWithWhereUniqueWithoutUserFeedInput>>;
  createMany?: Maybe<GqlUserArticleCreateManyUserFeedInputEnvelope>;
  connect?: Maybe<Array<GqlUserArticleWhereUniqueInput>>;
  set?: Maybe<Array<GqlUserArticleWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlUserArticleWhereUniqueInput>>;
  delete?: Maybe<Array<GqlUserArticleWhereUniqueInput>>;
  update?: Maybe<Array<GqlUserArticleUpdateWithWhereUniqueWithoutUserFeedInput>>;
  updateMany?: Maybe<Array<GqlUserArticleUpdateManyWithWhereWithoutUserFeedInput>>;
  deleteMany?: Maybe<Array<GqlUserArticleScalarWhereInput>>;
};

export type GqlUserArticleUpdateWithWhereUniqueWithoutUserFeedInput = {
  where: GqlUserArticleWhereUniqueInput;
  data: GqlUserArticleUpdateWithoutUserFeedInput;
};

export type GqlUserArticleUpdateWithoutUserFeedInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  harvested?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  harvestUrl?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  source?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content?: Maybe<GqlStringFieldUpdateOperationsInput>;
  contentHtml?: Maybe<GqlStringFieldUpdateOperationsInput>;
};

export type GqlUserArticleUpsertWithWhereUniqueWithoutUserFeedInput = {
  where: GqlUserArticleWhereUniqueInput;
  update: GqlUserArticleUpdateWithoutUserFeedInput;
  create: GqlUserArticleCreateWithoutUserFeedInput;
};

export type GqlUserArticleWhereInput = {
  AND?: Maybe<Array<GqlUserArticleWhereInput>>;
  OR?: Maybe<Array<GqlUserArticleWhereInput>>;
  NOT?: Maybe<Array<GqlUserArticleWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  harvested?: Maybe<GqlBoolFilter>;
  harvestUrl?: Maybe<GqlStringNullableFilter>;
  source?: Maybe<GqlStringFilter>;
  title?: Maybe<GqlStringFilter>;
  content?: Maybe<GqlStringFilter>;
  contentHtml?: Maybe<GqlStringFilter>;
  userFeed?: Maybe<GqlUserFeedRelationFilter>;
  userFeedId?: Maybe<GqlStringFilter>;
};

export type GqlUserArticleWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlUserCountAggregate = {
  __typename?: 'UserCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  email: FieldWrapper<Scalars['Int']>;
  name: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlUserCreateInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  feeds?: Maybe<GqlUserFeedCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
};

export type GqlUserCreateNestedOneWithoutArticleRefsInput = {
  create?: Maybe<GqlUserCreateWithoutArticleRefsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutArticleRefsInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
};

export type GqlUserCreateNestedOneWithoutBucketsInput = {
  create?: Maybe<GqlUserCreateWithoutBucketsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutBucketsInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
};

export type GqlUserCreateNestedOneWithoutFeedsInput = {
  create?: Maybe<GqlUserCreateWithoutFeedsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutFeedsInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
};

export type GqlUserCreateNestedOneWithoutSubscriptionInput = {
  create?: Maybe<GqlUserCreateWithoutSubscriptionInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutSubscriptionInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
};

export type GqlUserCreateOrConnectWithoutArticleRefsInput = {
  where: GqlUserWhereUniqueInput;
  create: GqlUserCreateWithoutArticleRefsInput;
};

export type GqlUserCreateOrConnectWithoutBucketsInput = {
  where: GqlUserWhereUniqueInput;
  create: GqlUserCreateWithoutBucketsInput;
};

export type GqlUserCreateOrConnectWithoutFeedsInput = {
  where: GqlUserWhereUniqueInput;
  create: GqlUserCreateWithoutFeedsInput;
};

export type GqlUserCreateOrConnectWithoutSubscriptionInput = {
  where: GqlUserWhereUniqueInput;
  create: GqlUserCreateWithoutSubscriptionInput;
};

export type GqlUserCreateWithoutArticleRefsInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutOwnerInput>;
  feeds?: Maybe<GqlUserFeedCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateWithoutBucketsInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  feeds?: Maybe<GqlUserFeedCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateWithoutFeedsInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateWithoutSubscriptionInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  feeds?: Maybe<GqlUserFeedCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserFeed = {
  __typename?: 'UserFeed';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  exposed: FieldWrapper<Scalars['Boolean']>;
  title: FieldWrapper<Scalars['String']>;
  feedType: FieldWrapper<Scalars['String']>;
  description: FieldWrapper<Scalars['String']>;
  owner: FieldWrapper<GqlUser>;
  articles: Array<FieldWrapper<GqlUserArticle>>;
};


export type GqlUserFeedArticlesArgs = {
  where?: Maybe<GqlUserArticleWhereInput>;
  orderBy?: Maybe<Array<GqlUserArticleOrderByInput>>;
  cursor?: Maybe<GqlUserArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlUserArticleScalarFieldEnum>>;
};

export type GqlUserFeedCountAggregate = {
  __typename?: 'UserFeedCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  exposed: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  feedType: FieldWrapper<Scalars['Int']>;
  description: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlUserFeedCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  exposed: Scalars['Boolean'];
  title: Scalars['String'];
  feedType: Scalars['String'];
  description: Scalars['String'];
  owner: GqlUserCreateNestedOneWithoutFeedsInput;
  articles?: Maybe<GqlUserArticleCreateNestedManyWithoutUserFeedInput>;
};

export type GqlUserFeedCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  ownerId: Scalars['String'];
  exposed: Scalars['Boolean'];
  title: Scalars['String'];
  feedType: Scalars['String'];
  description: Scalars['String'];
};

export type GqlUserFeedCreateManyOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  exposed: Scalars['Boolean'];
  title: Scalars['String'];
  feedType: Scalars['String'];
  description: Scalars['String'];
};

export type GqlUserFeedCreateManyOwnerInputEnvelope = {
  data: Array<GqlUserFeedCreateManyOwnerInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlUserFeedCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlUserFeedCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlUserFeedCreateOrConnectWithoutOwnerInput>>;
  createMany?: Maybe<GqlUserFeedCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlUserFeedWhereUniqueInput>>;
};

export type GqlUserFeedCreateNestedOneWithoutArticlesInput = {
  create?: Maybe<GqlUserFeedCreateWithoutArticlesInput>;
  connectOrCreate?: Maybe<GqlUserFeedCreateOrConnectWithoutArticlesInput>;
  connect?: Maybe<GqlUserFeedWhereUniqueInput>;
};

export type GqlUserFeedCreateOrConnectWithoutArticlesInput = {
  where: GqlUserFeedWhereUniqueInput;
  create: GqlUserFeedCreateWithoutArticlesInput;
};

export type GqlUserFeedCreateOrConnectWithoutOwnerInput = {
  where: GqlUserFeedWhereUniqueInput;
  create: GqlUserFeedCreateWithoutOwnerInput;
};

export type GqlUserFeedCreateWithoutArticlesInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  exposed: Scalars['Boolean'];
  title: Scalars['String'];
  feedType: Scalars['String'];
  description: Scalars['String'];
  owner: GqlUserCreateNestedOneWithoutFeedsInput;
};

export type GqlUserFeedCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  exposed: Scalars['Boolean'];
  title: Scalars['String'];
  feedType: Scalars['String'];
  description: Scalars['String'];
  articles?: Maybe<GqlUserArticleCreateNestedManyWithoutUserFeedInput>;
};

export type GqlUserFeedGroupBy = {
  __typename?: 'UserFeedGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  exposed: FieldWrapper<Scalars['Boolean']>;
  title: FieldWrapper<Scalars['String']>;
  feedType: FieldWrapper<Scalars['String']>;
  description: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlUserFeedCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlUserFeedMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlUserFeedMaxAggregate>>;
};

export type GqlUserFeedListRelationFilter = {
  every?: Maybe<GqlUserFeedWhereInput>;
  some?: Maybe<GqlUserFeedWhereInput>;
  none?: Maybe<GqlUserFeedWhereInput>;
};

export type GqlUserFeedMaxAggregate = {
  __typename?: 'UserFeedMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  exposed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  feedType?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlUserFeedMinAggregate = {
  __typename?: 'UserFeedMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  exposed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  feedType?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlUserFeedOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  exposed?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  feedType?: Maybe<GqlSortOrder>;
  description?: Maybe<GqlSortOrder>;
};

export type GqlUserFeedRelationFilter = {
  is?: Maybe<GqlUserFeedWhereInput>;
  isNot?: Maybe<GqlUserFeedWhereInput>;
};

export enum GqlUserFeedScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  OwnerId = 'ownerId',
  Exposed = 'exposed',
  Title = 'title',
  FeedType = 'feedType',
  Description = 'description'
}

export type GqlUserFeedScalarWhereInput = {
  AND?: Maybe<Array<GqlUserFeedScalarWhereInput>>;
  OR?: Maybe<Array<GqlUserFeedScalarWhereInput>>;
  NOT?: Maybe<Array<GqlUserFeedScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  exposed?: Maybe<GqlBoolFilter>;
  title?: Maybe<GqlStringFilter>;
  feedType?: Maybe<GqlStringFilter>;
  description?: Maybe<GqlStringFilter>;
};

export type GqlUserFeedScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlUserFeedScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlUserFeedScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlUserFeedScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
  exposed?: Maybe<GqlBoolWithAggregatesFilter>;
  title?: Maybe<GqlStringWithAggregatesFilter>;
  feedType?: Maybe<GqlStringWithAggregatesFilter>;
  description?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlUserFeedUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  exposed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feedType?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutFeedsInput>;
  articles?: Maybe<GqlUserArticleUpdateManyWithoutUserFeedInput>;
};

export type GqlUserFeedUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  exposed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feedType?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
};

export type GqlUserFeedUpdateManyWithWhereWithoutOwnerInput = {
  where: GqlUserFeedScalarWhereInput;
  data: GqlUserFeedUpdateManyMutationInput;
};

export type GqlUserFeedUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlUserFeedCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlUserFeedCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<GqlUserFeedUpsertWithWhereUniqueWithoutOwnerInput>>;
  createMany?: Maybe<GqlUserFeedCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlUserFeedWhereUniqueInput>>;
  set?: Maybe<Array<GqlUserFeedWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlUserFeedWhereUniqueInput>>;
  delete?: Maybe<Array<GqlUserFeedWhereUniqueInput>>;
  update?: Maybe<Array<GqlUserFeedUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<GqlUserFeedUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<GqlUserFeedScalarWhereInput>>;
};

export type GqlUserFeedUpdateOneRequiredWithoutArticlesInput = {
  create?: Maybe<GqlUserFeedCreateWithoutArticlesInput>;
  connectOrCreate?: Maybe<GqlUserFeedCreateOrConnectWithoutArticlesInput>;
  upsert?: Maybe<GqlUserFeedUpsertWithoutArticlesInput>;
  connect?: Maybe<GqlUserFeedWhereUniqueInput>;
  update?: Maybe<GqlUserFeedUpdateWithoutArticlesInput>;
};

export type GqlUserFeedUpdateWithWhereUniqueWithoutOwnerInput = {
  where: GqlUserFeedWhereUniqueInput;
  data: GqlUserFeedUpdateWithoutOwnerInput;
};

export type GqlUserFeedUpdateWithoutArticlesInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  exposed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feedType?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutFeedsInput>;
};

export type GqlUserFeedUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  exposed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feedType?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
  articles?: Maybe<GqlUserArticleUpdateManyWithoutUserFeedInput>;
};

export type GqlUserFeedUpsertWithWhereUniqueWithoutOwnerInput = {
  where: GqlUserFeedWhereUniqueInput;
  update: GqlUserFeedUpdateWithoutOwnerInput;
  create: GqlUserFeedCreateWithoutOwnerInput;
};

export type GqlUserFeedUpsertWithoutArticlesInput = {
  update: GqlUserFeedUpdateWithoutArticlesInput;
  create: GqlUserFeedCreateWithoutArticlesInput;
};

export type GqlUserFeedWhereInput = {
  AND?: Maybe<Array<GqlUserFeedWhereInput>>;
  OR?: Maybe<Array<GqlUserFeedWhereInput>>;
  NOT?: Maybe<Array<GqlUserFeedWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  owner?: Maybe<GqlUserRelationFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  articles?: Maybe<GqlUserArticleListRelationFilter>;
  exposed?: Maybe<GqlBoolFilter>;
  title?: Maybe<GqlStringFilter>;
  feedType?: Maybe<GqlStringFilter>;
  description?: Maybe<GqlStringFilter>;
};

export type GqlUserFeedWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlUserGroupBy = {
  __typename?: 'UserGroupBy';
  id: FieldWrapper<Scalars['String']>;
  email: FieldWrapper<Scalars['String']>;
  name: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlUserCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlUserMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlUserMaxAggregate>>;
};

export type GqlUserMaxAggregate = {
  __typename?: 'UserMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  email?: Maybe<FieldWrapper<Scalars['String']>>;
  name?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlUserMinAggregate = {
  __typename?: 'UserMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  email?: Maybe<FieldWrapper<Scalars['String']>>;
  name?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlUserOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  email?: Maybe<GqlSortOrder>;
  name?: Maybe<GqlSortOrder>;
};

export type GqlUserRelationFilter = {
  is?: Maybe<GqlUserWhereInput>;
  isNot?: Maybe<GqlUserWhereInput>;
};

export enum GqlUserScalarFieldEnum {
  Id = 'id',
  Email = 'email',
  Name = 'name'
}

export type GqlUserScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlUserScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlUserScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlUserScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  email?: Maybe<GqlStringWithAggregatesFilter>;
  name?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlUserUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  feeds?: Maybe<GqlUserFeedUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
};

export type GqlUserUpdateOneRequiredWithoutArticleRefsInput = {
  create?: Maybe<GqlUserCreateWithoutArticleRefsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutArticleRefsInput>;
  upsert?: Maybe<GqlUserUpsertWithoutArticleRefsInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
  update?: Maybe<GqlUserUpdateWithoutArticleRefsInput>;
};

export type GqlUserUpdateOneRequiredWithoutBucketsInput = {
  create?: Maybe<GqlUserCreateWithoutBucketsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutBucketsInput>;
  upsert?: Maybe<GqlUserUpsertWithoutBucketsInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
  update?: Maybe<GqlUserUpdateWithoutBucketsInput>;
};

export type GqlUserUpdateOneRequiredWithoutFeedsInput = {
  create?: Maybe<GqlUserCreateWithoutFeedsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutFeedsInput>;
  upsert?: Maybe<GqlUserUpsertWithoutFeedsInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
  update?: Maybe<GqlUserUpdateWithoutFeedsInput>;
};

export type GqlUserUpdateOneRequiredWithoutSubscriptionInput = {
  create?: Maybe<GqlUserCreateWithoutSubscriptionInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutSubscriptionInput>;
  upsert?: Maybe<GqlUserUpsertWithoutSubscriptionInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
  update?: Maybe<GqlUserUpdateWithoutSubscriptionInput>;
};

export type GqlUserUpdateWithoutArticleRefsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  feeds?: Maybe<GqlUserFeedUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpdateWithoutBucketsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  feeds?: Maybe<GqlUserFeedUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpdateWithoutFeedsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpdateWithoutSubscriptionInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  feeds?: Maybe<GqlUserFeedUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpsertWithoutArticleRefsInput = {
  update: GqlUserUpdateWithoutArticleRefsInput;
  create: GqlUserCreateWithoutArticleRefsInput;
};

export type GqlUserUpsertWithoutBucketsInput = {
  update: GqlUserUpdateWithoutBucketsInput;
  create: GqlUserCreateWithoutBucketsInput;
};

export type GqlUserUpsertWithoutFeedsInput = {
  update: GqlUserUpdateWithoutFeedsInput;
  create: GqlUserCreateWithoutFeedsInput;
};

export type GqlUserUpsertWithoutSubscriptionInput = {
  update: GqlUserUpdateWithoutSubscriptionInput;
  create: GqlUserCreateWithoutSubscriptionInput;
};

export type GqlUserWhereInput = {
  AND?: Maybe<Array<GqlUserWhereInput>>;
  OR?: Maybe<Array<GqlUserWhereInput>>;
  NOT?: Maybe<Array<GqlUserWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  email?: Maybe<GqlStringFilter>;
  name?: Maybe<GqlStringFilter>;
  buckets?: Maybe<GqlBucketListRelationFilter>;
  articleRefs?: Maybe<GqlArticleRefListRelationFilter>;
  feeds?: Maybe<GqlUserFeedListRelationFilter>;
  subscription?: Maybe<GqlSubscriptionListRelationFilter>;
};

export type GqlUserWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
  email?: Maybe<Scalars['String']>;
};
