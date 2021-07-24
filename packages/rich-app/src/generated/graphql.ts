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
};



export type GqlAffectedRowsOutput = {
  __typename?: 'AffectedRowsOutput';
  count: FieldWrapper<Scalars['Int']>;
};

export type GqlAggregateArticle = {
  __typename?: 'AggregateArticle';
  _count?: Maybe<FieldWrapper<GqlArticleCountAggregate>>;
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
  url: FieldWrapper<Scalars['String']>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title: FieldWrapper<Scalars['String']>;
  tags?: Maybe<FieldWrapper<Scalars['String']>>;
  content_text: FieldWrapper<Scalars['String']>;
  content_html?: Maybe<FieldWrapper<Scalars['String']>>;
  enclosure_json?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  feed?: Maybe<FieldWrapper<GqlFeed>>;
  ArticleRef: Array<FieldWrapper<GqlArticleRef>>;
};


export type GqlArticleArticleRefArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlArticleRefOrderByInput>>;
  cursor?: Maybe<GqlArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleRefScalarFieldEnum>>;
};

export type GqlArticleCountAggregate = {
  __typename?: 'ArticleCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  date_published: FieldWrapper<Scalars['Int']>;
  date_modified: FieldWrapper<Scalars['Int']>;
  url: FieldWrapper<Scalars['Int']>;
  author: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  tags: FieldWrapper<Scalars['Int']>;
  content_text: FieldWrapper<Scalars['Int']>;
  content_html: FieldWrapper<Scalars['Int']>;
  enclosure_json: FieldWrapper<Scalars['Int']>;
  feedId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlArticleCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  date_published?: Maybe<Scalars['DateTime']>;
  date_modified?: Maybe<Scalars['DateTime']>;
  url: Scalars['String'];
  author?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['String']>;
  content_text: Scalars['String'];
  content_html?: Maybe<Scalars['String']>;
  enclosure_json?: Maybe<Scalars['String']>;
  feed?: Maybe<GqlFeedCreateNestedOneWithoutArticlesInput>;
  ArticleRef?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleInput>;
};

export type GqlArticleCreateNestedManyWithoutFeedInput = {
  create?: Maybe<Array<GqlArticleCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleCreateOrConnectWithoutFeedInput>>;
  connect?: Maybe<Array<GqlArticleWhereUniqueInput>>;
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

export type GqlArticleCreateOrConnectWithoutFeedInput = {
  where: GqlArticleWhereUniqueInput;
  create: GqlArticleCreateWithoutFeedInput;
};

export type GqlArticleCreateWithoutArticleRefInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  date_published?: Maybe<Scalars['DateTime']>;
  date_modified?: Maybe<Scalars['DateTime']>;
  url: Scalars['String'];
  author?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['String']>;
  content_text: Scalars['String'];
  content_html?: Maybe<Scalars['String']>;
  enclosure_json?: Maybe<Scalars['String']>;
  feed?: Maybe<GqlFeedCreateNestedOneWithoutArticlesInput>;
};

export type GqlArticleCreateWithoutFeedInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  date_published?: Maybe<Scalars['DateTime']>;
  date_modified?: Maybe<Scalars['DateTime']>;
  url: Scalars['String'];
  author?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['String']>;
  content_text: Scalars['String'];
  content_html?: Maybe<Scalars['String']>;
  enclosure_json?: Maybe<Scalars['String']>;
  ArticleRef?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleInput>;
};

export type GqlArticleGroupBy = {
  __typename?: 'ArticleGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  date_published: FieldWrapper<Scalars['DateTime']>;
  date_modified?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  url: FieldWrapper<Scalars['String']>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title: FieldWrapper<Scalars['String']>;
  tags?: Maybe<FieldWrapper<Scalars['String']>>;
  content_text: FieldWrapper<Scalars['String']>;
  content_html?: Maybe<FieldWrapper<Scalars['String']>>;
  enclosure_json?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  _count?: Maybe<FieldWrapper<GqlArticleCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlArticleMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlArticleMaxAggregate>>;
};

export type GqlArticleListRelationFilter = {
  every?: Maybe<GqlArticleWhereInput>;
  some?: Maybe<GqlArticleWhereInput>;
  none?: Maybe<GqlArticleWhereInput>;
};

