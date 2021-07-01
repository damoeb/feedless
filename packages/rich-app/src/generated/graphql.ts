import { gql } from 'apollo-angular';
export type Maybe<T> = T | null;
export type Exact<T extends { [key: string]: unknown }> = {
  [K in keyof T]: T[K];
};
export type MakeOptional<T, K extends keyof T> = Omit<T, K> &
  { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> &
  { [SubKey in K]: Maybe<T[SubKey]> };
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

export type AffectedRowsOutput = {
  __typename?: 'AffectedRowsOutput';
  count: FieldWrapper<Scalars['Int']>;
};

export type AggregateArticle = {
  __typename?: 'AggregateArticle';
  _count?: Maybe<FieldWrapper<ArticleCountAggregate>>;
  _min?: Maybe<FieldWrapper<ArticleMinAggregate>>;
  _max?: Maybe<FieldWrapper<ArticleMaxAggregate>>;
};

export type AggregateArticleFilter = {
  __typename?: 'AggregateArticleFilter';
  _count?: Maybe<FieldWrapper<ArticleFilterCountAggregate>>;
  _min?: Maybe<FieldWrapper<ArticleFilterMinAggregate>>;
  _max?: Maybe<FieldWrapper<ArticleFilterMaxAggregate>>;
};

export type AggregateArticleRef = {
  __typename?: 'AggregateArticleRef';
  _count?: Maybe<FieldWrapper<ArticleRefCountAggregate>>;
  _min?: Maybe<FieldWrapper<ArticleRefMinAggregate>>;
  _max?: Maybe<FieldWrapper<ArticleRefMaxAggregate>>;
};

export type AggregateBucket = {
  __typename?: 'AggregateBucket';
  _count?: Maybe<FieldWrapper<BucketCountAggregate>>;
  _min?: Maybe<FieldWrapper<BucketMinAggregate>>;
  _max?: Maybe<FieldWrapper<BucketMaxAggregate>>;
};

export type AggregateFeed = {
  __typename?: 'AggregateFeed';
  _count?: Maybe<FieldWrapper<FeedCountAggregate>>;
  _min?: Maybe<FieldWrapper<FeedMinAggregate>>;
  _max?: Maybe<FieldWrapper<FeedMaxAggregate>>;
};

export type AggregateSubscription = {
  __typename?: 'AggregateSubscription';
  _count?: Maybe<FieldWrapper<SubscriptionCountAggregate>>;
  _min?: Maybe<FieldWrapper<SubscriptionMinAggregate>>;
  _max?: Maybe<FieldWrapper<SubscriptionMaxAggregate>>;
};

export type AggregateUser = {
  __typename?: 'AggregateUser';
  _count?: Maybe<FieldWrapper<UserCountAggregate>>;
  _min?: Maybe<FieldWrapper<UserMinAggregate>>;
  _max?: Maybe<FieldWrapper<UserMaxAggregate>>;
};

export type AggregateUserArticle = {
  __typename?: 'AggregateUserArticle';
  _count?: Maybe<FieldWrapper<UserArticleCountAggregate>>;
  _min?: Maybe<FieldWrapper<UserArticleMinAggregate>>;
  _max?: Maybe<FieldWrapper<UserArticleMaxAggregate>>;
};

export type AggregateUserFeed = {
  __typename?: 'AggregateUserFeed';
  _count?: Maybe<FieldWrapper<UserFeedCountAggregate>>;
  _min?: Maybe<FieldWrapper<UserFeedMinAggregate>>;
  _max?: Maybe<FieldWrapper<UserFeedMaxAggregate>>;
};

export type Article = {
  __typename?: 'Article';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  url: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  content: FieldWrapper<Scalars['String']>;
  contentHtml: FieldWrapper<Scalars['String']>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  ArticleRef: Array<FieldWrapper<ArticleRef>>;
  Feed?: Maybe<FieldWrapper<Feed>>;
};

export type ArticleArticleRefArgs = {
  where?: Maybe<ArticleRefWhereInput>;
  orderBy?: Maybe<Array<ArticleRefOrderByInput>>;
  cursor?: Maybe<ArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<ArticleRefScalarFieldEnum>>;
};

export type ArticleCountAggregate = {
  __typename?: 'ArticleCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  url: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  content: FieldWrapper<Scalars['Int']>;
  contentHtml: FieldWrapper<Scalars['Int']>;
  feedId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type ArticleCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  url: Scalars['String'];
  title: Scalars['String'];
  content: Scalars['String'];
  contentHtml: Scalars['String'];
  ArticleRef?: Maybe<ArticleRefCreateNestedManyWithoutArticleInput>;
  Feed?: Maybe<FeedCreateNestedOneWithoutArticlesInput>;
};

export type ArticleCreateNestedManyWithoutFeedInput = {
  create?: Maybe<Array<ArticleCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<ArticleCreateOrConnectWithoutFeedInput>>;
  connect?: Maybe<Array<ArticleWhereUniqueInput>>;
};

export type ArticleCreateNestedOneWithoutArticleRefInput = {
  create?: Maybe<ArticleCreateWithoutArticleRefInput>;
  connectOrCreate?: Maybe<ArticleCreateOrConnectWithoutArticleRefInput>;
  connect?: Maybe<ArticleWhereUniqueInput>;
};

export type ArticleCreateOrConnectWithoutArticleRefInput = {
  where: ArticleWhereUniqueInput;
  create: ArticleCreateWithoutArticleRefInput;
};

export type ArticleCreateOrConnectWithoutFeedInput = {
  where: ArticleWhereUniqueInput;
  create: ArticleCreateWithoutFeedInput;
};

export type ArticleCreateWithoutArticleRefInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  url: Scalars['String'];
  title: Scalars['String'];
  content: Scalars['String'];
  contentHtml: Scalars['String'];
  Feed?: Maybe<FeedCreateNestedOneWithoutArticlesInput>;
};

export type ArticleCreateWithoutFeedInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  url: Scalars['String'];
  title: Scalars['String'];
  content: Scalars['String'];
  contentHtml: Scalars['String'];
  ArticleRef?: Maybe<ArticleRefCreateNestedManyWithoutArticleInput>;
};

export type ArticleFilter = {
  __typename?: 'ArticleFilter';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
};

export type ArticleFilterCountAggregate = {
  __typename?: 'ArticleFilterCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type ArticleFilterCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
};

export type ArticleFilterGroupBy = {
  __typename?: 'ArticleFilterGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  _count?: Maybe<FieldWrapper<ArticleFilterCountAggregate>>;
  _min?: Maybe<FieldWrapper<ArticleFilterMinAggregate>>;
  _max?: Maybe<FieldWrapper<ArticleFilterMaxAggregate>>;
};

export type ArticleFilterMaxAggregate = {
  __typename?: 'ArticleFilterMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type ArticleFilterMinAggregate = {
  __typename?: 'ArticleFilterMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type ArticleFilterOrderByInput = {
  id?: Maybe<SortOrder>;
  createdAt?: Maybe<SortOrder>;
  updatedAt?: Maybe<SortOrder>;
};

export enum ArticleFilterScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
}

export type ArticleFilterScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<ArticleFilterScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<ArticleFilterScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<ArticleFilterScalarWhereWithAggregatesInput>>;
  id?: Maybe<StringWithAggregatesFilter>;
  createdAt?: Maybe<DateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<DateTimeWithAggregatesFilter>;
};

export type ArticleFilterUpdateInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
};

export type ArticleFilterUpdateManyMutationInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
};

export type ArticleFilterWhereInput = {
  AND?: Maybe<Array<ArticleFilterWhereInput>>;
  OR?: Maybe<Array<ArticleFilterWhereInput>>;
  NOT?: Maybe<Array<ArticleFilterWhereInput>>;
  id?: Maybe<StringFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
};

export type ArticleFilterWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type ArticleGroupBy = {
  __typename?: 'ArticleGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  url: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  content: FieldWrapper<Scalars['String']>;
  contentHtml: FieldWrapper<Scalars['String']>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  _count?: Maybe<FieldWrapper<ArticleCountAggregate>>;
  _min?: Maybe<FieldWrapper<ArticleMinAggregate>>;
  _max?: Maybe<FieldWrapper<ArticleMaxAggregate>>;
};

export type ArticleListRelationFilter = {
  every?: Maybe<ArticleWhereInput>;
  some?: Maybe<ArticleWhereInput>;
  none?: Maybe<ArticleWhereInput>;
};

export type ArticleMaxAggregate = {
  __typename?: 'ArticleMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  content?: Maybe<FieldWrapper<Scalars['String']>>;
  contentHtml?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type ArticleMinAggregate = {
  __typename?: 'ArticleMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  content?: Maybe<FieldWrapper<Scalars['String']>>;
  contentHtml?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type ArticleOrderByInput = {
  id?: Maybe<SortOrder>;
  createdAt?: Maybe<SortOrder>;
  updatedAt?: Maybe<SortOrder>;
  url?: Maybe<SortOrder>;
  title?: Maybe<SortOrder>;
  content?: Maybe<SortOrder>;
  contentHtml?: Maybe<SortOrder>;
  feedId?: Maybe<SortOrder>;
};

export type ArticleRef = {
  __typename?: 'ArticleRef';
  id: FieldWrapper<Scalars['String']>;
  read: FieldWrapper<Scalars['Boolean']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  articleId: FieldWrapper<Scalars['String']>;
  feedId: FieldWrapper<Scalars['String']>;
  owner: FieldWrapper<User>;
  article: FieldWrapper<Article>;
  feed: FieldWrapper<Feed>;
};

export type ArticleRefCountAggregate = {
  __typename?: 'ArticleRefCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  read: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  articleId: FieldWrapper<Scalars['Int']>;
  feedId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type ArticleRefCreateInput = {
  id?: Maybe<Scalars['String']>;
  read?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  owner: UserCreateNestedOneWithoutArticleRefsInput;
  article: ArticleCreateNestedOneWithoutArticleRefInput;
  feed: FeedCreateNestedOneWithoutArticleRefInput;
};

export type ArticleRefCreateNestedManyWithoutArticleInput = {
  create?: Maybe<Array<ArticleRefCreateWithoutArticleInput>>;
  connectOrCreate?: Maybe<Array<ArticleRefCreateOrConnectWithoutArticleInput>>;
  connect?: Maybe<Array<ArticleRefWhereUniqueInput>>;
};

export type ArticleRefCreateNestedManyWithoutFeedInput = {
  create?: Maybe<Array<ArticleRefCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<ArticleRefCreateOrConnectWithoutFeedInput>>;
  connect?: Maybe<Array<ArticleRefWhereUniqueInput>>;
};

export type ArticleRefCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<ArticleRefCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<ArticleRefCreateOrConnectWithoutOwnerInput>>;
  connect?: Maybe<Array<ArticleRefWhereUniqueInput>>;
};

export type ArticleRefCreateOrConnectWithoutArticleInput = {
  where: ArticleRefWhereUniqueInput;
  create: ArticleRefCreateWithoutArticleInput;
};

export type ArticleRefCreateOrConnectWithoutFeedInput = {
  where: ArticleRefWhereUniqueInput;
  create: ArticleRefCreateWithoutFeedInput;
};

export type ArticleRefCreateOrConnectWithoutOwnerInput = {
  where: ArticleRefWhereUniqueInput;
  create: ArticleRefCreateWithoutOwnerInput;
};

export type ArticleRefCreateWithoutArticleInput = {
  id?: Maybe<Scalars['String']>;
  read?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  owner: UserCreateNestedOneWithoutArticleRefsInput;
  feed: FeedCreateNestedOneWithoutArticleRefInput;
};

export type ArticleRefCreateWithoutFeedInput = {
  id?: Maybe<Scalars['String']>;
  read?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  owner: UserCreateNestedOneWithoutArticleRefsInput;
  article: ArticleCreateNestedOneWithoutArticleRefInput;
};

export type ArticleRefCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  read?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  article: ArticleCreateNestedOneWithoutArticleRefInput;
  feed: FeedCreateNestedOneWithoutArticleRefInput;
};

export type ArticleRefGroupBy = {
  __typename?: 'ArticleRefGroupBy';
  id: FieldWrapper<Scalars['String']>;
  read: FieldWrapper<Scalars['Boolean']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  articleId: FieldWrapper<Scalars['String']>;
  feedId: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<ArticleRefCountAggregate>>;
  _min?: Maybe<FieldWrapper<ArticleRefMinAggregate>>;
  _max?: Maybe<FieldWrapper<ArticleRefMaxAggregate>>;
};

export type ArticleRefListRelationFilter = {
  every?: Maybe<ArticleRefWhereInput>;
  some?: Maybe<ArticleRefWhereInput>;
  none?: Maybe<ArticleRefWhereInput>;
};

export type ArticleRefMaxAggregate = {
  __typename?: 'ArticleRefMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  read?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  articleId?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type ArticleRefMinAggregate = {
  __typename?: 'ArticleRefMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  read?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  articleId?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type ArticleRefOrderByInput = {
  id?: Maybe<SortOrder>;
  read?: Maybe<SortOrder>;
  createdAt?: Maybe<SortOrder>;
  updatedAt?: Maybe<SortOrder>;
  ownerId?: Maybe<SortOrder>;
  articleId?: Maybe<SortOrder>;
  feedId?: Maybe<SortOrder>;
};

export enum ArticleRefScalarFieldEnum {
  Id = 'id',
  Read = 'read',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  OwnerId = 'ownerId',
  ArticleId = 'articleId',
  FeedId = 'feedId',
}

export type ArticleRefScalarWhereInput = {
  AND?: Maybe<Array<ArticleRefScalarWhereInput>>;
  OR?: Maybe<Array<ArticleRefScalarWhereInput>>;
  NOT?: Maybe<Array<ArticleRefScalarWhereInput>>;
  id?: Maybe<StringFilter>;
  read?: Maybe<BoolFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  ownerId?: Maybe<StringFilter>;
  articleId?: Maybe<StringFilter>;
  feedId?: Maybe<StringFilter>;
};

export type ArticleRefScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<ArticleRefScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<ArticleRefScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<ArticleRefScalarWhereWithAggregatesInput>>;
  id?: Maybe<StringWithAggregatesFilter>;
  read?: Maybe<BoolWithAggregatesFilter>;
  createdAt?: Maybe<DateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<DateTimeWithAggregatesFilter>;
  ownerId?: Maybe<StringWithAggregatesFilter>;
  articleId?: Maybe<StringWithAggregatesFilter>;
  feedId?: Maybe<StringWithAggregatesFilter>;
};