export type GqlArticleMaxAggregate = {
  __typename?: 'ArticleMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  date_published?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  date_modified?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  tags?: Maybe<FieldWrapper<Scalars['String']>>;
  content_text?: Maybe<FieldWrapper<Scalars['String']>>;
  content_html?: Maybe<FieldWrapper<Scalars['String']>>;
  enclosure_json?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleMinAggregate = {
  __typename?: 'ArticleMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  date_published?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  date_modified?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  tags?: Maybe<FieldWrapper<Scalars['String']>>;
  content_text?: Maybe<FieldWrapper<Scalars['String']>>;
  content_html?: Maybe<FieldWrapper<Scalars['String']>>;
  enclosure_json?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  date_published?: Maybe<GqlSortOrder>;
  date_modified?: Maybe<GqlSortOrder>;
  url?: Maybe<GqlSortOrder>;
  author?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  tags?: Maybe<GqlSortOrder>;
  content_text?: Maybe<GqlSortOrder>;
  content_html?: Maybe<GqlSortOrder>;
  enclosure_json?: Maybe<GqlSortOrder>;
  feedId?: Maybe<GqlSortOrder>;
};

export type GqlArticleRef = {
  __typename?: 'ArticleRef';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  favored: FieldWrapper<Scalars['Boolean']>;
  articleId: FieldWrapper<Scalars['String']>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  articleRefId?: Maybe<FieldWrapper<Scalars['String']>>;
  owner: FieldWrapper<GqlUser>;
  article: FieldWrapper<GqlArticle>;
  related: Array<FieldWrapper<GqlArticleRef>>;
  articleRef?: Maybe<FieldWrapper<GqlArticleRef>>;
};


export type GqlArticleRefRelatedArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlArticleRefOrderByInput>>;
  cursor?: Maybe<GqlArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleRefScalarFieldEnum>>;
};

export type GqlArticleRefCountAggregate = {
  __typename?: 'ArticleRefCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  favored: FieldWrapper<Scalars['Int']>;
  articleId: FieldWrapper<Scalars['Int']>;
  url: FieldWrapper<Scalars['Int']>;
  articleRefId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlArticleRefCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored: Scalars['Boolean'];
  url?: Maybe<Scalars['String']>;
  owner: GqlUserCreateNestedOneWithoutArticleRefsInput;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  related?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefCreateNestedOneWithoutRelatedInput>;
};

export type GqlArticleRefCreateNestedManyWithoutArticleInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutArticleInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutArticleInput>>;
  connect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
};

export type GqlArticleRefCreateNestedManyWithoutArticleRefInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutArticleRefInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutArticleRefInput>>;
  connect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
};

export type GqlArticleRefCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutOwnerInput>>;
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

export type GqlArticleRefCreateWithoutArticleInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored: Scalars['Boolean'];
  url?: Maybe<Scalars['String']>;
  owner: GqlUserCreateNestedOneWithoutArticleRefsInput;
  related?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefCreateNestedOneWithoutRelatedInput>;
};

export type GqlArticleRefCreateWithoutArticleRefInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored: Scalars['Boolean'];
  url?: Maybe<Scalars['String']>;
  owner: GqlUserCreateNestedOneWithoutArticleRefsInput;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  related?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleRefInput>;
};

export type GqlArticleRefCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored: Scalars['Boolean'];
  url?: Maybe<Scalars['String']>;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  related?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefCreateNestedOneWithoutRelatedInput>;
};

export type GqlArticleRefCreateWithoutRelatedInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  favored: Scalars['Boolean'];
  url?: Maybe<Scalars['String']>;
  owner: GqlUserCreateNestedOneWithoutArticleRefsInput;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  articleRef?: Maybe<GqlArticleRefCreateNestedOneWithoutRelatedInput>;
};

export type GqlArticleRefGroupBy = {
  __typename?: 'ArticleRefGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  favored: FieldWrapper<Scalars['Boolean']>;
  articleId: FieldWrapper<Scalars['String']>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
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
  url?: Maybe<FieldWrapper<Scalars['String']>>;
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
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  articleRefId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleRefOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  favored?: Maybe<GqlSortOrder>;
  articleId?: Maybe<GqlSortOrder>;
  url?: Maybe<GqlSortOrder>;
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
  ArticleId = 'articleId',
  Url = 'url',
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
  articleId?: Maybe<GqlStringFilter>;
  url?: Maybe<GqlStringNullableFilter>;
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
  articleId?: Maybe<GqlStringWithAggregatesFilter>;
  url?: Maybe<GqlStringNullableWithAggregatesFilter>;
  articleRefId?: Maybe<GqlStringNullableWithAggregatesFilter>;
};

export type GqlArticleRefUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  related?: Maybe<GqlArticleRefUpdateManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefUpdateOneWithoutRelatedInput>;
};

export type GqlArticleRefUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
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

export type GqlArticleRefUpdateManyWithoutArticleInput = {
  create?: Maybe<Array<GqlArticleRefCreateWithoutArticleInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleRefCreateOrConnectWithoutArticleInput>>;
  upsert?: Maybe<Array<GqlArticleRefUpsertWithWhereUniqueWithoutArticleInput>>;
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
  connect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  set?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  delete?: Maybe<Array<GqlArticleRefWhereUniqueInput>>;
  update?: Maybe<Array<GqlArticleRefUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<GqlArticleRefUpdateManyWithWhereWithoutOwnerInput>>;
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

export type GqlArticleRefUpdateWithoutArticleInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  related?: Maybe<GqlArticleRefUpdateManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefUpdateOneWithoutRelatedInput>;
};

export type GqlArticleRefUpdateWithoutArticleRefInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  related?: Maybe<GqlArticleRefUpdateManyWithoutArticleRefInput>;
};

export type GqlArticleRefUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  related?: Maybe<GqlArticleRefUpdateManyWithoutArticleRefInput>;
  articleRef?: Maybe<GqlArticleRefUpdateOneWithoutRelatedInput>;
};

export type GqlArticleRefUpdateWithoutRelatedInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
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
  article?: Maybe<GqlArticleRelationFilter>;
  articleId?: Maybe<GqlStringFilter>;
  related?: Maybe<GqlArticleRefListRelationFilter>;
  url?: Maybe<GqlStringNullableFilter>;
  articleRef?: Maybe<GqlArticleRefRelationFilter>;
  articleRefId?: Maybe<GqlStringNullableFilter>;
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
  Url = 'url',
  Author = 'author',
  Title = 'title',
  Tags = 'tags',
  ContentText = 'content_text',
  ContentHtml = 'content_html',
  EnclosureJson = 'enclosure_json',
  FeedId = 'feedId'
}

export type GqlArticleScalarWhereInput = {
  AND?: Maybe<Array<GqlArticleScalarWhereInput>>;
  OR?: Maybe<Array<GqlArticleScalarWhereInput>>;
  NOT?: Maybe<Array<GqlArticleScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  date_published?: Maybe<GqlDateTimeFilter>;
  date_modified?: Maybe<GqlDateTimeNullableFilter>;
  url?: Maybe<GqlStringFilter>;
  author?: Maybe<GqlStringNullableFilter>;
  title?: Maybe<GqlStringFilter>;
  tags?: Maybe<GqlStringNullableFilter>;
  content_text?: Maybe<GqlStringFilter>;
  content_html?: Maybe<GqlStringNullableFilter>;
  enclosure_json?: Maybe<GqlStringNullableFilter>;
  feedId?: Maybe<GqlStringNullableFilter>;
};

export type GqlArticleScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlArticleScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlArticleScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlArticleScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  date_published?: Maybe<GqlDateTimeWithAggregatesFilter>;
  date_modified?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
  url?: Maybe<GqlStringWithAggregatesFilter>;
  author?: Maybe<GqlStringNullableWithAggregatesFilter>;
  title?: Maybe<GqlStringWithAggregatesFilter>;
  tags?: Maybe<GqlStringNullableWithAggregatesFilter>;
  content_text?: Maybe<GqlStringWithAggregatesFilter>;
  content_html?: Maybe<GqlStringNullableWithAggregatesFilter>;
  enclosure_json?: Maybe<GqlStringNullableWithAggregatesFilter>;
  feedId?: Maybe<GqlStringNullableWithAggregatesFilter>;
};

export type GqlArticleUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_published?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_modified?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  content_text?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content_html?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  enclosure_json?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  feed?: Maybe<GqlFeedUpdateOneWithoutArticlesInput>;
  ArticleRef?: Maybe<GqlArticleRefUpdateManyWithoutArticleInput>;
};

export type GqlArticleUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_published?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_modified?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  content_text?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content_html?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  enclosure_json?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
};

export type GqlArticleUpdateManyWithWhereWithoutFeedInput = {
  where: GqlArticleScalarWhereInput;
  data: GqlArticleUpdateManyMutationInput;
};

export type GqlArticleUpdateManyWithoutFeedInput = {
  create?: Maybe<Array<GqlArticleCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleCreateOrConnectWithoutFeedInput>>;
  upsert?: Maybe<Array<GqlArticleUpsertWithWhereUniqueWithoutFeedInput>>;
  connect?: Maybe<Array<GqlArticleWhereUniqueInput>>;
  set?: Maybe<Array<GqlArticleWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlArticleWhereUniqueInput>>;
  delete?: Maybe<Array<GqlArticleWhereUniqueInput>>;
  update?: Maybe<Array<GqlArticleUpdateWithWhereUniqueWithoutFeedInput>>;
  updateMany?: Maybe<Array<GqlArticleUpdateManyWithWhereWithoutFeedInput>>;
  deleteMany?: Maybe<Array<GqlArticleScalarWhereInput>>;
};