export type ArticleRefUpdateInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  read?: Maybe<BoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<UserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<ArticleUpdateOneRequiredWithoutArticleRefInput>;
  feed?: Maybe<FeedUpdateOneRequiredWithoutArticleRefInput>;
};

export type ArticleRefUpdateManyMutationInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  read?: Maybe<BoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
};

export type ArticleRefUpdateManyWithWhereWithoutArticleInput = {
  where: ArticleRefScalarWhereInput;
  data: ArticleRefUpdateManyMutationInput;
};

export type ArticleRefUpdateManyWithWhereWithoutFeedInput = {
  where: ArticleRefScalarWhereInput;
  data: ArticleRefUpdateManyMutationInput;
};

export type ArticleRefUpdateManyWithWhereWithoutOwnerInput = {
  where: ArticleRefScalarWhereInput;
  data: ArticleRefUpdateManyMutationInput;
};

export type ArticleRefUpdateManyWithoutArticleInput = {
  create?: Maybe<Array<ArticleRefCreateWithoutArticleInput>>;
  connectOrCreate?: Maybe<Array<ArticleRefCreateOrConnectWithoutArticleInput>>;
  upsert?: Maybe<Array<ArticleRefUpsertWithWhereUniqueWithoutArticleInput>>;
  connect?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  set?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  disconnect?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  delete?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  update?: Maybe<Array<ArticleRefUpdateWithWhereUniqueWithoutArticleInput>>;
  updateMany?: Maybe<Array<ArticleRefUpdateManyWithWhereWithoutArticleInput>>;
  deleteMany?: Maybe<Array<ArticleRefScalarWhereInput>>;
};

export type ArticleRefUpdateManyWithoutFeedInput = {
  create?: Maybe<Array<ArticleRefCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<ArticleRefCreateOrConnectWithoutFeedInput>>;
  upsert?: Maybe<Array<ArticleRefUpsertWithWhereUniqueWithoutFeedInput>>;
  connect?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  set?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  disconnect?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  delete?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  update?: Maybe<Array<ArticleRefUpdateWithWhereUniqueWithoutFeedInput>>;
  updateMany?: Maybe<Array<ArticleRefUpdateManyWithWhereWithoutFeedInput>>;
  deleteMany?: Maybe<Array<ArticleRefScalarWhereInput>>;
};

export type ArticleRefUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<ArticleRefCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<ArticleRefCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<ArticleRefUpsertWithWhereUniqueWithoutOwnerInput>>;
  connect?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  set?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  disconnect?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  delete?: Maybe<Array<ArticleRefWhereUniqueInput>>;
  update?: Maybe<Array<ArticleRefUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<ArticleRefUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<ArticleRefScalarWhereInput>>;
};

export type ArticleRefUpdateWithWhereUniqueWithoutArticleInput = {
  where: ArticleRefWhereUniqueInput;
  data: ArticleRefUpdateWithoutArticleInput;
};

export type ArticleRefUpdateWithWhereUniqueWithoutFeedInput = {
  where: ArticleRefWhereUniqueInput;
  data: ArticleRefUpdateWithoutFeedInput;
};

export type ArticleRefUpdateWithWhereUniqueWithoutOwnerInput = {
  where: ArticleRefWhereUniqueInput;
  data: ArticleRefUpdateWithoutOwnerInput;
};

export type ArticleRefUpdateWithoutArticleInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  read?: Maybe<BoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<UserUpdateOneRequiredWithoutArticleRefsInput>;
  feed?: Maybe<FeedUpdateOneRequiredWithoutArticleRefInput>;
};

export type ArticleRefUpdateWithoutFeedInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  read?: Maybe<BoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<UserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<ArticleUpdateOneRequiredWithoutArticleRefInput>;
};

export type ArticleRefUpdateWithoutOwnerInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  read?: Maybe<BoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  article?: Maybe<ArticleUpdateOneRequiredWithoutArticleRefInput>;
  feed?: Maybe<FeedUpdateOneRequiredWithoutArticleRefInput>;
};

export type ArticleRefUpsertWithWhereUniqueWithoutArticleInput = {
  where: ArticleRefWhereUniqueInput;
  update: ArticleRefUpdateWithoutArticleInput;
  create: ArticleRefCreateWithoutArticleInput;
};

export type ArticleRefUpsertWithWhereUniqueWithoutFeedInput = {
  where: ArticleRefWhereUniqueInput;
  update: ArticleRefUpdateWithoutFeedInput;
  create: ArticleRefCreateWithoutFeedInput;
};

export type ArticleRefUpsertWithWhereUniqueWithoutOwnerInput = {
  where: ArticleRefWhereUniqueInput;
  update: ArticleRefUpdateWithoutOwnerInput;
  create: ArticleRefCreateWithoutOwnerInput;
};

export type ArticleRefWhereInput = {
  AND?: Maybe<Array<ArticleRefWhereInput>>;
  OR?: Maybe<Array<ArticleRefWhereInput>>;
  NOT?: Maybe<Array<ArticleRefWhereInput>>;
  id?: Maybe<StringFilter>;
  read?: Maybe<BoolFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  owner?: Maybe<UserRelationFilter>;
  ownerId?: Maybe<StringFilter>;
  article?: Maybe<ArticleRelationFilter>;
  articleId?: Maybe<StringFilter>;
  feed?: Maybe<FeedRelationFilter>;
  feedId?: Maybe<StringFilter>;
};

export type ArticleRefWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type ArticleRelationFilter = {
  is?: Maybe<ArticleWhereInput>;
  isNot?: Maybe<ArticleWhereInput>;
};

export enum ArticleScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Url = 'url',
  Title = 'title',
  Content = 'content',
  ContentHtml = 'contentHtml',
  FeedId = 'feedId',
}

export type ArticleScalarWhereInput = {
  AND?: Maybe<Array<ArticleScalarWhereInput>>;
  OR?: Maybe<Array<ArticleScalarWhereInput>>;
  NOT?: Maybe<Array<ArticleScalarWhereInput>>;
  id?: Maybe<StringFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  url?: Maybe<StringFilter>;
  title?: Maybe<StringFilter>;
  content?: Maybe<StringFilter>;
  contentHtml?: Maybe<StringFilter>;
  feedId?: Maybe<StringNullableFilter>;
};

export type ArticleScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<ArticleScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<ArticleScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<ArticleScalarWhereWithAggregatesInput>>;
  id?: Maybe<StringWithAggregatesFilter>;
  createdAt?: Maybe<DateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<DateTimeWithAggregatesFilter>;
  url?: Maybe<StringWithAggregatesFilter>;
  title?: Maybe<StringWithAggregatesFilter>;
  content?: Maybe<StringWithAggregatesFilter>;
  contentHtml?: Maybe<StringWithAggregatesFilter>;
  feedId?: Maybe<StringNullableWithAggregatesFilter>;
};

export type ArticleUpdateInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  url?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  content?: Maybe<StringFieldUpdateOperationsInput>;
  contentHtml?: Maybe<StringFieldUpdateOperationsInput>;
  ArticleRef?: Maybe<ArticleRefUpdateManyWithoutArticleInput>;
  Feed?: Maybe<FeedUpdateOneWithoutArticlesInput>;
};

export type ArticleUpdateManyMutationInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  url?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  content?: Maybe<StringFieldUpdateOperationsInput>;
  contentHtml?: Maybe<StringFieldUpdateOperationsInput>;
};

export type ArticleUpdateManyWithWhereWithoutFeedInput = {
  where: ArticleScalarWhereInput;
  data: ArticleUpdateManyMutationInput;
};

export type ArticleUpdateManyWithoutFeedInput = {
  create?: Maybe<Array<ArticleCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<ArticleCreateOrConnectWithoutFeedInput>>;
  upsert?: Maybe<Array<ArticleUpsertWithWhereUniqueWithoutFeedInput>>;
  connect?: Maybe<Array<ArticleWhereUniqueInput>>;
  set?: Maybe<Array<ArticleWhereUniqueInput>>;
  disconnect?: Maybe<Array<ArticleWhereUniqueInput>>;
  delete?: Maybe<Array<ArticleWhereUniqueInput>>;
  update?: Maybe<Array<ArticleUpdateWithWhereUniqueWithoutFeedInput>>;
  updateMany?: Maybe<Array<ArticleUpdateManyWithWhereWithoutFeedInput>>;
  deleteMany?: Maybe<Array<ArticleScalarWhereInput>>;
};

export type ArticleUpdateOneRequiredWithoutArticleRefInput = {
  create?: Maybe<ArticleCreateWithoutArticleRefInput>;
  connectOrCreate?: Maybe<ArticleCreateOrConnectWithoutArticleRefInput>;
  upsert?: Maybe<ArticleUpsertWithoutArticleRefInput>;
  connect?: Maybe<ArticleWhereUniqueInput>;
  update?: Maybe<ArticleUpdateWithoutArticleRefInput>;
};

export type ArticleUpdateWithWhereUniqueWithoutFeedInput = {
  where: ArticleWhereUniqueInput;
  data: ArticleUpdateWithoutFeedInput;
};

export type ArticleUpdateWithoutArticleRefInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  url?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  content?: Maybe<StringFieldUpdateOperationsInput>;
  contentHtml?: Maybe<StringFieldUpdateOperationsInput>;
  Feed?: Maybe<FeedUpdateOneWithoutArticlesInput>;
};

export type ArticleUpdateWithoutFeedInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  url?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  content?: Maybe<StringFieldUpdateOperationsInput>;
  contentHtml?: Maybe<StringFieldUpdateOperationsInput>;
  ArticleRef?: Maybe<ArticleRefUpdateManyWithoutArticleInput>;
};

export type ArticleUpsertWithWhereUniqueWithoutFeedInput = {
  where: ArticleWhereUniqueInput;
  update: ArticleUpdateWithoutFeedInput;
  create: ArticleCreateWithoutFeedInput;
};

export type ArticleUpsertWithoutArticleRefInput = {
  update: ArticleUpdateWithoutArticleRefInput;
  create: ArticleCreateWithoutArticleRefInput;
};

export type ArticleWhereInput = {
  AND?: Maybe<Array<ArticleWhereInput>>;
  OR?: Maybe<Array<ArticleWhereInput>>;
  NOT?: Maybe<Array<ArticleWhereInput>>;
  id?: Maybe<StringFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  url?: Maybe<StringFilter>;
  title?: Maybe<StringFilter>;
  content?: Maybe<StringFilter>;
  contentHtml?: Maybe<StringFilter>;
  ArticleRef?: Maybe<ArticleRefListRelationFilter>;
  Feed?: Maybe<FeedRelationFilter>;
  feedId?: Maybe<StringNullableFilter>;
};

export type ArticleWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type BoolFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['Boolean']>;
};

export type BoolFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<NestedBoolFilter>;
};

export type BoolWithAggregatesFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<NestedBoolWithAggregatesFilter>;
  _count?: Maybe<NestedIntFilter>;
  _min?: Maybe<NestedBoolFilter>;
  _max?: Maybe<NestedBoolFilter>;
};

export type Bucket = {
  __typename?: 'Bucket';
  id: FieldWrapper<Scalars['String']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed: FieldWrapper<Scalars['Boolean']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  title: FieldWrapper<Scalars['String']>;
  ownerId: FieldWrapper<Scalars['String']>;
  owner: FieldWrapper<User>;
  subscriptions: Array<FieldWrapper<Subscription>>;
};

export type BucketSubscriptionsArgs = {
  where?: Maybe<SubscriptionWhereInput>;
  orderBy?: Maybe<Array<SubscriptionOrderByInput>>;
  cursor?: Maybe<SubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<SubscriptionScalarFieldEnum>>;
};

export type BucketCountAggregate = {
  __typename?: 'BucketCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  description: FieldWrapper<Scalars['Int']>;
  listed: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type BucketCreateInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  owner: UserCreateNestedOneWithoutBucketsInput;
  subscriptions?: Maybe<SubscriptionCreateNestedManyWithoutBucketInput>;
};

export type BucketCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<BucketCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<BucketCreateOrConnectWithoutOwnerInput>>;
  connect?: Maybe<Array<BucketWhereUniqueInput>>;
};

export type BucketCreateNestedOneWithoutSubscriptionsInput = {
  create?: Maybe<BucketCreateWithoutSubscriptionsInput>;
  connectOrCreate?: Maybe<BucketCreateOrConnectWithoutSubscriptionsInput>;
  connect?: Maybe<BucketWhereUniqueInput>;
};

export type BucketCreateOrConnectWithoutOwnerInput = {
  where: BucketWhereUniqueInput;
  create: BucketCreateWithoutOwnerInput;
};

export type BucketCreateOrConnectWithoutSubscriptionsInput = {
  where: BucketWhereUniqueInput;
  create: BucketCreateWithoutSubscriptionsInput;
};