export type GqlArticleUpdateOneRequiredWithoutArticleRefInput = {
  create?: Maybe<GqlArticleCreateWithoutArticleRefInput>;
  connectOrCreate?: Maybe<GqlArticleCreateOrConnectWithoutArticleRefInput>;
  upsert?: Maybe<GqlArticleUpsertWithoutArticleRefInput>;
  connect?: Maybe<GqlArticleWhereUniqueInput>;
  update?: Maybe<GqlArticleUpdateWithoutArticleRefInput>;
};

export type GqlArticleUpdateWithWhereUniqueWithoutFeedInput = {
  where: GqlArticleWhereUniqueInput;
  data: GqlArticleUpdateWithoutFeedInput;
};

export type GqlArticleUpdateWithoutArticleRefInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_published?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_modified?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  content_text?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content_html?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  enclosure_json?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  feed?: Maybe<GqlFeedUpdateOneWithoutArticlesInput>;
};

export type GqlArticleUpdateWithoutFeedInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_published?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_modified?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  content_text?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content_html?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  enclosure_json?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  ArticleRef?: Maybe<GqlArticleRefUpdateManyWithoutArticleInput>;
};

export type GqlArticleUpsertWithWhereUniqueWithoutFeedInput = {
  where: GqlArticleWhereUniqueInput;
  update: GqlArticleUpdateWithoutFeedInput;
  create: GqlArticleCreateWithoutFeedInput;
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
  url?: Maybe<GqlStringFilter>;
  author?: Maybe<GqlStringNullableFilter>;
  title?: Maybe<GqlStringFilter>;
  tags?: Maybe<GqlStringNullableFilter>;
  content_text?: Maybe<GqlStringFilter>;
  content_html?: Maybe<GqlStringNullableFilter>;
  enclosure_json?: Maybe<GqlStringNullableFilter>;
  feed?: Maybe<GqlFeedRelationFilter>;
  feedId?: Maybe<GqlStringNullableFilter>;
  ArticleRef?: Maybe<GqlArticleRefListRelationFilter>;
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
  takeIfFilter?: Maybe<FieldWrapper<Scalars['String']>>;
  retentionPolicy?: Maybe<FieldWrapper<Scalars['String']>>;
  owner: FieldWrapper<GqlUser>;
  subscriptions: Array<FieldWrapper<GqlSubscription>>;
  entryPostProcessors: Array<FieldWrapper<GqlEntryPostProcessor>>;
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
  takeIfFilter: FieldWrapper<Scalars['Int']>;
  retentionPolicy: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlBucketCreateInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  takeIfFilter?: Maybe<Scalars['String']>;
  retentionPolicy?: Maybe<Scalars['String']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutBucketInput>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorCreateNestedManyWithoutBucketInput>;
};

export type GqlBucketCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlBucketCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlBucketCreateOrConnectWithoutOwnerInput>>;
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
  takeIfFilter?: Maybe<Scalars['String']>;
  retentionPolicy?: Maybe<Scalars['String']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutBucketInput>;
};

export type GqlBucketCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  takeIfFilter?: Maybe<Scalars['String']>;
  retentionPolicy?: Maybe<Scalars['String']>;
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
  takeIfFilter?: Maybe<Scalars['String']>;
  retentionPolicy?: Maybe<Scalars['String']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorCreateNestedManyWithoutBucketInput>;
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
  takeIfFilter?: Maybe<FieldWrapper<Scalars['String']>>;
  retentionPolicy?: Maybe<FieldWrapper<Scalars['String']>>;
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
  takeIfFilter?: Maybe<FieldWrapper<Scalars['String']>>;
  retentionPolicy?: Maybe<FieldWrapper<Scalars['String']>>;
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
  takeIfFilter?: Maybe<FieldWrapper<Scalars['String']>>;
  retentionPolicy?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlBucketOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  description?: Maybe<GqlSortOrder>;
  listed?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  takeIfFilter?: Maybe<GqlSortOrder>;
  retentionPolicy?: Maybe<GqlSortOrder>;
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
  TakeIfFilter = 'takeIfFilter',
  RetentionPolicy = 'retentionPolicy'
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
  takeIfFilter?: Maybe<GqlStringNullableFilter>;
  retentionPolicy?: Maybe<GqlStringNullableFilter>;
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
  takeIfFilter?: Maybe<GqlStringNullableWithAggregatesFilter>;
  retentionPolicy?: Maybe<GqlStringNullableWithAggregatesFilter>;
};

export type GqlBucketUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  takeIfFilter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  retentionPolicy?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutBucketInput>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorUpdateManyWithoutBucketInput>;
};

export type GqlBucketUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  takeIfFilter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  retentionPolicy?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
};

export type GqlBucketUpdateManyWithWhereWithoutOwnerInput = {
  where: GqlBucketScalarWhereInput;
  data: GqlBucketUpdateManyMutationInput;
};

export type GqlBucketUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlBucketCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlBucketCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<GqlBucketUpsertWithWhereUniqueWithoutOwnerInput>>;
  connect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  set?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  delete?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  update?: Maybe<Array<GqlBucketUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<GqlBucketUpdateManyWithWhereWithoutOwnerInput>>;
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

export type GqlBucketUpdateWithoutEntryPostProcessorsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  takeIfFilter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  retentionPolicy?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutBucketInput>;
};

export type GqlBucketUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  takeIfFilter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  retentionPolicy?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
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
  takeIfFilter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  retentionPolicy?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorUpdateManyWithoutBucketInput>;
};

export type GqlBucketUpsertWithWhereUniqueWithoutOwnerInput = {
  where: GqlBucketWhereUniqueInput;
  update: GqlBucketUpdateWithoutOwnerInput;
  create: GqlBucketCreateWithoutOwnerInput;
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
  takeIfFilter?: Maybe<GqlStringNullableFilter>;
  retentionPolicy?: Maybe<GqlStringNullableFilter>;
  entryPostProcessors?: Maybe<GqlEntryPostProcessorListRelationFilter>;
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

export type GqlEntryPostProcessorCreateNestedManyWithoutBucketInput = {
  create?: Maybe<Array<GqlEntryPostProcessorCreateWithoutBucketInput>>;
  connectOrCreate?: Maybe<Array<GqlEntryPostProcessorCreateOrConnectWithoutBucketInput>>;
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
  home_page_url: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  expired: FieldWrapper<Scalars['Boolean']>;
  description: FieldWrapper<Scalars['String']>;
  status?: Maybe<FieldWrapper<Scalars['String']>>;
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  nextHarvestAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  articles: Array<FieldWrapper<GqlArticle>>;
  subscriptions: Array<FieldWrapper<GqlSubscription>>;
};


export type GqlFeedArticlesArgs = {
  where?: Maybe<GqlArticleWhereInput>;
  orderBy?: Maybe<Array<GqlArticleOrderByInput>>;
  cursor?: Maybe<GqlArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleScalarFieldEnum>>;
};


export type GqlFeedSubscriptionsArgs = {
  where?: Maybe<GqlSubscriptionWhereInput>;
  orderBy?: Maybe<Array<GqlSubscriptionOrderByInput>>;
  cursor?: Maybe<GqlSubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlSubscriptionScalarFieldEnum>>;
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
  author: FieldWrapper<Scalars['Int']>;
  expired: FieldWrapper<Scalars['Int']>;
  description: FieldWrapper<Scalars['Int']>;
  status: FieldWrapper<Scalars['Int']>;
  harvestIntervalMinutes: FieldWrapper<Scalars['Int']>;
  nextHarvestAt: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlFeedCreateInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url: Scalars['String'];
  title: Scalars['String'];
  author?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  description: Scalars['String'];
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  articles?: Maybe<GqlArticleCreateNestedManyWithoutFeedInput>;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedCreateNestedOneWithoutArticlesInput = {
  create?: Maybe<GqlFeedCreateWithoutArticlesInput>;
  connectOrCreate?: Maybe<GqlFeedCreateOrConnectWithoutArticlesInput>;
  connect?: Maybe<GqlFeedWhereUniqueInput>;
};

export type GqlFeedCreateNestedOneWithoutSubscriptionsInput = {
  create?: Maybe<GqlFeedCreateWithoutSubscriptionsInput>;
  connectOrCreate?: Maybe<GqlFeedCreateOrConnectWithoutSubscriptionsInput>;
  connect?: Maybe<GqlFeedWhereUniqueInput>;
};

export type GqlFeedCreateOrConnectWithoutArticlesInput = {
  where: GqlFeedWhereUniqueInput;
  create: GqlFeedCreateWithoutArticlesInput;
};

export type GqlFeedCreateOrConnectWithoutSubscriptionsInput = {
  where: GqlFeedWhereUniqueInput;
  create: GqlFeedCreateWithoutSubscriptionsInput;
};

export type GqlFeedCreateWithoutArticlesInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url: Scalars['String'];
  title: Scalars['String'];
  author?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  description: Scalars['String'];
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedCreateWithoutSubscriptionsInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url: Scalars['String'];
  title: Scalars['String'];
  author?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  description: Scalars['String'];
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  articles?: Maybe<GqlArticleCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedGroupBy = {
  __typename?: 'FeedGroupBy';
  id: FieldWrapper<Scalars['String']>;
  feed_url: FieldWrapper<Scalars['String']>;
  home_page_url: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  expired: FieldWrapper<Scalars['Boolean']>;
  description: FieldWrapper<Scalars['String']>;
  status?: Maybe<FieldWrapper<Scalars['String']>>;
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  nextHarvestAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  _count?: Maybe<FieldWrapper<GqlFeedCountAggregate>>;
  _avg?: Maybe<FieldWrapper<GqlFeedAvgAggregate>>;
  _sum?: Maybe<FieldWrapper<GqlFeedSumAggregate>>;
  _min?: Maybe<FieldWrapper<GqlFeedMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlFeedMaxAggregate>>;
};

export type GqlFeedMaxAggregate = {
  __typename?: 'FeedMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  home_page_url?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  expired?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  status?: Maybe<FieldWrapper<Scalars['String']>>;
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  nextHarvestAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlFeedMinAggregate = {
  __typename?: 'FeedMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  home_page_url?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  expired?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  status?: Maybe<FieldWrapper<Scalars['String']>>;
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  nextHarvestAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlFeedOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  feed_url?: Maybe<GqlSortOrder>;
  home_page_url?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  author?: Maybe<GqlSortOrder>;
  expired?: Maybe<GqlSortOrder>;
  description?: Maybe<GqlSortOrder>;
  status?: Maybe<GqlSortOrder>;
  harvestIntervalMinutes?: Maybe<GqlSortOrder>;
  nextHarvestAt?: Maybe<GqlSortOrder>;
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
  Author = 'author',
  Expired = 'expired',
  Description = 'description',
  Status = 'status',
  HarvestIntervalMinutes = 'harvestIntervalMinutes',
  NextHarvestAt = 'nextHarvestAt',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt'
}

export type GqlFeedScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlFeedScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlFeedScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlFeedScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  feed_url?: Maybe<GqlStringWithAggregatesFilter>;
  home_page_url?: Maybe<GqlStringWithAggregatesFilter>;
  title?: Maybe<GqlStringWithAggregatesFilter>;
  author?: Maybe<GqlStringNullableWithAggregatesFilter>;
  expired?: Maybe<GqlBoolWithAggregatesFilter>;
  description?: Maybe<GqlStringWithAggregatesFilter>;
  status?: Maybe<GqlStringNullableWithAggregatesFilter>;
  harvestIntervalMinutes?: Maybe<GqlIntNullableWithAggregatesFilter>;
  nextHarvestAt?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
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
  home_page_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  articles?: Maybe<GqlArticleUpdateManyWithoutFeedInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
};

export type GqlFeedUpdateOneRequiredWithoutSubscriptionsInput = {
  create?: Maybe<GqlFeedCreateWithoutSubscriptionsInput>;
  connectOrCreate?: Maybe<GqlFeedCreateOrConnectWithoutSubscriptionsInput>;
  upsert?: Maybe<GqlFeedUpsertWithoutSubscriptionsInput>;
  connect?: Maybe<GqlFeedWhereUniqueInput>;
  update?: Maybe<GqlFeedUpdateWithoutSubscriptionsInput>;
};

export type GqlFeedUpdateOneWithoutArticlesInput = {
  create?: Maybe<GqlFeedCreateWithoutArticlesInput>;
  connectOrCreate?: Maybe<GqlFeedCreateOrConnectWithoutArticlesInput>;
  upsert?: Maybe<GqlFeedUpsertWithoutArticlesInput>;
  connect?: Maybe<GqlFeedWhereUniqueInput>;
  disconnect?: Maybe<Scalars['Boolean']>;
  delete?: Maybe<Scalars['Boolean']>;
  update?: Maybe<GqlFeedUpdateWithoutArticlesInput>;
};

export type GqlFeedUpdateWithoutArticlesInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpdateWithoutSubscriptionsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  articles?: Maybe<GqlArticleUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpsertWithoutArticlesInput = {
  update: GqlFeedUpdateWithoutArticlesInput;
  create: GqlFeedCreateWithoutArticlesInput;
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
  home_page_url?: Maybe<GqlStringFilter>;
  title?: Maybe<GqlStringFilter>;
  author?: Maybe<GqlStringNullableFilter>;
  expired?: Maybe<GqlBoolFilter>;
  description?: Maybe<GqlStringFilter>;
  status?: Maybe<GqlStringNullableFilter>;
  harvestIntervalMinutes?: Maybe<GqlIntNullableFilter>;
  nextHarvestAt?: Maybe<GqlDateTimeNullableFilter>;
  articles?: Maybe<GqlArticleListRelationFilter>;
  subscriptions?: Maybe<GqlSubscriptionListRelationFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
};

export type GqlFeedWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
  feed_url?: Maybe<Scalars['String']>;
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

export type GqlMutation = {
  __typename?: 'Mutation';
  createArticle: FieldWrapper<GqlArticle>;
  deleteArticle?: Maybe<FieldWrapper<GqlArticle>>;
  updateArticle?: Maybe<FieldWrapper<GqlArticle>>;
  deleteManyArticle: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyArticle: FieldWrapper<GqlAffectedRowsOutput>;
  upsertArticle: FieldWrapper<GqlArticle>;
  createArticleRef: FieldWrapper<GqlArticleRef>;
  deleteArticleRef?: Maybe<FieldWrapper<GqlArticleRef>>;
  updateArticleRef?: Maybe<FieldWrapper<GqlArticleRef>>;
  deleteManyArticleRef: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyArticleRef: FieldWrapper<GqlAffectedRowsOutput>;
  upsertArticleRef: FieldWrapper<GqlArticleRef>;
  createBucket: FieldWrapper<GqlBucket>;
  deleteBucket?: Maybe<FieldWrapper<GqlBucket>>;
  updateBucket?: Maybe<FieldWrapper<GqlBucket>>;
  deleteManyBucket: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyBucket: FieldWrapper<GqlAffectedRowsOutput>;
  upsertBucket: FieldWrapper<GqlBucket>;
  createEntryPostProcessor: FieldWrapper<GqlEntryPostProcessor>;
  deleteEntryPostProcessor?: Maybe<FieldWrapper<GqlEntryPostProcessor>>;
  updateEntryPostProcessor?: Maybe<FieldWrapper<GqlEntryPostProcessor>>;
  deleteManyEntryPostProcessor: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyEntryPostProcessor: FieldWrapper<GqlAffectedRowsOutput>;
  upsertEntryPostProcessor: FieldWrapper<GqlEntryPostProcessor>;
  createFeed: FieldWrapper<GqlFeed>;
  deleteFeed?: Maybe<FieldWrapper<GqlFeed>>;
  updateFeed?: Maybe<FieldWrapper<GqlFeed>>;
  deleteManyFeed: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyFeed: FieldWrapper<GqlAffectedRowsOutput>;
  upsertFeed: FieldWrapper<GqlFeed>;
  createSubscription: FieldWrapper<GqlSubscription>;
  deleteSubscription?: Maybe<FieldWrapper<GqlSubscription>>;
  updateSubscription?: Maybe<FieldWrapper<GqlSubscription>>;
  deleteManySubscription: FieldWrapper<GqlAffectedRowsOutput>;
  updateManySubscription: FieldWrapper<GqlAffectedRowsOutput>;
  upsertSubscription: FieldWrapper<GqlSubscription>;
  createUser: FieldWrapper<GqlUser>;
  deleteUser?: Maybe<FieldWrapper<GqlUser>>;
  updateUser?: Maybe<FieldWrapper<GqlUser>>;
  deleteManyUser: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyUser: FieldWrapper<GqlAffectedRowsOutput>;
  upsertUser: FieldWrapper<GqlUser>;
  createUserArticle: FieldWrapper<GqlUserArticle>;
  deleteUserArticle?: Maybe<FieldWrapper<GqlUserArticle>>;
  updateUserArticle?: Maybe<FieldWrapper<GqlUserArticle>>;
  deleteManyUserArticle: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyUserArticle: FieldWrapper<GqlAffectedRowsOutput>;
  upsertUserArticle: FieldWrapper<GqlUserArticle>;
  createUserFeed: FieldWrapper<GqlUserFeed>;
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


export type GqlMutationCreateSubscriptionArgs = {
  data: GqlSubscriptionCreateInput;
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
  articlesForFeedUrl: Array<FieldWrapper<GqlArticle>>;
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

export enum GqlSortOrder {
  Asc = 'asc',
  Desc = 'desc'
}

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
  tags: FieldWrapper<Scalars['String']>;
  feedId: FieldWrapper<Scalars['String']>;
  ownerId: FieldWrapper<Scalars['String']>;
  bucketId: FieldWrapper<Scalars['String']>;
  feed: FieldWrapper<GqlFeed>;
  owner: FieldWrapper<GqlUser>;
  bucket: FieldWrapper<GqlBucket>;
};

export type GqlSubscriptionCountAggregate = {
  __typename?: 'SubscriptionCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  tags: FieldWrapper<Scalars['Int']>;
  feedId: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  bucketId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlSubscriptionCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  tags?: Maybe<Scalars['String']>;
  feed: GqlFeedCreateNestedOneWithoutSubscriptionsInput;
  owner: GqlUserCreateNestedOneWithoutSubscriptionInput;
  bucket: GqlBucketCreateNestedOneWithoutSubscriptionsInput;
};

export type GqlSubscriptionCreateNestedManyWithoutBucketInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutBucketInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutBucketInput>>;
  connect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
};

export type GqlSubscriptionCreateNestedManyWithoutFeedInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutFeedInput>>;
  connect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
};

export type GqlSubscriptionCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutOwnerInput>>;
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

export type GqlSubscriptionCreateWithoutBucketInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  tags?: Maybe<Scalars['String']>;
  feed: GqlFeedCreateNestedOneWithoutSubscriptionsInput;
  owner: GqlUserCreateNestedOneWithoutSubscriptionInput;
};

export type GqlSubscriptionCreateWithoutFeedInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  tags?: Maybe<Scalars['String']>;
  owner: GqlUserCreateNestedOneWithoutSubscriptionInput;
  bucket: GqlBucketCreateNestedOneWithoutSubscriptionsInput;
};

export type GqlSubscriptionCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  tags?: Maybe<Scalars['String']>;
  feed: GqlFeedCreateNestedOneWithoutSubscriptionsInput;
  bucket: GqlBucketCreateNestedOneWithoutSubscriptionsInput;
};

export type GqlSubscriptionGroupBy = {
  __typename?: 'SubscriptionGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  tags: FieldWrapper<Scalars['String']>;
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
  tags?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlSubscriptionMinAggregate = {
  __typename?: 'SubscriptionMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  tags?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlSubscriptionOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  tags?: Maybe<GqlSortOrder>;
  feedId?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  bucketId?: Maybe<GqlSortOrder>;
};

export enum GqlSubscriptionScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Tags = 'tags',
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
  tags?: Maybe<GqlStringFilter>;
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
  tags?: Maybe<GqlStringWithAggregatesFilter>;
  feedId?: Maybe<GqlStringWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
  bucketId?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlSubscriptionUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  tags?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed?: Maybe<GqlFeedUpdateOneRequiredWithoutSubscriptionsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutSubscriptionInput>;
  bucket?: Maybe<GqlBucketUpdateOneRequiredWithoutSubscriptionsInput>;
};