export type BucketCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  subscriptions?: Maybe<SubscriptionCreateNestedManyWithoutBucketInput>;
};

export type BucketCreateWithoutSubscriptionsInput = {
  id?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  owner: UserCreateNestedOneWithoutBucketsInput;
};

export type BucketGroupBy = {
  __typename?: 'BucketGroupBy';
  id: FieldWrapper<Scalars['String']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed: FieldWrapper<Scalars['Boolean']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  title: FieldWrapper<Scalars['String']>;
  ownerId: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<BucketCountAggregate>>;
  _min?: Maybe<FieldWrapper<BucketMinAggregate>>;
  _max?: Maybe<FieldWrapper<BucketMaxAggregate>>;
};

export type BucketListRelationFilter = {
  every?: Maybe<BucketWhereInput>;
  some?: Maybe<BucketWhereInput>;
  none?: Maybe<BucketWhereInput>;
};

export type BucketMaxAggregate = {
  __typename?: 'BucketMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type BucketMinAggregate = {
  __typename?: 'BucketMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type BucketOrderByInput = {
  id?: Maybe<SortOrder>;
  description?: Maybe<SortOrder>;
  listed?: Maybe<SortOrder>;
  createdAt?: Maybe<SortOrder>;
  updatedAt?: Maybe<SortOrder>;
  title?: Maybe<SortOrder>;
  ownerId?: Maybe<SortOrder>;
};

export type BucketRelationFilter = {
  is?: Maybe<BucketWhereInput>;
  isNot?: Maybe<BucketWhereInput>;
};

export enum BucketScalarFieldEnum {
  Id = 'id',
  Description = 'description',
  Listed = 'listed',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Title = 'title',
  OwnerId = 'ownerId',
}

export type BucketScalarWhereInput = {
  AND?: Maybe<Array<BucketScalarWhereInput>>;
  OR?: Maybe<Array<BucketScalarWhereInput>>;
  NOT?: Maybe<Array<BucketScalarWhereInput>>;
  id?: Maybe<StringFilter>;
  description?: Maybe<StringNullableFilter>;
  listed?: Maybe<BoolFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  title?: Maybe<StringFilter>;
  ownerId?: Maybe<StringFilter>;
};

export type BucketScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<BucketScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<BucketScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<BucketScalarWhereWithAggregatesInput>>;
  id?: Maybe<StringWithAggregatesFilter>;
  description?: Maybe<StringNullableWithAggregatesFilter>;
  listed?: Maybe<BoolWithAggregatesFilter>;
  createdAt?: Maybe<DateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<DateTimeWithAggregatesFilter>;
  title?: Maybe<StringWithAggregatesFilter>;
  ownerId?: Maybe<StringWithAggregatesFilter>;
};

export type BucketUpdateInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<NullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<BoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  owner?: Maybe<UserUpdateOneRequiredWithoutBucketsInput>;
  subscriptions?: Maybe<SubscriptionUpdateManyWithoutBucketInput>;
};

export type BucketUpdateManyMutationInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<NullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<BoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
};

export type BucketUpdateManyWithWhereWithoutOwnerInput = {
  where: BucketScalarWhereInput;
  data: BucketUpdateManyMutationInput;
};

export type BucketUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<BucketCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<BucketCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<BucketUpsertWithWhereUniqueWithoutOwnerInput>>;
  connect?: Maybe<Array<BucketWhereUniqueInput>>;
  set?: Maybe<Array<BucketWhereUniqueInput>>;
  disconnect?: Maybe<Array<BucketWhereUniqueInput>>;
  delete?: Maybe<Array<BucketWhereUniqueInput>>;
  update?: Maybe<Array<BucketUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<BucketUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<BucketScalarWhereInput>>;
};

export type BucketUpdateOneRequiredWithoutSubscriptionsInput = {
  create?: Maybe<BucketCreateWithoutSubscriptionsInput>;
  connectOrCreate?: Maybe<BucketCreateOrConnectWithoutSubscriptionsInput>;
  upsert?: Maybe<BucketUpsertWithoutSubscriptionsInput>;
  connect?: Maybe<BucketWhereUniqueInput>;
  update?: Maybe<BucketUpdateWithoutSubscriptionsInput>;
};

export type BucketUpdateWithWhereUniqueWithoutOwnerInput = {
  where: BucketWhereUniqueInput;
  data: BucketUpdateWithoutOwnerInput;
};

export type BucketUpdateWithoutOwnerInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<NullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<BoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  subscriptions?: Maybe<SubscriptionUpdateManyWithoutBucketInput>;
};

export type BucketUpdateWithoutSubscriptionsInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<NullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<BoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  owner?: Maybe<UserUpdateOneRequiredWithoutBucketsInput>;
};

export type BucketUpsertWithWhereUniqueWithoutOwnerInput = {
  where: BucketWhereUniqueInput;
  update: BucketUpdateWithoutOwnerInput;
  create: BucketCreateWithoutOwnerInput;
};

export type BucketUpsertWithoutSubscriptionsInput = {
  update: BucketUpdateWithoutSubscriptionsInput;
  create: BucketCreateWithoutSubscriptionsInput;
};

export type BucketWhereInput = {
  AND?: Maybe<Array<BucketWhereInput>>;
  OR?: Maybe<Array<BucketWhereInput>>;
  NOT?: Maybe<Array<BucketWhereInput>>;
  id?: Maybe<StringFilter>;
  description?: Maybe<StringNullableFilter>;
  listed?: Maybe<BoolFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  title?: Maybe<StringFilter>;
  owner?: Maybe<UserRelationFilter>;
  ownerId?: Maybe<StringFilter>;
  subscriptions?: Maybe<SubscriptionListRelationFilter>;
};

export type BucketWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type DateTimeFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['DateTime']>;
};

export type DateTimeFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<NestedDateTimeFilter>;
};

export type DateTimeWithAggregatesFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<NestedDateTimeWithAggregatesFilter>;
  _count?: Maybe<NestedIntFilter>;
  _min?: Maybe<NestedDateTimeFilter>;
  _max?: Maybe<NestedDateTimeFilter>;
};

export type DiscoveredFeed = {
  __typename?: 'DiscoveredFeed';
  id: FieldWrapper<Scalars['String']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  url: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  description: FieldWrapper<Scalars['String']>;
  type: FieldWrapper<Scalars['String']>;
};

export type Feed = {
  __typename?: 'Feed';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  url: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  description: FieldWrapper<Scalars['String']>;
  articles: Array<FieldWrapper<Article>>;
  subscriptions: Array<FieldWrapper<Subscription>>;
  ArticleRef: Array<FieldWrapper<ArticleRef>>;
};

export type FeedArticlesArgs = {
  where?: Maybe<ArticleWhereInput>;
  orderBy?: Maybe<Array<ArticleOrderByInput>>;
  cursor?: Maybe<ArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<ArticleScalarFieldEnum>>;
};

export type FeedSubscriptionsArgs = {
  where?: Maybe<SubscriptionWhereInput>;
  orderBy?: Maybe<Array<SubscriptionOrderByInput>>;
  cursor?: Maybe<SubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<SubscriptionScalarFieldEnum>>;
};

export type FeedArticleRefArgs = {
  where?: Maybe<ArticleRefWhereInput>;
  orderBy?: Maybe<Array<ArticleRefOrderByInput>>;
  cursor?: Maybe<ArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<ArticleRefScalarFieldEnum>>;
};

export type FeedCountAggregate = {
  __typename?: 'FeedCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  url: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  description: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type FeedCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  url: Scalars['String'];
  title: Scalars['String'];
  description: Scalars['String'];
  articles?: Maybe<ArticleCreateNestedManyWithoutFeedInput>;
  subscriptions?: Maybe<SubscriptionCreateNestedManyWithoutFeedInput>;
  ArticleRef?: Maybe<ArticleRefCreateNestedManyWithoutFeedInput>;
};

export type FeedCreateNestedOneWithoutArticleRefInput = {
  create?: Maybe<FeedCreateWithoutArticleRefInput>;
  connectOrCreate?: Maybe<FeedCreateOrConnectWithoutArticleRefInput>;
  connect?: Maybe<FeedWhereUniqueInput>;
};

export type FeedCreateNestedOneWithoutArticlesInput = {
  create?: Maybe<FeedCreateWithoutArticlesInput>;
  connectOrCreate?: Maybe<FeedCreateOrConnectWithoutArticlesInput>;
  connect?: Maybe<FeedWhereUniqueInput>;
};

export type FeedCreateNestedOneWithoutSubscriptionsInput = {
  create?: Maybe<FeedCreateWithoutSubscriptionsInput>;
  connectOrCreate?: Maybe<FeedCreateOrConnectWithoutSubscriptionsInput>;
  connect?: Maybe<FeedWhereUniqueInput>;
};

export type FeedCreateOrConnectWithoutArticleRefInput = {
  where: FeedWhereUniqueInput;
  create: FeedCreateWithoutArticleRefInput;
};

export type FeedCreateOrConnectWithoutArticlesInput = {
  where: FeedWhereUniqueInput;
  create: FeedCreateWithoutArticlesInput;
};

export type FeedCreateOrConnectWithoutSubscriptionsInput = {
  where: FeedWhereUniqueInput;
  create: FeedCreateWithoutSubscriptionsInput;
};

export type FeedCreateWithoutArticleRefInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  url: Scalars['String'];
  title: Scalars['String'];
  description: Scalars['String'];
  articles?: Maybe<ArticleCreateNestedManyWithoutFeedInput>;
  subscriptions?: Maybe<SubscriptionCreateNestedManyWithoutFeedInput>;
};

export type FeedCreateWithoutArticlesInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  url: Scalars['String'];
  title: Scalars['String'];
  description: Scalars['String'];
  subscriptions?: Maybe<SubscriptionCreateNestedManyWithoutFeedInput>;
  ArticleRef?: Maybe<ArticleRefCreateNestedManyWithoutFeedInput>;
};

export type FeedCreateWithoutSubscriptionsInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  url: Scalars['String'];
  title: Scalars['String'];
  description: Scalars['String'];
  articles?: Maybe<ArticleCreateNestedManyWithoutFeedInput>;
  ArticleRef?: Maybe<ArticleRefCreateNestedManyWithoutFeedInput>;
};

export type FeedGroupBy = {
  __typename?: 'FeedGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  url: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  description: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<FeedCountAggregate>>;
  _min?: Maybe<FieldWrapper<FeedMinAggregate>>;
  _max?: Maybe<FieldWrapper<FeedMaxAggregate>>;
};

export type FeedMaxAggregate = {
  __typename?: 'FeedMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type FeedMinAggregate = {
  __typename?: 'FeedMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type FeedOrderByInput = {
  id?: Maybe<SortOrder>;
  createdAt?: Maybe<SortOrder>;
  updatedAt?: Maybe<SortOrder>;
  url?: Maybe<SortOrder>;
  title?: Maybe<SortOrder>;
  description?: Maybe<SortOrder>;
};

export type FeedRelationFilter = {
  is?: Maybe<FeedWhereInput>;
  isNot?: Maybe<FeedWhereInput>;
};

export enum FeedScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Url = 'url',
  Title = 'title',
  Description = 'description',
}

export type FeedScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<FeedScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<FeedScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<FeedScalarWhereWithAggregatesInput>>;
  id?: Maybe<StringWithAggregatesFilter>;
  createdAt?: Maybe<DateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<DateTimeWithAggregatesFilter>;
  url?: Maybe<StringWithAggregatesFilter>;
  title?: Maybe<StringWithAggregatesFilter>;
  description?: Maybe<StringWithAggregatesFilter>;
};

export type FeedUpdateInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  url?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<StringFieldUpdateOperationsInput>;
  articles?: Maybe<ArticleUpdateManyWithoutFeedInput>;
  subscriptions?: Maybe<SubscriptionUpdateManyWithoutFeedInput>;
  ArticleRef?: Maybe<ArticleRefUpdateManyWithoutFeedInput>;
};

export type FeedUpdateManyMutationInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  url?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<StringFieldUpdateOperationsInput>;
};

export type FeedUpdateOneRequiredWithoutArticleRefInput = {
  create?: Maybe<FeedCreateWithoutArticleRefInput>;
  connectOrCreate?: Maybe<FeedCreateOrConnectWithoutArticleRefInput>;
  upsert?: Maybe<FeedUpsertWithoutArticleRefInput>;
  connect?: Maybe<FeedWhereUniqueInput>;
  update?: Maybe<FeedUpdateWithoutArticleRefInput>;
};

export type FeedUpdateOneRequiredWithoutSubscriptionsInput = {
  create?: Maybe<FeedCreateWithoutSubscriptionsInput>;
  connectOrCreate?: Maybe<FeedCreateOrConnectWithoutSubscriptionsInput>;
  upsert?: Maybe<FeedUpsertWithoutSubscriptionsInput>;
  connect?: Maybe<FeedWhereUniqueInput>;
  update?: Maybe<FeedUpdateWithoutSubscriptionsInput>;
};

export type FeedUpdateOneWithoutArticlesInput = {
  create?: Maybe<FeedCreateWithoutArticlesInput>;
  connectOrCreate?: Maybe<FeedCreateOrConnectWithoutArticlesInput>;
  upsert?: Maybe<FeedUpsertWithoutArticlesInput>;
  connect?: Maybe<FeedWhereUniqueInput>;
  disconnect?: Maybe<Scalars['Boolean']>;
  delete?: Maybe<Scalars['Boolean']>;
  update?: Maybe<FeedUpdateWithoutArticlesInput>;
};

export type FeedUpdateWithoutArticleRefInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  url?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<StringFieldUpdateOperationsInput>;
  articles?: Maybe<ArticleUpdateManyWithoutFeedInput>;
  subscriptions?: Maybe<SubscriptionUpdateManyWithoutFeedInput>;
};

export type FeedUpdateWithoutArticlesInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  url?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<StringFieldUpdateOperationsInput>;
  subscriptions?: Maybe<SubscriptionUpdateManyWithoutFeedInput>;
  ArticleRef?: Maybe<ArticleRefUpdateManyWithoutFeedInput>;
};

export type FeedUpdateWithoutSubscriptionsInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  url?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<StringFieldUpdateOperationsInput>;
  articles?: Maybe<ArticleUpdateManyWithoutFeedInput>;
  ArticleRef?: Maybe<ArticleRefUpdateManyWithoutFeedInput>;
};

export type FeedUpsertWithoutArticleRefInput = {
  update: FeedUpdateWithoutArticleRefInput;
  create: FeedCreateWithoutArticleRefInput;
};

export type FeedUpsertWithoutArticlesInput = {
  update: FeedUpdateWithoutArticlesInput;
  create: FeedCreateWithoutArticlesInput;
};

export type FeedUpsertWithoutSubscriptionsInput = {
  update: FeedUpdateWithoutSubscriptionsInput;
  create: FeedCreateWithoutSubscriptionsInput;
};

export type FeedWhereInput = {
  AND?: Maybe<Array<FeedWhereInput>>;
  OR?: Maybe<Array<FeedWhereInput>>;
  NOT?: Maybe<Array<FeedWhereInput>>;
  id?: Maybe<StringFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  url?: Maybe<StringFilter>;
  title?: Maybe<StringFilter>;
  description?: Maybe<StringFilter>;
  articles?: Maybe<ArticleListRelationFilter>;
  subscriptions?: Maybe<SubscriptionListRelationFilter>;
  ArticleRef?: Maybe<ArticleRefListRelationFilter>;
};

export type FeedWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type Mutation = {
  __typename?: 'Mutation';
  createArticle: FieldWrapper<Article>;
  deleteArticle?: Maybe<FieldWrapper<Article>>;
  updateArticle?: Maybe<FieldWrapper<Article>>;
  deleteManyArticle: FieldWrapper<AffectedRowsOutput>;
  updateManyArticle: FieldWrapper<AffectedRowsOutput>;
  upsertArticle: FieldWrapper<Article>;
  createArticleFilter: FieldWrapper<ArticleFilter>;
  deleteArticleFilter?: Maybe<FieldWrapper<ArticleFilter>>;
  updateArticleFilter?: Maybe<FieldWrapper<ArticleFilter>>;
  deleteManyArticleFilter: FieldWrapper<AffectedRowsOutput>;
  updateManyArticleFilter: FieldWrapper<AffectedRowsOutput>;
  upsertArticleFilter: FieldWrapper<ArticleFilter>;
  createArticleRef: FieldWrapper<ArticleRef>;
  deleteArticleRef?: Maybe<FieldWrapper<ArticleRef>>;
  updateArticleRef?: Maybe<FieldWrapper<ArticleRef>>;
  deleteManyArticleRef: FieldWrapper<AffectedRowsOutput>;
  updateManyArticleRef: FieldWrapper<AffectedRowsOutput>;
  upsertArticleRef: FieldWrapper<ArticleRef>;
  createBucket: FieldWrapper<Bucket>;
  deleteBucket?: Maybe<FieldWrapper<Bucket>>;
  updateBucket?: Maybe<FieldWrapper<Bucket>>;
  deleteManyBucket: FieldWrapper<AffectedRowsOutput>;
  updateManyBucket: FieldWrapper<AffectedRowsOutput>;
  upsertBucket: FieldWrapper<Bucket>;
  createFeed: FieldWrapper<Feed>;
  deleteFeed?: Maybe<FieldWrapper<Feed>>;
  updateFeed?: Maybe<FieldWrapper<Feed>>;
  deleteManyFeed: FieldWrapper<AffectedRowsOutput>;
  updateManyFeed: FieldWrapper<AffectedRowsOutput>;
  upsertFeed: FieldWrapper<Feed>;
  createSubscription: FieldWrapper<Subscription>;
  deleteSubscription?: Maybe<FieldWrapper<Subscription>>;
  updateSubscription?: Maybe<FieldWrapper<Subscription>>;
  deleteManySubscription: FieldWrapper<AffectedRowsOutput>;
  updateManySubscription: FieldWrapper<AffectedRowsOutput>;
  upsertSubscription: FieldWrapper<Subscription>;
  createUser: FieldWrapper<User>;
  deleteUser?: Maybe<FieldWrapper<User>>;
  updateUser?: Maybe<FieldWrapper<User>>;
  deleteManyUser: FieldWrapper<AffectedRowsOutput>;
  updateManyUser: FieldWrapper<AffectedRowsOutput>;
  upsertUser: FieldWrapper<User>;
  createUserArticle: FieldWrapper<UserArticle>;
  deleteUserArticle?: Maybe<FieldWrapper<UserArticle>>;
  updateUserArticle?: Maybe<FieldWrapper<UserArticle>>;
  deleteManyUserArticle: FieldWrapper<AffectedRowsOutput>;
  updateManyUserArticle: FieldWrapper<AffectedRowsOutput>;
  upsertUserArticle: FieldWrapper<UserArticle>;
  createUserFeed: FieldWrapper<UserFeed>;
  deleteUserFeed?: Maybe<FieldWrapper<UserFeed>>;
  updateUserFeed?: Maybe<FieldWrapper<UserFeed>>;
  deleteManyUserFeed: FieldWrapper<AffectedRowsOutput>;
  updateManyUserFeed: FieldWrapper<AffectedRowsOutput>;
  upsertUserFeed: FieldWrapper<UserFeed>;
};

export type MutationCreateArticleArgs = {
  data: ArticleCreateInput;
};

export type MutationDeleteArticleArgs = {
  where: ArticleWhereUniqueInput;
};

export type MutationUpdateArticleArgs = {
  data: ArticleUpdateInput;
  where: ArticleWhereUniqueInput;
};

export type MutationDeleteManyArticleArgs = {
  where?: Maybe<ArticleWhereInput>;
};

export type MutationUpdateManyArticleArgs = {
  data: ArticleUpdateManyMutationInput;
  where?: Maybe<ArticleWhereInput>;
};

export type MutationUpsertArticleArgs = {
  where: ArticleWhereUniqueInput;
  create: ArticleCreateInput;
  update: ArticleUpdateInput;
};

export type MutationCreateArticleFilterArgs = {
  data: ArticleFilterCreateInput;
};

export type MutationDeleteArticleFilterArgs = {
  where: ArticleFilterWhereUniqueInput;
};

export type MutationUpdateArticleFilterArgs = {
  data: ArticleFilterUpdateInput;
  where: ArticleFilterWhereUniqueInput;
};

export type MutationDeleteManyArticleFilterArgs = {
  where?: Maybe<ArticleFilterWhereInput>;
};

export type MutationUpdateManyArticleFilterArgs = {
  data: ArticleFilterUpdateManyMutationInput;
  where?: Maybe<ArticleFilterWhereInput>;
};

export type MutationUpsertArticleFilterArgs = {
  where: ArticleFilterWhereUniqueInput;
  create: ArticleFilterCreateInput;
  update: ArticleFilterUpdateInput;
};

export type MutationCreateArticleRefArgs = {
  data: ArticleRefCreateInput;
};

export type MutationDeleteArticleRefArgs = {
  where: ArticleRefWhereUniqueInput;
};

export type MutationUpdateArticleRefArgs = {
  data: ArticleRefUpdateInput;
  where: ArticleRefWhereUniqueInput;
};

export type MutationDeleteManyArticleRefArgs = {
  where?: Maybe<ArticleRefWhereInput>;
};

export type MutationUpdateManyArticleRefArgs = {
  data: ArticleRefUpdateManyMutationInput;
  where?: Maybe<ArticleRefWhereInput>;
};

export type MutationUpsertArticleRefArgs = {
  where: ArticleRefWhereUniqueInput;
  create: ArticleRefCreateInput;
  update: ArticleRefUpdateInput;
};

export type MutationCreateBucketArgs = {
  data: BucketCreateInput;
};

export type MutationDeleteBucketArgs = {
  where: BucketWhereUniqueInput;
};

export type MutationUpdateBucketArgs = {
  data: BucketUpdateInput;
  where: BucketWhereUniqueInput;
};

export type MutationDeleteManyBucketArgs = {
  where?: Maybe<BucketWhereInput>;
};

export type MutationUpdateManyBucketArgs = {
  data: BucketUpdateManyMutationInput;
  where?: Maybe<BucketWhereInput>;
};

export type MutationUpsertBucketArgs = {
  where: BucketWhereUniqueInput;
  create: BucketCreateInput;
  update: BucketUpdateInput;
};

export type MutationCreateFeedArgs = {
  data: FeedCreateInput;
};

export type MutationDeleteFeedArgs = {
  where: FeedWhereUniqueInput;
};

export type MutationUpdateFeedArgs = {
  data: FeedUpdateInput;
  where: FeedWhereUniqueInput;
};

export type MutationDeleteManyFeedArgs = {
  where?: Maybe<FeedWhereInput>;
};

export type MutationUpdateManyFeedArgs = {
  data: FeedUpdateManyMutationInput;
  where?: Maybe<FeedWhereInput>;
};

export type MutationUpsertFeedArgs = {
  where: FeedWhereUniqueInput;
  create: FeedCreateInput;
  update: FeedUpdateInput;
};

export type MutationCreateSubscriptionArgs = {
  data: SubscriptionCreateInput;
};

export type MutationDeleteSubscriptionArgs = {
  where: SubscriptionWhereUniqueInput;
};

export type MutationUpdateSubscriptionArgs = {
  data: SubscriptionUpdateInput;
  where: SubscriptionWhereUniqueInput;
};

export type MutationDeleteManySubscriptionArgs = {
  where?: Maybe<SubscriptionWhereInput>;
};

export type MutationUpdateManySubscriptionArgs = {
  data: SubscriptionUpdateManyMutationInput;
  where?: Maybe<SubscriptionWhereInput>;
};

export type MutationUpsertSubscriptionArgs = {
  where: SubscriptionWhereUniqueInput;
  create: SubscriptionCreateInput;
  update: SubscriptionUpdateInput;
};

export type MutationCreateUserArgs = {
  data: UserCreateInput;
};

export type MutationDeleteUserArgs = {
  where: UserWhereUniqueInput;
};

export type MutationUpdateUserArgs = {
  data: UserUpdateInput;
  where: UserWhereUniqueInput;
};

export type MutationDeleteManyUserArgs = {
  where?: Maybe<UserWhereInput>;
};

export type MutationUpdateManyUserArgs = {
  data: UserUpdateManyMutationInput;
  where?: Maybe<UserWhereInput>;
};

export type MutationUpsertUserArgs = {
  where: UserWhereUniqueInput;
  create: UserCreateInput;
  update: UserUpdateInput;
};

export type MutationCreateUserArticleArgs = {
  data: UserArticleCreateInput;
};

export type MutationDeleteUserArticleArgs = {
  where: UserArticleWhereUniqueInput;
};

export type MutationUpdateUserArticleArgs = {
  data: UserArticleUpdateInput;
  where: UserArticleWhereUniqueInput;
};

export type MutationDeleteManyUserArticleArgs = {
  where?: Maybe<UserArticleWhereInput>;
};

export type MutationUpdateManyUserArticleArgs = {
  data: UserArticleUpdateManyMutationInput;
  where?: Maybe<UserArticleWhereInput>;
};

export type MutationUpsertUserArticleArgs = {
  where: UserArticleWhereUniqueInput;
  create: UserArticleCreateInput;
  update: UserArticleUpdateInput;
};

export type MutationCreateUserFeedArgs = {
  data: UserFeedCreateInput;
};

export type MutationDeleteUserFeedArgs = {
  where: UserFeedWhereUniqueInput;
};

export type MutationUpdateUserFeedArgs = {
  data: UserFeedUpdateInput;
  where: UserFeedWhereUniqueInput;
};

export type MutationDeleteManyUserFeedArgs = {
  where?: Maybe<UserFeedWhereInput>;
};

export type MutationUpdateManyUserFeedArgs = {
  data: UserFeedUpdateManyMutationInput;
  where?: Maybe<UserFeedWhereInput>;
};

export type MutationUpsertUserFeedArgs = {
  where: UserFeedWhereUniqueInput;
  create: UserFeedCreateInput;
  update: UserFeedUpdateInput;
};

export type NestedBoolFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<NestedBoolFilter>;
};

export type NestedBoolWithAggregatesFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<NestedBoolWithAggregatesFilter>;
  _count?: Maybe<NestedIntFilter>;
  _min?: Maybe<NestedBoolFilter>;
  _max?: Maybe<NestedBoolFilter>;
};

export type NestedDateTimeFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<NestedDateTimeFilter>;
};

export type NestedDateTimeWithAggregatesFilter = {
  equals?: Maybe<Scalars['DateTime']>;
  in?: Maybe<Array<Scalars['DateTime']>>;
  notIn?: Maybe<Array<Scalars['DateTime']>>;
  lt?: Maybe<Scalars['DateTime']>;
  lte?: Maybe<Scalars['DateTime']>;
  gt?: Maybe<Scalars['DateTime']>;
  gte?: Maybe<Scalars['DateTime']>;
  not?: Maybe<NestedDateTimeWithAggregatesFilter>;
  _count?: Maybe<NestedIntFilter>;
  _min?: Maybe<NestedDateTimeFilter>;
  _max?: Maybe<NestedDateTimeFilter>;
};

export type NestedIntFilter = {
  equals?: Maybe<Scalars['Int']>;
  in?: Maybe<Array<Scalars['Int']>>;
  notIn?: Maybe<Array<Scalars['Int']>>;
  lt?: Maybe<Scalars['Int']>;
  lte?: Maybe<Scalars['Int']>;
  gt?: Maybe<Scalars['Int']>;
  gte?: Maybe<Scalars['Int']>;
  not?: Maybe<NestedIntFilter>;
};

export type NestedIntNullableFilter = {
  equals?: Maybe<Scalars['Int']>;
  in?: Maybe<Array<Scalars['Int']>>;
  notIn?: Maybe<Array<Scalars['Int']>>;
  lt?: Maybe<Scalars['Int']>;
  lte?: Maybe<Scalars['Int']>;
  gt?: Maybe<Scalars['Int']>;
  gte?: Maybe<Scalars['Int']>;
  not?: Maybe<NestedIntNullableFilter>;
};

export type NestedStringFilter = {
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
  not?: Maybe<NestedStringFilter>;
};

export type NestedStringNullableFilter = {
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
  not?: Maybe<NestedStringNullableFilter>;
};

export type NestedStringNullableWithAggregatesFilter = {
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
  not?: Maybe<NestedStringNullableWithAggregatesFilter>;
  _count?: Maybe<NestedIntNullableFilter>;
  _min?: Maybe<NestedStringNullableFilter>;
  _max?: Maybe<NestedStringNullableFilter>;
};

export type NestedStringWithAggregatesFilter = {
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
  not?: Maybe<NestedStringWithAggregatesFilter>;
  _count?: Maybe<NestedIntFilter>;
  _min?: Maybe<NestedStringFilter>;
  _max?: Maybe<NestedStringFilter>;
};

export type NullableStringFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['String']>;
};

export type Query = {
  __typename?: 'Query';
  article?: Maybe<FieldWrapper<Article>>;
  findFirstArticle?: Maybe<FieldWrapper<Article>>;
  articles: Array<FieldWrapper<Article>>;
  aggregateArticle: FieldWrapper<AggregateArticle>;
  groupByArticle: Array<FieldWrapper<ArticleGroupBy>>;
  articleFilter?: Maybe<FieldWrapper<ArticleFilter>>;
  findFirstArticleFilter?: Maybe<FieldWrapper<ArticleFilter>>;
  articleFilters: Array<FieldWrapper<ArticleFilter>>;
  aggregateArticleFilter: FieldWrapper<AggregateArticleFilter>;
  groupByArticleFilter: Array<FieldWrapper<ArticleFilterGroupBy>>;
  articleRef?: Maybe<FieldWrapper<ArticleRef>>;
  findFirstArticleRef?: Maybe<FieldWrapper<ArticleRef>>;
  articleRefs: Array<FieldWrapper<ArticleRef>>;
  aggregateArticleRef: FieldWrapper<AggregateArticleRef>;
  groupByArticleRef: Array<FieldWrapper<ArticleRefGroupBy>>;
  bucket?: Maybe<FieldWrapper<Bucket>>;
  findFirstBucket?: Maybe<FieldWrapper<Bucket>>;
  buckets: Array<FieldWrapper<Bucket>>;
  aggregateBucket: FieldWrapper<AggregateBucket>;
  groupByBucket: Array<FieldWrapper<BucketGroupBy>>;
  feed?: Maybe<FieldWrapper<Feed>>;
  findFirstFeed?: Maybe<FieldWrapper<Feed>>;
  feeds: Array<FieldWrapper<Feed>>;
  aggregateFeed: FieldWrapper<AggregateFeed>;
  groupByFeed: Array<FieldWrapper<FeedGroupBy>>;
  subscription?: Maybe<FieldWrapper<Subscription>>;
  findFirstSubscription?: Maybe<FieldWrapper<Subscription>>;
  subscriptions: Array<FieldWrapper<Subscription>>;
  aggregateSubscription: FieldWrapper<AggregateSubscription>;
  groupBySubscription: Array<FieldWrapper<SubscriptionGroupBy>>;
  user?: Maybe<FieldWrapper<User>>;
  findFirstUser?: Maybe<FieldWrapper<User>>;
  users: Array<FieldWrapper<User>>;
  aggregateUser: FieldWrapper<AggregateUser>;
  groupByUser: Array<FieldWrapper<UserGroupBy>>;
  userArticle?: Maybe<FieldWrapper<UserArticle>>;
  findFirstUserArticle?: Maybe<FieldWrapper<UserArticle>>;
  userArticles: Array<FieldWrapper<UserArticle>>;
  aggregateUserArticle: FieldWrapper<AggregateUserArticle>;
  groupByUserArticle: Array<FieldWrapper<UserArticleGroupBy>>;
  userFeed?: Maybe<FieldWrapper<UserFeed>>;
  findFirstUserFeed?: Maybe<FieldWrapper<UserFeed>>;
  userFeeds: Array<FieldWrapper<UserFeed>>;
  aggregateUserFeed: FieldWrapper<AggregateUserFeed>;
  groupByUserFeed: Array<FieldWrapper<UserFeedGroupBy>>;
  discoverFeedsByQuery: Array<FieldWrapper<DiscoveredFeed>>;
};

export type QueryArticleArgs = {
  where: ArticleWhereUniqueInput;
};

export type QueryFindFirstArticleArgs = {
  where?: Maybe<ArticleWhereInput>;
  orderBy?: Maybe<Array<ArticleOrderByInput>>;
  cursor?: Maybe<ArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<ArticleScalarFieldEnum>>;
};

export type QueryArticlesArgs = {
  where?: Maybe<ArticleWhereInput>;
  orderBy?: Maybe<Array<ArticleOrderByInput>>;
  cursor?: Maybe<ArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<ArticleScalarFieldEnum>>;
};