export type GqlSubscriptionUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  tags?: Maybe<GqlStringFieldUpdateOperationsInput>;
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

export type GqlSubscriptionUpdateManyWithoutBucketInput = {
  create?: Maybe<Array<GqlSubscriptionCreateWithoutBucketInput>>;
  connectOrCreate?: Maybe<Array<GqlSubscriptionCreateOrConnectWithoutBucketInput>>;
  upsert?: Maybe<Array<GqlSubscriptionUpsertWithWhereUniqueWithoutBucketInput>>;
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
  connect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  set?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  delete?: Maybe<Array<GqlSubscriptionWhereUniqueInput>>;
  update?: Maybe<Array<GqlSubscriptionUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<GqlSubscriptionUpdateManyWithWhereWithoutOwnerInput>>;
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

export type GqlSubscriptionUpdateWithoutBucketInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  tags?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed?: Maybe<GqlFeedUpdateOneRequiredWithoutSubscriptionsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutSubscriptionInput>;
};

export type GqlSubscriptionUpdateWithoutFeedInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  tags?: Maybe<GqlStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutSubscriptionInput>;
  bucket?: Maybe<GqlBucketUpdateOneRequiredWithoutSubscriptionsInput>;
};

export type GqlSubscriptionUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  tags?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed?: Maybe<GqlFeedUpdateOneRequiredWithoutSubscriptionsInput>;
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

export type GqlSubscriptionWhereInput = {
  AND?: Maybe<Array<GqlSubscriptionWhereInput>>;
  OR?: Maybe<Array<GqlSubscriptionWhereInput>>;
  NOT?: Maybe<Array<GqlSubscriptionWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  tags?: Maybe<GqlStringFilter>;
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

export type GqlUserArticleCreateNestedManyWithoutUserFeedInput = {
  create?: Maybe<Array<GqlUserArticleCreateWithoutUserFeedInput>>;
  connectOrCreate?: Maybe<Array<GqlUserArticleCreateOrConnectWithoutUserFeedInput>>;
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

export type GqlUserFeedCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlUserFeedCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlUserFeedCreateOrConnectWithoutOwnerInput>>;
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