export type QueryAggregateArticleArgs = {
  where?: Maybe<ArticleWhereInput>;
  orderBy?: Maybe<Array<ArticleOrderByInput>>;
  cursor?: Maybe<ArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryGroupByArticleArgs = {
  where?: Maybe<ArticleWhereInput>;
  orderBy?: Maybe<Array<ArticleOrderByInput>>;
  by: Array<ArticleScalarFieldEnum>;
  having?: Maybe<ArticleScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryArticleFilterArgs = {
  where: ArticleFilterWhereUniqueInput;
};

export type QueryFindFirstArticleFilterArgs = {
  where?: Maybe<ArticleFilterWhereInput>;
  orderBy?: Maybe<Array<ArticleFilterOrderByInput>>;
  cursor?: Maybe<ArticleFilterWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<ArticleFilterScalarFieldEnum>>;
};

export type QueryArticleFiltersArgs = {
  where?: Maybe<ArticleFilterWhereInput>;
  orderBy?: Maybe<Array<ArticleFilterOrderByInput>>;
  cursor?: Maybe<ArticleFilterWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<ArticleFilterScalarFieldEnum>>;
};

export type QueryAggregateArticleFilterArgs = {
  where?: Maybe<ArticleFilterWhereInput>;
  orderBy?: Maybe<Array<ArticleFilterOrderByInput>>;
  cursor?: Maybe<ArticleFilterWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryGroupByArticleFilterArgs = {
  where?: Maybe<ArticleFilterWhereInput>;
  orderBy?: Maybe<Array<ArticleFilterOrderByInput>>;
  by: Array<ArticleFilterScalarFieldEnum>;
  having?: Maybe<ArticleFilterScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryArticleRefArgs = {
  where: ArticleRefWhereUniqueInput;
};

export type QueryFindFirstArticleRefArgs = {
  where?: Maybe<ArticleRefWhereInput>;
  orderBy?: Maybe<Array<ArticleRefOrderByInput>>;
  cursor?: Maybe<ArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<ArticleRefScalarFieldEnum>>;
};

export type QueryArticleRefsArgs = {
  where?: Maybe<ArticleRefWhereInput>;
  orderBy?: Maybe<Array<ArticleRefOrderByInput>>;
  cursor?: Maybe<ArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<ArticleRefScalarFieldEnum>>;
};

export type QueryAggregateArticleRefArgs = {
  where?: Maybe<ArticleRefWhereInput>;
  orderBy?: Maybe<Array<ArticleRefOrderByInput>>;
  cursor?: Maybe<ArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryGroupByArticleRefArgs = {
  where?: Maybe<ArticleRefWhereInput>;
  orderBy?: Maybe<Array<ArticleRefOrderByInput>>;
  by: Array<ArticleRefScalarFieldEnum>;
  having?: Maybe<ArticleRefScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryBucketArgs = {
  where: BucketWhereUniqueInput;
};

export type QueryFindFirstBucketArgs = {
  where?: Maybe<BucketWhereInput>;
  orderBy?: Maybe<Array<BucketOrderByInput>>;
  cursor?: Maybe<BucketWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<BucketScalarFieldEnum>>;
};

export type QueryBucketsArgs = {
  where?: Maybe<BucketWhereInput>;
  orderBy?: Maybe<Array<BucketOrderByInput>>;
  cursor?: Maybe<BucketWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<BucketScalarFieldEnum>>;
};

export type QueryAggregateBucketArgs = {
  where?: Maybe<BucketWhereInput>;
  orderBy?: Maybe<Array<BucketOrderByInput>>;
  cursor?: Maybe<BucketWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryGroupByBucketArgs = {
  where?: Maybe<BucketWhereInput>;
  orderBy?: Maybe<Array<BucketOrderByInput>>;
  by: Array<BucketScalarFieldEnum>;
  having?: Maybe<BucketScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryFeedArgs = {
  where: FeedWhereUniqueInput;
};

export type QueryFindFirstFeedArgs = {
  where?: Maybe<FeedWhereInput>;
  orderBy?: Maybe<Array<FeedOrderByInput>>;
  cursor?: Maybe<FeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<FeedScalarFieldEnum>>;
};

export type QueryFeedsArgs = {
  where?: Maybe<FeedWhereInput>;
  orderBy?: Maybe<Array<FeedOrderByInput>>;
  cursor?: Maybe<FeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<FeedScalarFieldEnum>>;
};

export type QueryAggregateFeedArgs = {
  where?: Maybe<FeedWhereInput>;
  orderBy?: Maybe<Array<FeedOrderByInput>>;
  cursor?: Maybe<FeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryGroupByFeedArgs = {
  where?: Maybe<FeedWhereInput>;
  orderBy?: Maybe<Array<FeedOrderByInput>>;
  by: Array<FeedScalarFieldEnum>;
  having?: Maybe<FeedScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QuerySubscriptionArgs = {
  where: SubscriptionWhereUniqueInput;
};

export type QueryFindFirstSubscriptionArgs = {
  where?: Maybe<SubscriptionWhereInput>;
  orderBy?: Maybe<Array<SubscriptionOrderByInput>>;
  cursor?: Maybe<SubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<SubscriptionScalarFieldEnum>>;
};

export type QuerySubscriptionsArgs = {
  where?: Maybe<SubscriptionWhereInput>;
  orderBy?: Maybe<Array<SubscriptionOrderByInput>>;
  cursor?: Maybe<SubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<SubscriptionScalarFieldEnum>>;
};

export type QueryAggregateSubscriptionArgs = {
  where?: Maybe<SubscriptionWhereInput>;
  orderBy?: Maybe<Array<SubscriptionOrderByInput>>;
  cursor?: Maybe<SubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryGroupBySubscriptionArgs = {
  where?: Maybe<SubscriptionWhereInput>;
  orderBy?: Maybe<Array<SubscriptionOrderByInput>>;
  by: Array<SubscriptionScalarFieldEnum>;
  having?: Maybe<SubscriptionScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryUserArgs = {
  where: UserWhereUniqueInput;
};

export type QueryFindFirstUserArgs = {
  where?: Maybe<UserWhereInput>;
  orderBy?: Maybe<Array<UserOrderByInput>>;
  cursor?: Maybe<UserWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<UserScalarFieldEnum>>;
};

export type QueryUsersArgs = {
  where?: Maybe<UserWhereInput>;
  orderBy?: Maybe<Array<UserOrderByInput>>;
  cursor?: Maybe<UserWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<UserScalarFieldEnum>>;
};

export type QueryAggregateUserArgs = {
  where?: Maybe<UserWhereInput>;
  orderBy?: Maybe<Array<UserOrderByInput>>;
  cursor?: Maybe<UserWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryGroupByUserArgs = {
  where?: Maybe<UserWhereInput>;
  orderBy?: Maybe<Array<UserOrderByInput>>;
  by: Array<UserScalarFieldEnum>;
  having?: Maybe<UserScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryUserArticleArgs = {
  where: UserArticleWhereUniqueInput;
};

export type QueryFindFirstUserArticleArgs = {
  where?: Maybe<UserArticleWhereInput>;
  orderBy?: Maybe<Array<UserArticleOrderByInput>>;
  cursor?: Maybe<UserArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<UserArticleScalarFieldEnum>>;
};

export type QueryUserArticlesArgs = {
  where?: Maybe<UserArticleWhereInput>;
  orderBy?: Maybe<Array<UserArticleOrderByInput>>;
  cursor?: Maybe<UserArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<UserArticleScalarFieldEnum>>;
};

export type QueryAggregateUserArticleArgs = {
  where?: Maybe<UserArticleWhereInput>;
  orderBy?: Maybe<Array<UserArticleOrderByInput>>;
  cursor?: Maybe<UserArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryGroupByUserArticleArgs = {
  where?: Maybe<UserArticleWhereInput>;
  orderBy?: Maybe<Array<UserArticleOrderByInput>>;
  by: Array<UserArticleScalarFieldEnum>;
  having?: Maybe<UserArticleScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryUserFeedArgs = {
  where: UserFeedWhereUniqueInput;
};

export type QueryFindFirstUserFeedArgs = {
  where?: Maybe<UserFeedWhereInput>;
  orderBy?: Maybe<Array<UserFeedOrderByInput>>;
  cursor?: Maybe<UserFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<UserFeedScalarFieldEnum>>;
};

export type QueryUserFeedsArgs = {
  where?: Maybe<UserFeedWhereInput>;
  orderBy?: Maybe<Array<UserFeedOrderByInput>>;
  cursor?: Maybe<UserFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<UserFeedScalarFieldEnum>>;
};

export type QueryAggregateUserFeedArgs = {
  where?: Maybe<UserFeedWhereInput>;
  orderBy?: Maybe<Array<UserFeedOrderByInput>>;
  cursor?: Maybe<UserFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryGroupByUserFeedArgs = {
  where?: Maybe<UserFeedWhereInput>;
  orderBy?: Maybe<Array<UserFeedOrderByInput>>;
  by: Array<UserFeedScalarFieldEnum>;
  having?: Maybe<UserFeedScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};

export type QueryDiscoverFeedsByQueryArgs = {
  query: Scalars['String'];
};

export enum SortOrder {
  Asc = 'asc',
  Desc = 'desc',
}

export type StringFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['String']>;
};

export type StringFilter = {
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
  not?: Maybe<NestedStringFilter>;
};

export type StringNullableFilter = {
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
  not?: Maybe<NestedStringNullableFilter>;
};

export type StringNullableWithAggregatesFilter = {
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
  not?: Maybe<NestedStringNullableWithAggregatesFilter>;
  _count?: Maybe<NestedIntNullableFilter>;
  _min?: Maybe<NestedStringNullableFilter>;
  _max?: Maybe<NestedStringNullableFilter>;
};

export type StringWithAggregatesFilter = {
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
  not?: Maybe<NestedStringWithAggregatesFilter>;
  _count?: Maybe<NestedIntFilter>;
  _min?: Maybe<NestedStringFilter>;
  _max?: Maybe<NestedStringFilter>;
};

export type Subscription = {
  __typename?: 'Subscription';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  tags: FieldWrapper<Scalars['String']>;
  feedId: FieldWrapper<Scalars['String']>;
  ownerId: FieldWrapper<Scalars['String']>;
  bucketId: FieldWrapper<Scalars['String']>;
  feed: FieldWrapper<Feed>;
  owner: FieldWrapper<User>;
  bucket: FieldWrapper<Bucket>;
};

export type SubscriptionCountAggregate = {
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

export type SubscriptionCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  tags?: Maybe<Scalars['String']>;
  feed: FeedCreateNestedOneWithoutSubscriptionsInput;
  owner: UserCreateNestedOneWithoutSubscriptionInput;
  bucket: BucketCreateNestedOneWithoutSubscriptionsInput;
};

export type SubscriptionCreateNestedManyWithoutBucketInput = {
  create?: Maybe<Array<SubscriptionCreateWithoutBucketInput>>;
  connectOrCreate?: Maybe<Array<SubscriptionCreateOrConnectWithoutBucketInput>>;
  connect?: Maybe<Array<SubscriptionWhereUniqueInput>>;
};

export type SubscriptionCreateNestedManyWithoutFeedInput = {
  create?: Maybe<Array<SubscriptionCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<SubscriptionCreateOrConnectWithoutFeedInput>>;
  connect?: Maybe<Array<SubscriptionWhereUniqueInput>>;
};

export type SubscriptionCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<SubscriptionCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<SubscriptionCreateOrConnectWithoutOwnerInput>>;
  connect?: Maybe<Array<SubscriptionWhereUniqueInput>>;
};

export type SubscriptionCreateOrConnectWithoutBucketInput = {
  where: SubscriptionWhereUniqueInput;
  create: SubscriptionCreateWithoutBucketInput;
};

export type SubscriptionCreateOrConnectWithoutFeedInput = {
  where: SubscriptionWhereUniqueInput;
  create: SubscriptionCreateWithoutFeedInput;
};

export type SubscriptionCreateOrConnectWithoutOwnerInput = {
  where: SubscriptionWhereUniqueInput;
  create: SubscriptionCreateWithoutOwnerInput;
};

export type SubscriptionCreateWithoutBucketInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  tags?: Maybe<Scalars['String']>;
  feed: FeedCreateNestedOneWithoutSubscriptionsInput;
  owner: UserCreateNestedOneWithoutSubscriptionInput;
};

export type SubscriptionCreateWithoutFeedInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  tags?: Maybe<Scalars['String']>;
  owner: UserCreateNestedOneWithoutSubscriptionInput;
  bucket: BucketCreateNestedOneWithoutSubscriptionsInput;
};

export type SubscriptionCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  tags?: Maybe<Scalars['String']>;
  feed: FeedCreateNestedOneWithoutSubscriptionsInput;
  bucket: BucketCreateNestedOneWithoutSubscriptionsInput;
};

export type SubscriptionGroupBy = {
  __typename?: 'SubscriptionGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  tags: FieldWrapper<Scalars['String']>;
  feedId: FieldWrapper<Scalars['String']>;
  ownerId: FieldWrapper<Scalars['String']>;
  bucketId: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<SubscriptionCountAggregate>>;
  _min?: Maybe<FieldWrapper<SubscriptionMinAggregate>>;
  _max?: Maybe<FieldWrapper<SubscriptionMaxAggregate>>;
};

export type SubscriptionListRelationFilter = {
  every?: Maybe<SubscriptionWhereInput>;
  some?: Maybe<SubscriptionWhereInput>;
  none?: Maybe<SubscriptionWhereInput>;
};

export type SubscriptionMaxAggregate = {
  __typename?: 'SubscriptionMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  tags?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type SubscriptionMinAggregate = {
  __typename?: 'SubscriptionMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  tags?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type SubscriptionOrderByInput = {
  id?: Maybe<SortOrder>;
  createdAt?: Maybe<SortOrder>;
  updatedAt?: Maybe<SortOrder>;
  tags?: Maybe<SortOrder>;
  feedId?: Maybe<SortOrder>;
  ownerId?: Maybe<SortOrder>;
  bucketId?: Maybe<SortOrder>;
};

export enum SubscriptionScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Tags = 'tags',
  FeedId = 'feedId',
  OwnerId = 'ownerId',
  BucketId = 'bucketId',
}

export type SubscriptionScalarWhereInput = {
  AND?: Maybe<Array<SubscriptionScalarWhereInput>>;
  OR?: Maybe<Array<SubscriptionScalarWhereInput>>;
  NOT?: Maybe<Array<SubscriptionScalarWhereInput>>;
  id?: Maybe<StringFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  tags?: Maybe<StringFilter>;
  feedId?: Maybe<StringFilter>;
  ownerId?: Maybe<StringFilter>;
  bucketId?: Maybe<StringFilter>;
};

export type SubscriptionScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<SubscriptionScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<SubscriptionScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<SubscriptionScalarWhereWithAggregatesInput>>;
  id?: Maybe<StringWithAggregatesFilter>;
  createdAt?: Maybe<DateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<DateTimeWithAggregatesFilter>;
  tags?: Maybe<StringWithAggregatesFilter>;
  feedId?: Maybe<StringWithAggregatesFilter>;
  ownerId?: Maybe<StringWithAggregatesFilter>;
  bucketId?: Maybe<StringWithAggregatesFilter>;
};

export type SubscriptionUpdateInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  tags?: Maybe<StringFieldUpdateOperationsInput>;
  feed?: Maybe<FeedUpdateOneRequiredWithoutSubscriptionsInput>;
  owner?: Maybe<UserUpdateOneRequiredWithoutSubscriptionInput>;
  bucket?: Maybe<BucketUpdateOneRequiredWithoutSubscriptionsInput>;
};

export type SubscriptionUpdateManyMutationInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  tags?: Maybe<StringFieldUpdateOperationsInput>;
};

export type SubscriptionUpdateManyWithWhereWithoutBucketInput = {
  where: SubscriptionScalarWhereInput;
  data: SubscriptionUpdateManyMutationInput;
};

export type SubscriptionUpdateManyWithWhereWithoutFeedInput = {
  where: SubscriptionScalarWhereInput;
  data: SubscriptionUpdateManyMutationInput;
};

export type SubscriptionUpdateManyWithWhereWithoutOwnerInput = {
  where: SubscriptionScalarWhereInput;
  data: SubscriptionUpdateManyMutationInput;
};

export type SubscriptionUpdateManyWithoutBucketInput = {
  create?: Maybe<Array<SubscriptionCreateWithoutBucketInput>>;
  connectOrCreate?: Maybe<Array<SubscriptionCreateOrConnectWithoutBucketInput>>;
  upsert?: Maybe<Array<SubscriptionUpsertWithWhereUniqueWithoutBucketInput>>;
  connect?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  set?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  disconnect?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  delete?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  update?: Maybe<Array<SubscriptionUpdateWithWhereUniqueWithoutBucketInput>>;
  updateMany?: Maybe<Array<SubscriptionUpdateManyWithWhereWithoutBucketInput>>;
  deleteMany?: Maybe<Array<SubscriptionScalarWhereInput>>;
};

export type SubscriptionUpdateManyWithoutFeedInput = {
  create?: Maybe<Array<SubscriptionCreateWithoutFeedInput>>;
  connectOrCreate?: Maybe<Array<SubscriptionCreateOrConnectWithoutFeedInput>>;
  upsert?: Maybe<Array<SubscriptionUpsertWithWhereUniqueWithoutFeedInput>>;
  connect?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  set?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  disconnect?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  delete?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  update?: Maybe<Array<SubscriptionUpdateWithWhereUniqueWithoutFeedInput>>;
  updateMany?: Maybe<Array<SubscriptionUpdateManyWithWhereWithoutFeedInput>>;
  deleteMany?: Maybe<Array<SubscriptionScalarWhereInput>>;
};

export type SubscriptionUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<SubscriptionCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<SubscriptionCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<SubscriptionUpsertWithWhereUniqueWithoutOwnerInput>>;
  connect?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  set?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  disconnect?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  delete?: Maybe<Array<SubscriptionWhereUniqueInput>>;
  update?: Maybe<Array<SubscriptionUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<SubscriptionUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<SubscriptionScalarWhereInput>>;
};

export type SubscriptionUpdateWithWhereUniqueWithoutBucketInput = {
  where: SubscriptionWhereUniqueInput;
  data: SubscriptionUpdateWithoutBucketInput;
};

export type SubscriptionUpdateWithWhereUniqueWithoutFeedInput = {
  where: SubscriptionWhereUniqueInput;
  data: SubscriptionUpdateWithoutFeedInput;
};

export type SubscriptionUpdateWithWhereUniqueWithoutOwnerInput = {
  where: SubscriptionWhereUniqueInput;
  data: SubscriptionUpdateWithoutOwnerInput;
};

export type SubscriptionUpdateWithoutBucketInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  tags?: Maybe<StringFieldUpdateOperationsInput>;
  feed?: Maybe<FeedUpdateOneRequiredWithoutSubscriptionsInput>;
  owner?: Maybe<UserUpdateOneRequiredWithoutSubscriptionInput>;
};

export type SubscriptionUpdateWithoutFeedInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  tags?: Maybe<StringFieldUpdateOperationsInput>;
  owner?: Maybe<UserUpdateOneRequiredWithoutSubscriptionInput>;
  bucket?: Maybe<BucketUpdateOneRequiredWithoutSubscriptionsInput>;
};

export type SubscriptionUpdateWithoutOwnerInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  tags?: Maybe<StringFieldUpdateOperationsInput>;
  feed?: Maybe<FeedUpdateOneRequiredWithoutSubscriptionsInput>;
  bucket?: Maybe<BucketUpdateOneRequiredWithoutSubscriptionsInput>;
};

export type SubscriptionUpsertWithWhereUniqueWithoutBucketInput = {
  where: SubscriptionWhereUniqueInput;
  update: SubscriptionUpdateWithoutBucketInput;
  create: SubscriptionCreateWithoutBucketInput;
};

export type SubscriptionUpsertWithWhereUniqueWithoutFeedInput = {
  where: SubscriptionWhereUniqueInput;
  update: SubscriptionUpdateWithoutFeedInput;
  create: SubscriptionCreateWithoutFeedInput;
};

export type SubscriptionUpsertWithWhereUniqueWithoutOwnerInput = {
  where: SubscriptionWhereUniqueInput;
  update: SubscriptionUpdateWithoutOwnerInput;
  create: SubscriptionCreateWithoutOwnerInput;
};

export type SubscriptionWhereInput = {
  AND?: Maybe<Array<SubscriptionWhereInput>>;
  OR?: Maybe<Array<SubscriptionWhereInput>>;
  NOT?: Maybe<Array<SubscriptionWhereInput>>;
  id?: Maybe<StringFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  tags?: Maybe<StringFilter>;
  feed?: Maybe<FeedRelationFilter>;
  feedId?: Maybe<StringFilter>;
  owner?: Maybe<UserRelationFilter>;
  ownerId?: Maybe<StringFilter>;
  bucket?: Maybe<BucketRelationFilter>;
  bucketId?: Maybe<StringFilter>;
};

export type SubscriptionWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type User = {
  __typename?: 'User';
  id: FieldWrapper<Scalars['String']>;
  email: FieldWrapper<Scalars['String']>;
  name: FieldWrapper<Scalars['String']>;
  buckets: Array<FieldWrapper<Bucket>>;
  articleRefs: Array<FieldWrapper<ArticleRef>>;
  feeds: Array<FieldWrapper<UserFeed>>;
  Subscription: Array<FieldWrapper<Subscription>>;
};

export type UserBucketsArgs = {
  where?: Maybe<BucketWhereInput>;
  orderBy?: Maybe<Array<BucketOrderByInput>>;
  cursor?: Maybe<BucketWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<BucketScalarFieldEnum>>;
};

export type UserArticleRefsArgs = {
  where?: Maybe<ArticleRefWhereInput>;
  orderBy?: Maybe<Array<ArticleRefOrderByInput>>;
  cursor?: Maybe<ArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<ArticleRefScalarFieldEnum>>;
};

export type UserFeedsArgs = {
  where?: Maybe<UserFeedWhereInput>;
  orderBy?: Maybe<Array<UserFeedOrderByInput>>;
  cursor?: Maybe<UserFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<UserFeedScalarFieldEnum>>;
};

export type UserSubscriptionArgs = {
  where?: Maybe<SubscriptionWhereInput>;
  orderBy?: Maybe<Array<SubscriptionOrderByInput>>;
  cursor?: Maybe<SubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<SubscriptionScalarFieldEnum>>;
};

export type UserArticle = {
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
  userFeed: FieldWrapper<UserFeed>;
};

export type UserArticleCountAggregate = {
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

export type UserArticleCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  harvested?: Maybe<Scalars['Boolean']>;
  harvestUrl?: Maybe<Scalars['String']>;
  source: Scalars['String'];
  title: Scalars['String'];
  content: Scalars['String'];
  contentHtml: Scalars['String'];
  userFeed: UserFeedCreateNestedOneWithoutArticlesInput;
};

export type UserArticleCreateNestedManyWithoutUserFeedInput = {
  create?: Maybe<Array<UserArticleCreateWithoutUserFeedInput>>;
  connectOrCreate?: Maybe<
    Array<UserArticleCreateOrConnectWithoutUserFeedInput>
  >;
  connect?: Maybe<Array<UserArticleWhereUniqueInput>>;
};

export type UserArticleCreateOrConnectWithoutUserFeedInput = {
  where: UserArticleWhereUniqueInput;
  create: UserArticleCreateWithoutUserFeedInput;
};

export type UserArticleCreateWithoutUserFeedInput = {
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

export type UserArticleGroupBy = {
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
  _count?: Maybe<FieldWrapper<UserArticleCountAggregate>>;
  _min?: Maybe<FieldWrapper<UserArticleMinAggregate>>;
  _max?: Maybe<FieldWrapper<UserArticleMaxAggregate>>;
};

export type UserArticleListRelationFilter = {
  every?: Maybe<UserArticleWhereInput>;
  some?: Maybe<UserArticleWhereInput>;
  none?: Maybe<UserArticleWhereInput>;
};

export type UserArticleMaxAggregate = {
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

export type UserArticleMinAggregate = {
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

export type UserArticleOrderByInput = {
  id?: Maybe<SortOrder>;
  createdAt?: Maybe<SortOrder>;
  updatedAt?: Maybe<SortOrder>;
  harvested?: Maybe<SortOrder>;
  harvestUrl?: Maybe<SortOrder>;
  source?: Maybe<SortOrder>;
  title?: Maybe<SortOrder>;
  content?: Maybe<SortOrder>;
  contentHtml?: Maybe<SortOrder>;
  userFeedId?: Maybe<SortOrder>;
};

export enum UserArticleScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Harvested = 'harvested',
  HarvestUrl = 'harvestUrl',
  Source = 'source',
  Title = 'title',
  Content = 'content',
  ContentHtml = 'contentHtml',
  UserFeedId = 'userFeedId',
}

export type UserArticleScalarWhereInput = {
  AND?: Maybe<Array<UserArticleScalarWhereInput>>;
  OR?: Maybe<Array<UserArticleScalarWhereInput>>;
  NOT?: Maybe<Array<UserArticleScalarWhereInput>>;
  id?: Maybe<StringFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  harvested?: Maybe<BoolFilter>;
  harvestUrl?: Maybe<StringNullableFilter>;
  source?: Maybe<StringFilter>;
  title?: Maybe<StringFilter>;
  content?: Maybe<StringFilter>;
  contentHtml?: Maybe<StringFilter>;
  userFeedId?: Maybe<StringFilter>;
};

export type UserArticleScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<UserArticleScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<UserArticleScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<UserArticleScalarWhereWithAggregatesInput>>;
  id?: Maybe<StringWithAggregatesFilter>;
  createdAt?: Maybe<DateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<DateTimeWithAggregatesFilter>;
  harvested?: Maybe<BoolWithAggregatesFilter>;
  harvestUrl?: Maybe<StringNullableWithAggregatesFilter>;
  source?: Maybe<StringWithAggregatesFilter>;
  title?: Maybe<StringWithAggregatesFilter>;
  content?: Maybe<StringWithAggregatesFilter>;
  contentHtml?: Maybe<StringWithAggregatesFilter>;
  userFeedId?: Maybe<StringWithAggregatesFilter>;
};

export type UserArticleUpdateInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  harvested?: Maybe<BoolFieldUpdateOperationsInput>;
  harvestUrl?: Maybe<NullableStringFieldUpdateOperationsInput>;
  source?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  content?: Maybe<StringFieldUpdateOperationsInput>;
  contentHtml?: Maybe<StringFieldUpdateOperationsInput>;
  userFeed?: Maybe<UserFeedUpdateOneRequiredWithoutArticlesInput>;
};

export type UserArticleUpdateManyMutationInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  harvested?: Maybe<BoolFieldUpdateOperationsInput>;
  harvestUrl?: Maybe<NullableStringFieldUpdateOperationsInput>;
  source?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  content?: Maybe<StringFieldUpdateOperationsInput>;
  contentHtml?: Maybe<StringFieldUpdateOperationsInput>;
};

export type UserArticleUpdateManyWithWhereWithoutUserFeedInput = {
  where: UserArticleScalarWhereInput;
  data: UserArticleUpdateManyMutationInput;
};

export type UserArticleUpdateManyWithoutUserFeedInput = {
  create?: Maybe<Array<UserArticleCreateWithoutUserFeedInput>>;
  connectOrCreate?: Maybe<
    Array<UserArticleCreateOrConnectWithoutUserFeedInput>
  >;
  upsert?: Maybe<Array<UserArticleUpsertWithWhereUniqueWithoutUserFeedInput>>;
  connect?: Maybe<Array<UserArticleWhereUniqueInput>>;
  set?: Maybe<Array<UserArticleWhereUniqueInput>>;
  disconnect?: Maybe<Array<UserArticleWhereUniqueInput>>;
  delete?: Maybe<Array<UserArticleWhereUniqueInput>>;
  update?: Maybe<Array<UserArticleUpdateWithWhereUniqueWithoutUserFeedInput>>;
  updateMany?: Maybe<Array<UserArticleUpdateManyWithWhereWithoutUserFeedInput>>;
  deleteMany?: Maybe<Array<UserArticleScalarWhereInput>>;
};

export type UserArticleUpdateWithWhereUniqueWithoutUserFeedInput = {
  where: UserArticleWhereUniqueInput;
  data: UserArticleUpdateWithoutUserFeedInput;
};

export type UserArticleUpdateWithoutUserFeedInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  harvested?: Maybe<BoolFieldUpdateOperationsInput>;
  harvestUrl?: Maybe<NullableStringFieldUpdateOperationsInput>;
  source?: Maybe<StringFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  content?: Maybe<StringFieldUpdateOperationsInput>;
  contentHtml?: Maybe<StringFieldUpdateOperationsInput>;
};

export type UserArticleUpsertWithWhereUniqueWithoutUserFeedInput = {
  where: UserArticleWhereUniqueInput;
  update: UserArticleUpdateWithoutUserFeedInput;
  create: UserArticleCreateWithoutUserFeedInput;
};

export type UserArticleWhereInput = {
  AND?: Maybe<Array<UserArticleWhereInput>>;
  OR?: Maybe<Array<UserArticleWhereInput>>;
  NOT?: Maybe<Array<UserArticleWhereInput>>;
  id?: Maybe<StringFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  harvested?: Maybe<BoolFilter>;
  harvestUrl?: Maybe<StringNullableFilter>;
  source?: Maybe<StringFilter>;
  title?: Maybe<StringFilter>;
  content?: Maybe<StringFilter>;
  contentHtml?: Maybe<StringFilter>;
  userFeed?: Maybe<UserFeedRelationFilter>;
  userFeedId?: Maybe<StringFilter>;
};

export type UserArticleWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type UserCountAggregate = {
  __typename?: 'UserCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  email: FieldWrapper<Scalars['Int']>;
  name: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type UserCreateInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<BucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<ArticleRefCreateNestedManyWithoutOwnerInput>;
  feeds?: Maybe<UserFeedCreateNestedManyWithoutOwnerInput>;
  Subscription?: Maybe<SubscriptionCreateNestedManyWithoutOwnerInput>;
};

export type UserCreateNestedOneWithoutArticleRefsInput = {
  create?: Maybe<UserCreateWithoutArticleRefsInput>;
  connectOrCreate?: Maybe<UserCreateOrConnectWithoutArticleRefsInput>;
  connect?: Maybe<UserWhereUniqueInput>;
};

export type UserCreateNestedOneWithoutBucketsInput = {
  create?: Maybe<UserCreateWithoutBucketsInput>;
  connectOrCreate?: Maybe<UserCreateOrConnectWithoutBucketsInput>;
  connect?: Maybe<UserWhereUniqueInput>;
};

export type UserCreateNestedOneWithoutFeedsInput = {
  create?: Maybe<UserCreateWithoutFeedsInput>;
  connectOrCreate?: Maybe<UserCreateOrConnectWithoutFeedsInput>;
  connect?: Maybe<UserWhereUniqueInput>;
};

export type UserCreateNestedOneWithoutSubscriptionInput = {
  create?: Maybe<UserCreateWithoutSubscriptionInput>;
  connectOrCreate?: Maybe<UserCreateOrConnectWithoutSubscriptionInput>;
  connect?: Maybe<UserWhereUniqueInput>;
};

export type UserCreateOrConnectWithoutArticleRefsInput = {
  where: UserWhereUniqueInput;
  create: UserCreateWithoutArticleRefsInput;
};

export type UserCreateOrConnectWithoutBucketsInput = {
  where: UserWhereUniqueInput;
  create: UserCreateWithoutBucketsInput;
};

export type UserCreateOrConnectWithoutFeedsInput = {
  where: UserWhereUniqueInput;
  create: UserCreateWithoutFeedsInput;
};

export type UserCreateOrConnectWithoutSubscriptionInput = {
  where: UserWhereUniqueInput;
  create: UserCreateWithoutSubscriptionInput;
};

export type UserCreateWithoutArticleRefsInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<BucketCreateNestedManyWithoutOwnerInput>;
  feeds?: Maybe<UserFeedCreateNestedManyWithoutOwnerInput>;
  Subscription?: Maybe<SubscriptionCreateNestedManyWithoutOwnerInput>;
};

export type UserCreateWithoutBucketsInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  articleRefs?: Maybe<ArticleRefCreateNestedManyWithoutOwnerInput>;
  feeds?: Maybe<UserFeedCreateNestedManyWithoutOwnerInput>;
  Subscription?: Maybe<SubscriptionCreateNestedManyWithoutOwnerInput>;
};

export type UserCreateWithoutFeedsInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<BucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<ArticleRefCreateNestedManyWithoutOwnerInput>;
  Subscription?: Maybe<SubscriptionCreateNestedManyWithoutOwnerInput>;
};

export type UserCreateWithoutSubscriptionInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<BucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<ArticleRefCreateNestedManyWithoutOwnerInput>;
  feeds?: Maybe<UserFeedCreateNestedManyWithoutOwnerInput>;
};

export type UserFeed = {
  __typename?: 'UserFeed';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  exposed: FieldWrapper<Scalars['Boolean']>;
  title: FieldWrapper<Scalars['String']>;
  feedType: FieldWrapper<Scalars['String']>;
  description: FieldWrapper<Scalars['String']>;
  owner: FieldWrapper<User>;
  articles: Array<FieldWrapper<UserArticle>>;
};

export type UserFeedArticlesArgs = {
  where?: Maybe<UserArticleWhereInput>;
  orderBy?: Maybe<Array<UserArticleOrderByInput>>;
  cursor?: Maybe<UserArticleWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<UserArticleScalarFieldEnum>>;
};

export type UserFeedCountAggregate = {
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

export type UserFeedCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  exposed: Scalars['Boolean'];
  title: Scalars['String'];
  feedType: Scalars['String'];
  description: Scalars['String'];
  owner: UserCreateNestedOneWithoutFeedsInput;
  articles?: Maybe<UserArticleCreateNestedManyWithoutUserFeedInput>;
};

export type UserFeedCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<UserFeedCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<UserFeedCreateOrConnectWithoutOwnerInput>>;
  connect?: Maybe<Array<UserFeedWhereUniqueInput>>;
};

export type UserFeedCreateNestedOneWithoutArticlesInput = {
  create?: Maybe<UserFeedCreateWithoutArticlesInput>;
  connectOrCreate?: Maybe<UserFeedCreateOrConnectWithoutArticlesInput>;
  connect?: Maybe<UserFeedWhereUniqueInput>;
};

export type UserFeedCreateOrConnectWithoutArticlesInput = {
  where: UserFeedWhereUniqueInput;
  create: UserFeedCreateWithoutArticlesInput;
};

export type UserFeedCreateOrConnectWithoutOwnerInput = {
  where: UserFeedWhereUniqueInput;
  create: UserFeedCreateWithoutOwnerInput;
};

export type UserFeedCreateWithoutArticlesInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  exposed: Scalars['Boolean'];
  title: Scalars['String'];
  feedType: Scalars['String'];
  description: Scalars['String'];
  owner: UserCreateNestedOneWithoutFeedsInput;
};

export type UserFeedCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  exposed: Scalars['Boolean'];
  title: Scalars['String'];
  feedType: Scalars['String'];
  description: Scalars['String'];
  articles?: Maybe<UserArticleCreateNestedManyWithoutUserFeedInput>;
};

export type UserFeedGroupBy = {
  __typename?: 'UserFeedGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  exposed: FieldWrapper<Scalars['Boolean']>;
  title: FieldWrapper<Scalars['String']>;
  feedType: FieldWrapper<Scalars['String']>;
  description: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<UserFeedCountAggregate>>;
  _min?: Maybe<FieldWrapper<UserFeedMinAggregate>>;
  _max?: Maybe<FieldWrapper<UserFeedMaxAggregate>>;
};

export type UserFeedListRelationFilter = {
  every?: Maybe<UserFeedWhereInput>;
  some?: Maybe<UserFeedWhereInput>;
  none?: Maybe<UserFeedWhereInput>;
};

export type UserFeedMaxAggregate = {
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

export type UserFeedMinAggregate = {
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

export type UserFeedOrderByInput = {
  id?: Maybe<SortOrder>;
  createdAt?: Maybe<SortOrder>;
  updatedAt?: Maybe<SortOrder>;
  ownerId?: Maybe<SortOrder>;
  exposed?: Maybe<SortOrder>;
  title?: Maybe<SortOrder>;
  feedType?: Maybe<SortOrder>;
  description?: Maybe<SortOrder>;
};

export type UserFeedRelationFilter = {
  is?: Maybe<UserFeedWhereInput>;
  isNot?: Maybe<UserFeedWhereInput>;
};

export enum UserFeedScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  OwnerId = 'ownerId',
  Exposed = 'exposed',
  Title = 'title',
  FeedType = 'feedType',
  Description = 'description',
}

export type UserFeedScalarWhereInput = {
  AND?: Maybe<Array<UserFeedScalarWhereInput>>;
  OR?: Maybe<Array<UserFeedScalarWhereInput>>;
  NOT?: Maybe<Array<UserFeedScalarWhereInput>>;
  id?: Maybe<StringFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  ownerId?: Maybe<StringFilter>;
  exposed?: Maybe<BoolFilter>;
  title?: Maybe<StringFilter>;
  feedType?: Maybe<StringFilter>;
  description?: Maybe<StringFilter>;
};

export type UserFeedScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<UserFeedScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<UserFeedScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<UserFeedScalarWhereWithAggregatesInput>>;
  id?: Maybe<StringWithAggregatesFilter>;
  createdAt?: Maybe<DateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<DateTimeWithAggregatesFilter>;
  ownerId?: Maybe<StringWithAggregatesFilter>;
  exposed?: Maybe<BoolWithAggregatesFilter>;
  title?: Maybe<StringWithAggregatesFilter>;
  feedType?: Maybe<StringWithAggregatesFilter>;
  description?: Maybe<StringWithAggregatesFilter>;
};

export type UserFeedUpdateInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  exposed?: Maybe<BoolFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  feedType?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<StringFieldUpdateOperationsInput>;
  owner?: Maybe<UserUpdateOneRequiredWithoutFeedsInput>;
  articles?: Maybe<UserArticleUpdateManyWithoutUserFeedInput>;
};

export type UserFeedUpdateManyMutationInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  exposed?: Maybe<BoolFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  feedType?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<StringFieldUpdateOperationsInput>;
};

export type UserFeedUpdateManyWithWhereWithoutOwnerInput = {
  where: UserFeedScalarWhereInput;
  data: UserFeedUpdateManyMutationInput;
};

export type UserFeedUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<UserFeedCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<UserFeedCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<UserFeedUpsertWithWhereUniqueWithoutOwnerInput>>;
  connect?: Maybe<Array<UserFeedWhereUniqueInput>>;
  set?: Maybe<Array<UserFeedWhereUniqueInput>>;
  disconnect?: Maybe<Array<UserFeedWhereUniqueInput>>;
  delete?: Maybe<Array<UserFeedWhereUniqueInput>>;
  update?: Maybe<Array<UserFeedUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<UserFeedUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<UserFeedScalarWhereInput>>;
};

export type UserFeedUpdateOneRequiredWithoutArticlesInput = {
  create?: Maybe<UserFeedCreateWithoutArticlesInput>;
  connectOrCreate?: Maybe<UserFeedCreateOrConnectWithoutArticlesInput>;
  upsert?: Maybe<UserFeedUpsertWithoutArticlesInput>;
  connect?: Maybe<UserFeedWhereUniqueInput>;
  update?: Maybe<UserFeedUpdateWithoutArticlesInput>;
};

export type UserFeedUpdateWithWhereUniqueWithoutOwnerInput = {
  where: UserFeedWhereUniqueInput;
  data: UserFeedUpdateWithoutOwnerInput;
};

export type UserFeedUpdateWithoutArticlesInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  exposed?: Maybe<BoolFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  feedType?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<StringFieldUpdateOperationsInput>;
  owner?: Maybe<UserUpdateOneRequiredWithoutFeedsInput>;
};

export type UserFeedUpdateWithoutOwnerInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  createdAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<DateTimeFieldUpdateOperationsInput>;
  exposed?: Maybe<BoolFieldUpdateOperationsInput>;
  title?: Maybe<StringFieldUpdateOperationsInput>;
  feedType?: Maybe<StringFieldUpdateOperationsInput>;
  description?: Maybe<StringFieldUpdateOperationsInput>;
  articles?: Maybe<UserArticleUpdateManyWithoutUserFeedInput>;
};

export type UserFeedUpsertWithWhereUniqueWithoutOwnerInput = {
  where: UserFeedWhereUniqueInput;
  update: UserFeedUpdateWithoutOwnerInput;
  create: UserFeedCreateWithoutOwnerInput;
};

export type UserFeedUpsertWithoutArticlesInput = {
  update: UserFeedUpdateWithoutArticlesInput;
  create: UserFeedCreateWithoutArticlesInput;
};

export type UserFeedWhereInput = {
  AND?: Maybe<Array<UserFeedWhereInput>>;
  OR?: Maybe<Array<UserFeedWhereInput>>;
  NOT?: Maybe<Array<UserFeedWhereInput>>;
  id?: Maybe<StringFilter>;
  createdAt?: Maybe<DateTimeFilter>;
  updatedAt?: Maybe<DateTimeFilter>;
  owner?: Maybe<UserRelationFilter>;
  ownerId?: Maybe<StringFilter>;
  articles?: Maybe<UserArticleListRelationFilter>;
  exposed?: Maybe<BoolFilter>;
  title?: Maybe<StringFilter>;
  feedType?: Maybe<StringFilter>;
  description?: Maybe<StringFilter>;
};

export type UserFeedWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type UserGroupBy = {
  __typename?: 'UserGroupBy';
  id: FieldWrapper<Scalars['String']>;
  email: FieldWrapper<Scalars['String']>;
  name: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<UserCountAggregate>>;
  _min?: Maybe<FieldWrapper<UserMinAggregate>>;
  _max?: Maybe<FieldWrapper<UserMaxAggregate>>;
};

export type UserMaxAggregate = {
  __typename?: 'UserMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  email?: Maybe<FieldWrapper<Scalars['String']>>;
  name?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type UserMinAggregate = {
  __typename?: 'UserMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  email?: Maybe<FieldWrapper<Scalars['String']>>;
  name?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type UserOrderByInput = {
  id?: Maybe<SortOrder>;
  email?: Maybe<SortOrder>;
  name?: Maybe<SortOrder>;
};

export type UserRelationFilter = {
  is?: Maybe<UserWhereInput>;
  isNot?: Maybe<UserWhereInput>;
};

export enum UserScalarFieldEnum {
  Id = 'id',
  Email = 'email',
  Name = 'name',
}

export type UserScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<UserScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<UserScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<UserScalarWhereWithAggregatesInput>>;
  id?: Maybe<StringWithAggregatesFilter>;
  email?: Maybe<StringWithAggregatesFilter>;
  name?: Maybe<StringWithAggregatesFilter>;
};

export type UserUpdateInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  email?: Maybe<StringFieldUpdateOperationsInput>;
  name?: Maybe<StringFieldUpdateOperationsInput>;
  buckets?: Maybe<BucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<ArticleRefUpdateManyWithoutOwnerInput>;
  feeds?: Maybe<UserFeedUpdateManyWithoutOwnerInput>;
  Subscription?: Maybe<SubscriptionUpdateManyWithoutOwnerInput>;
};

export type UserUpdateManyMutationInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  email?: Maybe<StringFieldUpdateOperationsInput>;
  name?: Maybe<StringFieldUpdateOperationsInput>;
};

export type UserUpdateOneRequiredWithoutArticleRefsInput = {
  create?: Maybe<UserCreateWithoutArticleRefsInput>;
  connectOrCreate?: Maybe<UserCreateOrConnectWithoutArticleRefsInput>;
  upsert?: Maybe<UserUpsertWithoutArticleRefsInput>;
  connect?: Maybe<UserWhereUniqueInput>;
  update?: Maybe<UserUpdateWithoutArticleRefsInput>;
};

export type UserUpdateOneRequiredWithoutBucketsInput = {
  create?: Maybe<UserCreateWithoutBucketsInput>;
  connectOrCreate?: Maybe<UserCreateOrConnectWithoutBucketsInput>;
  upsert?: Maybe<UserUpsertWithoutBucketsInput>;
  connect?: Maybe<UserWhereUniqueInput>;
  update?: Maybe<UserUpdateWithoutBucketsInput>;
};

export type UserUpdateOneRequiredWithoutFeedsInput = {
  create?: Maybe<UserCreateWithoutFeedsInput>;
  connectOrCreate?: Maybe<UserCreateOrConnectWithoutFeedsInput>;
  upsert?: Maybe<UserUpsertWithoutFeedsInput>;
  connect?: Maybe<UserWhereUniqueInput>;
  update?: Maybe<UserUpdateWithoutFeedsInput>;
};

export type UserUpdateOneRequiredWithoutSubscriptionInput = {
  create?: Maybe<UserCreateWithoutSubscriptionInput>;
  connectOrCreate?: Maybe<UserCreateOrConnectWithoutSubscriptionInput>;
  upsert?: Maybe<UserUpsertWithoutSubscriptionInput>;
  connect?: Maybe<UserWhereUniqueInput>;
  update?: Maybe<UserUpdateWithoutSubscriptionInput>;
};

export type UserUpdateWithoutArticleRefsInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  email?: Maybe<StringFieldUpdateOperationsInput>;
  name?: Maybe<StringFieldUpdateOperationsInput>;
  buckets?: Maybe<BucketUpdateManyWithoutOwnerInput>;
  feeds?: Maybe<UserFeedUpdateManyWithoutOwnerInput>;
  Subscription?: Maybe<SubscriptionUpdateManyWithoutOwnerInput>;
};

export type UserUpdateWithoutBucketsInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  email?: Maybe<StringFieldUpdateOperationsInput>;
  name?: Maybe<StringFieldUpdateOperationsInput>;
  articleRefs?: Maybe<ArticleRefUpdateManyWithoutOwnerInput>;
  feeds?: Maybe<UserFeedUpdateManyWithoutOwnerInput>;
  Subscription?: Maybe<SubscriptionUpdateManyWithoutOwnerInput>;
};

export type UserUpdateWithoutFeedsInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  email?: Maybe<StringFieldUpdateOperationsInput>;
  name?: Maybe<StringFieldUpdateOperationsInput>;
  buckets?: Maybe<BucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<ArticleRefUpdateManyWithoutOwnerInput>;
  Subscription?: Maybe<SubscriptionUpdateManyWithoutOwnerInput>;
};

export type UserUpdateWithoutSubscriptionInput = {
  id?: Maybe<StringFieldUpdateOperationsInput>;
  email?: Maybe<StringFieldUpdateOperationsInput>;
  name?: Maybe<StringFieldUpdateOperationsInput>;
  buckets?: Maybe<BucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<ArticleRefUpdateManyWithoutOwnerInput>;
  feeds?: Maybe<UserFeedUpdateManyWithoutOwnerInput>;
};

export type UserUpsertWithoutArticleRefsInput = {
  update: UserUpdateWithoutArticleRefsInput;
  create: UserCreateWithoutArticleRefsInput;
};

export type UserUpsertWithoutBucketsInput = {
  update: UserUpdateWithoutBucketsInput;
  create: UserCreateWithoutBucketsInput;
};

export type UserUpsertWithoutFeedsInput = {
  update: UserUpdateWithoutFeedsInput;
  create: UserCreateWithoutFeedsInput;
};

export type UserUpsertWithoutSubscriptionInput = {
  update: UserUpdateWithoutSubscriptionInput;
  create: UserCreateWithoutSubscriptionInput;
};

export type UserWhereInput = {
  AND?: Maybe<Array<UserWhereInput>>;
  OR?: Maybe<Array<UserWhereInput>>;
  NOT?: Maybe<Array<UserWhereInput>>;
  id?: Maybe<StringFilter>;
  email?: Maybe<StringFilter>;
  name?: Maybe<StringFilter>;
  buckets?: Maybe<BucketListRelationFilter>;
  articleRefs?: Maybe<ArticleRefListRelationFilter>;
  feeds?: Maybe<UserFeedListRelationFilter>;
  Subscription?: Maybe<SubscriptionListRelationFilter>;
};

export type UserWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
  email?: Maybe<Scalars['String']>;
};
