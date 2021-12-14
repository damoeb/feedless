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

export type GqlAggregateArticleExporter = {
  __typename?: 'AggregateArticleExporter';
  _count?: Maybe<FieldWrapper<GqlArticleExporterCountAggregate>>;
  _avg?: Maybe<FieldWrapper<GqlArticleExporterAvgAggregate>>;
  _sum?: Maybe<FieldWrapper<GqlArticleExporterSumAggregate>>;
  _min?: Maybe<FieldWrapper<GqlArticleExporterMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlArticleExporterMaxAggregate>>;
};

export type GqlAggregateArticleExporterTarget = {
  __typename?: 'AggregateArticleExporterTarget';
  _count?: Maybe<FieldWrapper<GqlArticleExporterTargetCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlArticleExporterTargetMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlArticleExporterTargetMaxAggregate>>;
};

export type GqlAggregateArticlePostProcessor = {
  __typename?: 'AggregateArticlePostProcessor';
  _count?: Maybe<FieldWrapper<GqlArticlePostProcessorCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlArticlePostProcessorMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlArticlePostProcessorMaxAggregate>>;
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

export type GqlAggregateEventHook = {
  __typename?: 'AggregateEventHook';
  _count?: Maybe<FieldWrapper<GqlEventHookCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlEventHookMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlEventHookMaxAggregate>>;
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

export type GqlAggregateNoFollowUrl = {
  __typename?: 'AggregateNoFollowUrl';
  _count?: Maybe<FieldWrapper<GqlNoFollowUrlCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlNoFollowUrlMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlNoFollowUrlMaxAggregate>>;
};

export type GqlAggregateNotebook = {
  __typename?: 'AggregateNotebook';
  _count?: Maybe<FieldWrapper<GqlNotebookCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlNotebookMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlNotebookMaxAggregate>>;
};

export type GqlAggregatePlugin = {
  __typename?: 'AggregatePlugin';
  _count?: Maybe<FieldWrapper<GqlPluginCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlPluginMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlPluginMaxAggregate>>;
};

export type GqlAggregateProfileSettings = {
  __typename?: 'AggregateProfileSettings';
  _count?: Maybe<FieldWrapper<GqlProfileSettingsCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlProfileSettingsMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlProfileSettingsMaxAggregate>>;
};

export type GqlAggregateReferencedArticleRef = {
  __typename?: 'AggregateReferencedArticleRef';
  _count?: Maybe<FieldWrapper<GqlReferencedArticleRefCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlReferencedArticleRefMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlReferencedArticleRefMaxAggregate>>;
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

export type GqlArticle = {
  __typename?: 'Article';
  id: FieldWrapper<Scalars['String']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  released: FieldWrapper<Scalars['Boolean']>;
  date_published: FieldWrapper<Scalars['DateTime']>;
  date_modified?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  comment_feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  source_url?: Maybe<FieldWrapper<Scalars['String']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title: FieldWrapper<Scalars['String']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  fulltext_data?: Maybe<FieldWrapper<Scalars['String']>>;
  content_raw_mime?: Maybe<FieldWrapper<Scalars['String']>>;
  content_raw: FieldWrapper<Scalars['String']>;
  content_text?: Maybe<FieldWrapper<Scalars['String']>>;
  has_harvest?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_readability?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_video: FieldWrapper<Scalars['Boolean']>;
  has_audio: FieldWrapper<Scalars['Boolean']>;
  length_video?: Maybe<FieldWrapper<Scalars['Int']>>;
  length_audio?: Maybe<FieldWrapper<Scalars['Int']>>;
  word_count_text?: Maybe<FieldWrapper<Scalars['Int']>>;
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
  lastScoredAt: FieldWrapper<Scalars['DateTime']>;
  enclosure?: Maybe<FieldWrapper<Scalars['JSON']>>;
  data_json_map?: Maybe<FieldWrapper<Scalars['JSON']>>;
  readability?: Maybe<FieldWrapper<Scalars['JSON']>>;
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
  length_video?: Maybe<FieldWrapper<Scalars['Float']>>;
  length_audio?: Maybe<FieldWrapper<Scalars['Float']>>;
  word_count_text?: Maybe<FieldWrapper<Scalars['Float']>>;
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
};

export type GqlArticleCountAggregate = {
  __typename?: 'ArticleCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  released: FieldWrapper<Scalars['Int']>;
  date_published: FieldWrapper<Scalars['Int']>;
  date_modified: FieldWrapper<Scalars['Int']>;
  comment_feed_url: FieldWrapper<Scalars['Int']>;
  source_url: FieldWrapper<Scalars['Int']>;
  url: FieldWrapper<Scalars['Int']>;
  author: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  tags: FieldWrapper<Scalars['Int']>;
  fulltext_data: FieldWrapper<Scalars['Int']>;
  content_raw_mime: FieldWrapper<Scalars['Int']>;
  content_raw: FieldWrapper<Scalars['Int']>;
  content_text: FieldWrapper<Scalars['Int']>;
  has_harvest: FieldWrapper<Scalars['Int']>;
  has_readability: FieldWrapper<Scalars['Int']>;
  has_video: FieldWrapper<Scalars['Int']>;
  has_audio: FieldWrapper<Scalars['Int']>;
  length_video: FieldWrapper<Scalars['Int']>;
  length_audio: FieldWrapper<Scalars['Int']>;
  word_count_text: FieldWrapper<Scalars['Int']>;
  score: FieldWrapper<Scalars['Int']>;
  lastScoredAt: FieldWrapper<Scalars['Int']>;
  enclosure: FieldWrapper<Scalars['Int']>;
  data_json_map: FieldWrapper<Scalars['Int']>;
  readability: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlArticleCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  released?: Maybe<Scalars['Boolean']>;
  date_published?: Maybe<Scalars['DateTime']>;
  date_modified?: Maybe<Scalars['DateTime']>;
  comment_feed_url?: Maybe<Scalars['String']>;
  source_url?: Maybe<Scalars['String']>;
  url?: Maybe<Scalars['String']>;
  author?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  fulltext_data?: Maybe<Scalars['String']>;
  content_raw_mime?: Maybe<Scalars['String']>;
  content_raw: Scalars['String'];
  content_text?: Maybe<Scalars['String']>;
  has_harvest?: Maybe<Scalars['Boolean']>;
  has_readability?: Maybe<Scalars['Boolean']>;
  has_video?: Maybe<Scalars['Boolean']>;
  has_audio?: Maybe<Scalars['Boolean']>;
  length_video?: Maybe<Scalars['Int']>;
  length_audio?: Maybe<Scalars['Int']>;
  word_count_text?: Maybe<Scalars['Int']>;
  score?: Maybe<Scalars['Float']>;
  lastScoredAt?: Maybe<Scalars['DateTime']>;
  enclosure?: Maybe<Scalars['JSON']>;
  data_json_map?: Maybe<Scalars['JSON']>;
  readability?: Maybe<Scalars['JSON']>;
  articleRef?: Maybe<GqlArticleRefCreateNestedManyWithoutArticleInput>;
};

export type GqlArticleCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  released?: Maybe<Scalars['Boolean']>;
  date_published?: Maybe<Scalars['DateTime']>;
  date_modified?: Maybe<Scalars['DateTime']>;
  comment_feed_url?: Maybe<Scalars['String']>;
  source_url?: Maybe<Scalars['String']>;
  url?: Maybe<Scalars['String']>;
  author?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  fulltext_data?: Maybe<Scalars['String']>;
  content_raw_mime?: Maybe<Scalars['String']>;
  content_raw: Scalars['String'];
  content_text?: Maybe<Scalars['String']>;
  has_harvest?: Maybe<Scalars['Boolean']>;
  has_readability?: Maybe<Scalars['Boolean']>;
  has_video?: Maybe<Scalars['Boolean']>;
  has_audio?: Maybe<Scalars['Boolean']>;
  length_video?: Maybe<Scalars['Int']>;
  length_audio?: Maybe<Scalars['Int']>;
  word_count_text?: Maybe<Scalars['Int']>;
  score?: Maybe<Scalars['Float']>;
  lastScoredAt?: Maybe<Scalars['DateTime']>;
  enclosure?: Maybe<Scalars['JSON']>;
  data_json_map?: Maybe<Scalars['JSON']>;
  readability?: Maybe<Scalars['JSON']>;
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
  updatedAt?: Maybe<Scalars['DateTime']>;
  released?: Maybe<Scalars['Boolean']>;
  date_published?: Maybe<Scalars['DateTime']>;
  date_modified?: Maybe<Scalars['DateTime']>;
  comment_feed_url?: Maybe<Scalars['String']>;
  source_url?: Maybe<Scalars['String']>;
  url?: Maybe<Scalars['String']>;
  author?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  fulltext_data?: Maybe<Scalars['String']>;
  content_raw_mime?: Maybe<Scalars['String']>;
  content_raw: Scalars['String'];
  content_text?: Maybe<Scalars['String']>;
  has_harvest?: Maybe<Scalars['Boolean']>;
  has_readability?: Maybe<Scalars['Boolean']>;
  has_video?: Maybe<Scalars['Boolean']>;
  has_audio?: Maybe<Scalars['Boolean']>;
  length_video?: Maybe<Scalars['Int']>;
  length_audio?: Maybe<Scalars['Int']>;
  word_count_text?: Maybe<Scalars['Int']>;
  score?: Maybe<Scalars['Float']>;
  lastScoredAt?: Maybe<Scalars['DateTime']>;
  enclosure?: Maybe<Scalars['JSON']>;
  data_json_map?: Maybe<Scalars['JSON']>;
  readability?: Maybe<Scalars['JSON']>;
};

export type GqlArticleExporter = {
  __typename?: 'ArticleExporter';
  id: FieldWrapper<Scalars['String']>;
  segment: FieldWrapper<Scalars['Boolean']>;
  segment_sort_field?: Maybe<FieldWrapper<Scalars['String']>>;
  segment_sort_asc: FieldWrapper<Scalars['Boolean']>;
  segment_size?: Maybe<FieldWrapper<Scalars['Int']>>;
  segment_digest?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_refresh_on: FieldWrapper<Scalars['String']>;
  trigger_scheduled_last_at?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_scheduled_next_at?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_scheduled?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId: FieldWrapper<Scalars['String']>;
  targets: Array<FieldWrapper<GqlArticleExporterTarget>>;
  bucket: FieldWrapper<GqlBucket>;
};


export type GqlArticleExporterTargetsArgs = {
  where?: Maybe<GqlArticleExporterTargetWhereInput>;
  orderBy?: Maybe<Array<GqlArticleExporterTargetOrderByInput>>;
  cursor?: Maybe<GqlArticleExporterTargetWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleExporterTargetScalarFieldEnum>>;
};

export type GqlArticleExporterAvgAggregate = {
  __typename?: 'ArticleExporterAvgAggregate';
  segment_size?: Maybe<FieldWrapper<Scalars['Float']>>;
};

export type GqlArticleExporterCountAggregate = {
  __typename?: 'ArticleExporterCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  segment: FieldWrapper<Scalars['Int']>;
  segment_sort_field: FieldWrapper<Scalars['Int']>;
  segment_sort_asc: FieldWrapper<Scalars['Int']>;
  segment_size: FieldWrapper<Scalars['Int']>;
  segment_digest: FieldWrapper<Scalars['Int']>;
  lastUpdatedAt: FieldWrapper<Scalars['Int']>;
  trigger_refresh_on: FieldWrapper<Scalars['Int']>;
  trigger_scheduled_last_at: FieldWrapper<Scalars['Int']>;
  trigger_scheduled_next_at: FieldWrapper<Scalars['Int']>;
  trigger_scheduled: FieldWrapper<Scalars['Int']>;
  bucketId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlArticleExporterCreateInput = {
  id?: Maybe<Scalars['String']>;
  segment?: Maybe<Scalars['Boolean']>;
  segment_sort_field?: Maybe<Scalars['String']>;
  segment_sort_asc?: Maybe<Scalars['Boolean']>;
  segment_size?: Maybe<Scalars['Int']>;
  segment_digest?: Maybe<Scalars['Boolean']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  trigger_refresh_on?: Maybe<Scalars['String']>;
  trigger_scheduled_last_at?: Maybe<Scalars['DateTime']>;
  trigger_scheduled_next_at?: Maybe<Scalars['DateTime']>;
  trigger_scheduled?: Maybe<Scalars['String']>;
  targets?: Maybe<GqlArticleExporterTargetCreateNestedManyWithoutExporterInput>;
  bucket: GqlBucketCreateNestedOneWithoutExportersInput;
};

export type GqlArticleExporterCreateManyBucketInput = {
  id?: Maybe<Scalars['String']>;
  segment?: Maybe<Scalars['Boolean']>;
  segment_sort_field?: Maybe<Scalars['String']>;
  segment_sort_asc?: Maybe<Scalars['Boolean']>;
  segment_size?: Maybe<Scalars['Int']>;
  segment_digest?: Maybe<Scalars['Boolean']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  trigger_refresh_on?: Maybe<Scalars['String']>;
  trigger_scheduled_last_at?: Maybe<Scalars['DateTime']>;
  trigger_scheduled_next_at?: Maybe<Scalars['DateTime']>;
  trigger_scheduled?: Maybe<Scalars['String']>;
};

export type GqlArticleExporterCreateManyBucketInputEnvelope = {
  data: Array<GqlArticleExporterCreateManyBucketInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlArticleExporterCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  segment?: Maybe<Scalars['Boolean']>;
  segment_sort_field?: Maybe<Scalars['String']>;
  segment_sort_asc?: Maybe<Scalars['Boolean']>;
  segment_size?: Maybe<Scalars['Int']>;
  segment_digest?: Maybe<Scalars['Boolean']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  trigger_refresh_on?: Maybe<Scalars['String']>;
  trigger_scheduled_last_at?: Maybe<Scalars['DateTime']>;
  trigger_scheduled_next_at?: Maybe<Scalars['DateTime']>;
  trigger_scheduled?: Maybe<Scalars['String']>;
  bucketId: Scalars['String'];
};

export type GqlArticleExporterCreateNestedManyWithoutBucketInput = {
  create?: Maybe<Array<GqlArticleExporterCreateWithoutBucketInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleExporterCreateOrConnectWithoutBucketInput>>;
  createMany?: Maybe<GqlArticleExporterCreateManyBucketInputEnvelope>;
  connect?: Maybe<Array<GqlArticleExporterWhereUniqueInput>>;
};

export type GqlArticleExporterCreateNestedOneWithoutTargetsInput = {
  create?: Maybe<GqlArticleExporterCreateWithoutTargetsInput>;
  connectOrCreate?: Maybe<GqlArticleExporterCreateOrConnectWithoutTargetsInput>;
  connect?: Maybe<GqlArticleExporterWhereUniqueInput>;
};

export type GqlArticleExporterCreateOrConnectWithoutBucketInput = {
  where: GqlArticleExporterWhereUniqueInput;
  create: GqlArticleExporterCreateWithoutBucketInput;
};

export type GqlArticleExporterCreateOrConnectWithoutTargetsInput = {
  where: GqlArticleExporterWhereUniqueInput;
  create: GqlArticleExporterCreateWithoutTargetsInput;
};

export type GqlArticleExporterCreateWithoutBucketInput = {
  id?: Maybe<Scalars['String']>;
  segment?: Maybe<Scalars['Boolean']>;
  segment_sort_field?: Maybe<Scalars['String']>;
  segment_sort_asc?: Maybe<Scalars['Boolean']>;
  segment_size?: Maybe<Scalars['Int']>;
  segment_digest?: Maybe<Scalars['Boolean']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  trigger_refresh_on?: Maybe<Scalars['String']>;
  trigger_scheduled_last_at?: Maybe<Scalars['DateTime']>;
  trigger_scheduled_next_at?: Maybe<Scalars['DateTime']>;
  trigger_scheduled?: Maybe<Scalars['String']>;
  targets?: Maybe<GqlArticleExporterTargetCreateNestedManyWithoutExporterInput>;
};

export type GqlArticleExporterCreateWithoutTargetsInput = {
  id?: Maybe<Scalars['String']>;
  segment?: Maybe<Scalars['Boolean']>;
  segment_sort_field?: Maybe<Scalars['String']>;
  segment_sort_asc?: Maybe<Scalars['Boolean']>;
  segment_size?: Maybe<Scalars['Int']>;
  segment_digest?: Maybe<Scalars['Boolean']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  trigger_refresh_on?: Maybe<Scalars['String']>;
  trigger_scheduled_last_at?: Maybe<Scalars['DateTime']>;
  trigger_scheduled_next_at?: Maybe<Scalars['DateTime']>;
  trigger_scheduled?: Maybe<Scalars['String']>;
  bucket: GqlBucketCreateNestedOneWithoutExportersInput;
};

export type GqlArticleExporterGroupBy = {
  __typename?: 'ArticleExporterGroupBy';
  id: FieldWrapper<Scalars['String']>;
  segment: FieldWrapper<Scalars['Boolean']>;
  segment_sort_field?: Maybe<FieldWrapper<Scalars['String']>>;
  segment_sort_asc: FieldWrapper<Scalars['Boolean']>;
  segment_size?: Maybe<FieldWrapper<Scalars['Int']>>;
  segment_digest?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_refresh_on: FieldWrapper<Scalars['String']>;
  trigger_scheduled_last_at?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_scheduled_next_at?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_scheduled?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlArticleExporterCountAggregate>>;
  _avg?: Maybe<FieldWrapper<GqlArticleExporterAvgAggregate>>;
  _sum?: Maybe<FieldWrapper<GqlArticleExporterSumAggregate>>;
  _min?: Maybe<FieldWrapper<GqlArticleExporterMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlArticleExporterMaxAggregate>>;
};

export type GqlArticleExporterListRelationFilter = {
  every?: Maybe<GqlArticleExporterWhereInput>;
  some?: Maybe<GqlArticleExporterWhereInput>;
  none?: Maybe<GqlArticleExporterWhereInput>;
};

export type GqlArticleExporterMaxAggregate = {
  __typename?: 'ArticleExporterMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  segment?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  segment_sort_field?: Maybe<FieldWrapper<Scalars['String']>>;
  segment_sort_asc?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  segment_size?: Maybe<FieldWrapper<Scalars['Int']>>;
  segment_digest?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_refresh_on?: Maybe<FieldWrapper<Scalars['String']>>;
  trigger_scheduled_last_at?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_scheduled_next_at?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_scheduled?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleExporterMinAggregate = {
  __typename?: 'ArticleExporterMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  segment?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  segment_sort_field?: Maybe<FieldWrapper<Scalars['String']>>;
  segment_sort_asc?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  segment_size?: Maybe<FieldWrapper<Scalars['Int']>>;
  segment_digest?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_refresh_on?: Maybe<FieldWrapper<Scalars['String']>>;
  trigger_scheduled_last_at?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_scheduled_next_at?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  trigger_scheduled?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleExporterOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  segment?: Maybe<GqlSortOrder>;
  segment_sort_field?: Maybe<GqlSortOrder>;
  segment_sort_asc?: Maybe<GqlSortOrder>;
  segment_size?: Maybe<GqlSortOrder>;
  segment_digest?: Maybe<GqlSortOrder>;
  lastUpdatedAt?: Maybe<GqlSortOrder>;
  trigger_refresh_on?: Maybe<GqlSortOrder>;
  trigger_scheduled_last_at?: Maybe<GqlSortOrder>;
  trigger_scheduled_next_at?: Maybe<GqlSortOrder>;
  trigger_scheduled?: Maybe<GqlSortOrder>;
  bucketId?: Maybe<GqlSortOrder>;
};

export type GqlArticleExporterRelationFilter = {
  is?: Maybe<GqlArticleExporterWhereInput>;
  isNot?: Maybe<GqlArticleExporterWhereInput>;
};

export enum GqlArticleExporterScalarFieldEnum {
  Id = 'id',
  Segment = 'segment',
  SegmentSortField = 'segment_sort_field',
  SegmentSortAsc = 'segment_sort_asc',
  SegmentSize = 'segment_size',
  SegmentDigest = 'segment_digest',
  LastUpdatedAt = 'lastUpdatedAt',
  TriggerRefreshOn = 'trigger_refresh_on',
  TriggerScheduledLastAt = 'trigger_scheduled_last_at',
  TriggerScheduledNextAt = 'trigger_scheduled_next_at',
  TriggerScheduled = 'trigger_scheduled',
  BucketId = 'bucketId'
}

export type GqlArticleExporterScalarWhereInput = {
  AND?: Maybe<Array<GqlArticleExporterScalarWhereInput>>;
  OR?: Maybe<Array<GqlArticleExporterScalarWhereInput>>;
  NOT?: Maybe<Array<GqlArticleExporterScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  segment?: Maybe<GqlBoolFilter>;
  segment_sort_field?: Maybe<GqlStringNullableFilter>;
  segment_sort_asc?: Maybe<GqlBoolFilter>;
  segment_size?: Maybe<GqlIntNullableFilter>;
  segment_digest?: Maybe<GqlBoolNullableFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableFilter>;
  trigger_refresh_on?: Maybe<GqlStringFilter>;
  trigger_scheduled_last_at?: Maybe<GqlDateTimeNullableFilter>;
  trigger_scheduled_next_at?: Maybe<GqlDateTimeNullableFilter>;
  trigger_scheduled?: Maybe<GqlStringNullableFilter>;
  bucketId?: Maybe<GqlStringFilter>;
};

export type GqlArticleExporterScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlArticleExporterScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlArticleExporterScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlArticleExporterScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  segment?: Maybe<GqlBoolWithAggregatesFilter>;
  segment_sort_field?: Maybe<GqlStringNullableWithAggregatesFilter>;
  segment_sort_asc?: Maybe<GqlBoolWithAggregatesFilter>;
  segment_size?: Maybe<GqlIntNullableWithAggregatesFilter>;
  segment_digest?: Maybe<GqlBoolNullableWithAggregatesFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
  trigger_refresh_on?: Maybe<GqlStringWithAggregatesFilter>;
  trigger_scheduled_last_at?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
  trigger_scheduled_next_at?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
  trigger_scheduled?: Maybe<GqlStringNullableWithAggregatesFilter>;
  bucketId?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlArticleExporterSumAggregate = {
  __typename?: 'ArticleExporterSumAggregate';
  segment_size?: Maybe<FieldWrapper<Scalars['Int']>>;
};

export type GqlArticleExporterTarget = {
  __typename?: 'ArticleExporterTarget';
  id: FieldWrapper<Scalars['String']>;
  type: FieldWrapper<Scalars['String']>;
  context?: Maybe<FieldWrapper<Scalars['String']>>;
  forward_errors: FieldWrapper<Scalars['Boolean']>;
  exporterId: FieldWrapper<Scalars['String']>;
  exporter: FieldWrapper<GqlArticleExporter>;
};

export type GqlArticleExporterTargetCountAggregate = {
  __typename?: 'ArticleExporterTargetCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  type: FieldWrapper<Scalars['Int']>;
  context: FieldWrapper<Scalars['Int']>;
  forward_errors: FieldWrapper<Scalars['Int']>;
  exporterId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlArticleExporterTargetCreateInput = {
  id?: Maybe<Scalars['String']>;
  type: Scalars['String'];
  context?: Maybe<Scalars['String']>;
  forward_errors?: Maybe<Scalars['Boolean']>;
  exporter: GqlArticleExporterCreateNestedOneWithoutTargetsInput;
};

export type GqlArticleExporterTargetCreateManyExporterInput = {
  id?: Maybe<Scalars['String']>;
  type: Scalars['String'];
  context?: Maybe<Scalars['String']>;
  forward_errors?: Maybe<Scalars['Boolean']>;
};

export type GqlArticleExporterTargetCreateManyExporterInputEnvelope = {
  data: Array<GqlArticleExporterTargetCreateManyExporterInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlArticleExporterTargetCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  type: Scalars['String'];
  context?: Maybe<Scalars['String']>;
  forward_errors?: Maybe<Scalars['Boolean']>;
  exporterId: Scalars['String'];
};

export type GqlArticleExporterTargetCreateNestedManyWithoutExporterInput = {
  create?: Maybe<Array<GqlArticleExporterTargetCreateWithoutExporterInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleExporterTargetCreateOrConnectWithoutExporterInput>>;
  createMany?: Maybe<GqlArticleExporterTargetCreateManyExporterInputEnvelope>;
  connect?: Maybe<Array<GqlArticleExporterTargetWhereUniqueInput>>;
};

export type GqlArticleExporterTargetCreateOrConnectWithoutExporterInput = {
  where: GqlArticleExporterTargetWhereUniqueInput;
  create: GqlArticleExporterTargetCreateWithoutExporterInput;
};

export type GqlArticleExporterTargetCreateWithoutExporterInput = {
  id?: Maybe<Scalars['String']>;
  type: Scalars['String'];
  context?: Maybe<Scalars['String']>;
  forward_errors?: Maybe<Scalars['Boolean']>;
};

export type GqlArticleExporterTargetGroupBy = {
  __typename?: 'ArticleExporterTargetGroupBy';
  id: FieldWrapper<Scalars['String']>;
  type: FieldWrapper<Scalars['String']>;
  context?: Maybe<FieldWrapper<Scalars['String']>>;
  forward_errors: FieldWrapper<Scalars['Boolean']>;
  exporterId: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlArticleExporterTargetCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlArticleExporterTargetMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlArticleExporterTargetMaxAggregate>>;
};

export type GqlArticleExporterTargetListRelationFilter = {
  every?: Maybe<GqlArticleExporterTargetWhereInput>;
  some?: Maybe<GqlArticleExporterTargetWhereInput>;
  none?: Maybe<GqlArticleExporterTargetWhereInput>;
};

export type GqlArticleExporterTargetMaxAggregate = {
  __typename?: 'ArticleExporterTargetMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
  context?: Maybe<FieldWrapper<Scalars['String']>>;
  forward_errors?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  exporterId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleExporterTargetMinAggregate = {
  __typename?: 'ArticleExporterTargetMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
  context?: Maybe<FieldWrapper<Scalars['String']>>;
  forward_errors?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  exporterId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleExporterTargetOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  type?: Maybe<GqlSortOrder>;
  context?: Maybe<GqlSortOrder>;
  forward_errors?: Maybe<GqlSortOrder>;
  exporterId?: Maybe<GqlSortOrder>;
};

export enum GqlArticleExporterTargetScalarFieldEnum {
  Id = 'id',
  Type = 'type',
  Context = 'context',
  ForwardErrors = 'forward_errors',
  ExporterId = 'exporterId'
}

export type GqlArticleExporterTargetScalarWhereInput = {
  AND?: Maybe<Array<GqlArticleExporterTargetScalarWhereInput>>;
  OR?: Maybe<Array<GqlArticleExporterTargetScalarWhereInput>>;
  NOT?: Maybe<Array<GqlArticleExporterTargetScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  type?: Maybe<GqlStringFilter>;
  context?: Maybe<GqlStringNullableFilter>;
  forward_errors?: Maybe<GqlBoolFilter>;
  exporterId?: Maybe<GqlStringFilter>;
};

export type GqlArticleExporterTargetScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlArticleExporterTargetScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlArticleExporterTargetScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlArticleExporterTargetScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  type?: Maybe<GqlStringWithAggregatesFilter>;
  context?: Maybe<GqlStringNullableWithAggregatesFilter>;
  forward_errors?: Maybe<GqlBoolWithAggregatesFilter>;
  exporterId?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlArticleExporterTargetUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  context?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  forward_errors?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  exporter?: Maybe<GqlArticleExporterUpdateOneRequiredWithoutTargetsInput>;
};

export type GqlArticleExporterTargetUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  context?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  forward_errors?: Maybe<GqlBoolFieldUpdateOperationsInput>;
};

export type GqlArticleExporterTargetUpdateManyWithWhereWithoutExporterInput = {
  where: GqlArticleExporterTargetScalarWhereInput;
  data: GqlArticleExporterTargetUpdateManyMutationInput;
};

export type GqlArticleExporterTargetUpdateManyWithoutExporterInput = {
  create?: Maybe<Array<GqlArticleExporterTargetCreateWithoutExporterInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleExporterTargetCreateOrConnectWithoutExporterInput>>;
  upsert?: Maybe<Array<GqlArticleExporterTargetUpsertWithWhereUniqueWithoutExporterInput>>;
  createMany?: Maybe<GqlArticleExporterTargetCreateManyExporterInputEnvelope>;
  connect?: Maybe<Array<GqlArticleExporterTargetWhereUniqueInput>>;
  set?: Maybe<Array<GqlArticleExporterTargetWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlArticleExporterTargetWhereUniqueInput>>;
  delete?: Maybe<Array<GqlArticleExporterTargetWhereUniqueInput>>;
  update?: Maybe<Array<GqlArticleExporterTargetUpdateWithWhereUniqueWithoutExporterInput>>;
  updateMany?: Maybe<Array<GqlArticleExporterTargetUpdateManyWithWhereWithoutExporterInput>>;
  deleteMany?: Maybe<Array<GqlArticleExporterTargetScalarWhereInput>>;
};

export type GqlArticleExporterTargetUpdateWithWhereUniqueWithoutExporterInput = {
  where: GqlArticleExporterTargetWhereUniqueInput;
  data: GqlArticleExporterTargetUpdateWithoutExporterInput;
};

export type GqlArticleExporterTargetUpdateWithoutExporterInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  context?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  forward_errors?: Maybe<GqlBoolFieldUpdateOperationsInput>;
};

export type GqlArticleExporterTargetUpsertWithWhereUniqueWithoutExporterInput = {
  where: GqlArticleExporterTargetWhereUniqueInput;
  update: GqlArticleExporterTargetUpdateWithoutExporterInput;
  create: GqlArticleExporterTargetCreateWithoutExporterInput;
};

export type GqlArticleExporterTargetWhereInput = {
  AND?: Maybe<Array<GqlArticleExporterTargetWhereInput>>;
  OR?: Maybe<Array<GqlArticleExporterTargetWhereInput>>;
  NOT?: Maybe<Array<GqlArticleExporterTargetWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  type?: Maybe<GqlStringFilter>;
  context?: Maybe<GqlStringNullableFilter>;
  forward_errors?: Maybe<GqlBoolFilter>;
  exporter?: Maybe<GqlArticleExporterRelationFilter>;
  exporterId?: Maybe<GqlStringFilter>;
};

export type GqlArticleExporterTargetWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlArticleExporterUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  segment?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  segment_sort_field?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  segment_sort_asc?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  segment_size?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  segment_digest?: Maybe<GqlNullableBoolFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_refresh_on?: Maybe<GqlStringFieldUpdateOperationsInput>;
  trigger_scheduled_last_at?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_scheduled_next_at?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_scheduled?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  targets?: Maybe<GqlArticleExporterTargetUpdateManyWithoutExporterInput>;
  bucket?: Maybe<GqlBucketUpdateOneRequiredWithoutExportersInput>;
};

export type GqlArticleExporterUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  segment?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  segment_sort_field?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  segment_sort_asc?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  segment_size?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  segment_digest?: Maybe<GqlNullableBoolFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_refresh_on?: Maybe<GqlStringFieldUpdateOperationsInput>;
  trigger_scheduled_last_at?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_scheduled_next_at?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_scheduled?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
};

export type GqlArticleExporterUpdateManyWithWhereWithoutBucketInput = {
  where: GqlArticleExporterScalarWhereInput;
  data: GqlArticleExporterUpdateManyMutationInput;
};

export type GqlArticleExporterUpdateManyWithoutBucketInput = {
  create?: Maybe<Array<GqlArticleExporterCreateWithoutBucketInput>>;
  connectOrCreate?: Maybe<Array<GqlArticleExporterCreateOrConnectWithoutBucketInput>>;
  upsert?: Maybe<Array<GqlArticleExporterUpsertWithWhereUniqueWithoutBucketInput>>;
  createMany?: Maybe<GqlArticleExporterCreateManyBucketInputEnvelope>;
  connect?: Maybe<Array<GqlArticleExporterWhereUniqueInput>>;
  set?: Maybe<Array<GqlArticleExporterWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlArticleExporterWhereUniqueInput>>;
  delete?: Maybe<Array<GqlArticleExporterWhereUniqueInput>>;
  update?: Maybe<Array<GqlArticleExporterUpdateWithWhereUniqueWithoutBucketInput>>;
  updateMany?: Maybe<Array<GqlArticleExporterUpdateManyWithWhereWithoutBucketInput>>;
  deleteMany?: Maybe<Array<GqlArticleExporterScalarWhereInput>>;
};

export type GqlArticleExporterUpdateOneRequiredWithoutTargetsInput = {
  create?: Maybe<GqlArticleExporterCreateWithoutTargetsInput>;
  connectOrCreate?: Maybe<GqlArticleExporterCreateOrConnectWithoutTargetsInput>;
  upsert?: Maybe<GqlArticleExporterUpsertWithoutTargetsInput>;
  connect?: Maybe<GqlArticleExporterWhereUniqueInput>;
  update?: Maybe<GqlArticleExporterUpdateWithoutTargetsInput>;
};

export type GqlArticleExporterUpdateWithWhereUniqueWithoutBucketInput = {
  where: GqlArticleExporterWhereUniqueInput;
  data: GqlArticleExporterUpdateWithoutBucketInput;
};

export type GqlArticleExporterUpdateWithoutBucketInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  segment?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  segment_sort_field?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  segment_sort_asc?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  segment_size?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  segment_digest?: Maybe<GqlNullableBoolFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_refresh_on?: Maybe<GqlStringFieldUpdateOperationsInput>;
  trigger_scheduled_last_at?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_scheduled_next_at?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_scheduled?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  targets?: Maybe<GqlArticleExporterTargetUpdateManyWithoutExporterInput>;
};

export type GqlArticleExporterUpdateWithoutTargetsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  segment?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  segment_sort_field?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  segment_sort_asc?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  segment_size?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  segment_digest?: Maybe<GqlNullableBoolFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_refresh_on?: Maybe<GqlStringFieldUpdateOperationsInput>;
  trigger_scheduled_last_at?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_scheduled_next_at?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  trigger_scheduled?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  bucket?: Maybe<GqlBucketUpdateOneRequiredWithoutExportersInput>;
};

export type GqlArticleExporterUpsertWithWhereUniqueWithoutBucketInput = {
  where: GqlArticleExporterWhereUniqueInput;
  update: GqlArticleExporterUpdateWithoutBucketInput;
  create: GqlArticleExporterCreateWithoutBucketInput;
};

export type GqlArticleExporterUpsertWithoutTargetsInput = {
  update: GqlArticleExporterUpdateWithoutTargetsInput;
  create: GqlArticleExporterCreateWithoutTargetsInput;
};

export type GqlArticleExporterWhereInput = {
  AND?: Maybe<Array<GqlArticleExporterWhereInput>>;
  OR?: Maybe<Array<GqlArticleExporterWhereInput>>;
  NOT?: Maybe<Array<GqlArticleExporterWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  segment?: Maybe<GqlBoolFilter>;
  segment_sort_field?: Maybe<GqlStringNullableFilter>;
  segment_sort_asc?: Maybe<GqlBoolFilter>;
  segment_size?: Maybe<GqlIntNullableFilter>;
  segment_digest?: Maybe<GqlBoolNullableFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableFilter>;
  trigger_refresh_on?: Maybe<GqlStringFilter>;
  trigger_scheduled_last_at?: Maybe<GqlDateTimeNullableFilter>;
  trigger_scheduled_next_at?: Maybe<GqlDateTimeNullableFilter>;
  trigger_scheduled?: Maybe<GqlStringNullableFilter>;
  targets?: Maybe<GqlArticleExporterTargetListRelationFilter>;
  bucket?: Maybe<GqlBucketRelationFilter>;
  bucketId?: Maybe<GqlStringFilter>;
};

export type GqlArticleExporterWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlArticleGroupBy = {
  __typename?: 'ArticleGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  released: FieldWrapper<Scalars['Boolean']>;
  date_published: FieldWrapper<Scalars['DateTime']>;
  date_modified?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  comment_feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  source_url?: Maybe<FieldWrapper<Scalars['String']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title: FieldWrapper<Scalars['String']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  fulltext_data?: Maybe<FieldWrapper<Scalars['String']>>;
  content_raw_mime?: Maybe<FieldWrapper<Scalars['String']>>;
  content_raw: FieldWrapper<Scalars['String']>;
  content_text?: Maybe<FieldWrapper<Scalars['String']>>;
  has_harvest?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_readability?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_video: FieldWrapper<Scalars['Boolean']>;
  has_audio: FieldWrapper<Scalars['Boolean']>;
  length_video?: Maybe<FieldWrapper<Scalars['Int']>>;
  length_audio?: Maybe<FieldWrapper<Scalars['Int']>>;
  word_count_text?: Maybe<FieldWrapper<Scalars['Int']>>;
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
  lastScoredAt: FieldWrapper<Scalars['DateTime']>;
  enclosure?: Maybe<FieldWrapper<Scalars['JSON']>>;
  data_json_map?: Maybe<FieldWrapper<Scalars['JSON']>>;
  readability?: Maybe<FieldWrapper<Scalars['JSON']>>;
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
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  released?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  date_published?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  date_modified?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  comment_feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  source_url?: Maybe<FieldWrapper<Scalars['String']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  fulltext_data?: Maybe<FieldWrapper<Scalars['String']>>;
  content_raw_mime?: Maybe<FieldWrapper<Scalars['String']>>;
  content_raw?: Maybe<FieldWrapper<Scalars['String']>>;
  content_text?: Maybe<FieldWrapper<Scalars['String']>>;
  has_harvest?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_readability?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_video?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_audio?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  length_video?: Maybe<FieldWrapper<Scalars['Int']>>;
  length_audio?: Maybe<FieldWrapper<Scalars['Int']>>;
  word_count_text?: Maybe<FieldWrapper<Scalars['Int']>>;
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
  lastScoredAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlArticleMinAggregate = {
  __typename?: 'ArticleMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  released?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  date_published?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  date_modified?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  comment_feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  source_url?: Maybe<FieldWrapper<Scalars['String']>>;
  url?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  fulltext_data?: Maybe<FieldWrapper<Scalars['String']>>;
  content_raw_mime?: Maybe<FieldWrapper<Scalars['String']>>;
  content_raw?: Maybe<FieldWrapper<Scalars['String']>>;
  content_text?: Maybe<FieldWrapper<Scalars['String']>>;
  has_harvest?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_readability?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_video?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_audio?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  length_video?: Maybe<FieldWrapper<Scalars['Int']>>;
  length_audio?: Maybe<FieldWrapper<Scalars['Int']>>;
  word_count_text?: Maybe<FieldWrapper<Scalars['Int']>>;
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
  lastScoredAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlArticleOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  released?: Maybe<GqlSortOrder>;
  date_published?: Maybe<GqlSortOrder>;
  date_modified?: Maybe<GqlSortOrder>;
  comment_feed_url?: Maybe<GqlSortOrder>;
  source_url?: Maybe<GqlSortOrder>;
  url?: Maybe<GqlSortOrder>;
  author?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  tags?: Maybe<GqlSortOrder>;
  fulltext_data?: Maybe<GqlSortOrder>;
  content_raw_mime?: Maybe<GqlSortOrder>;
  content_raw?: Maybe<GqlSortOrder>;
  content_text?: Maybe<GqlSortOrder>;
  has_harvest?: Maybe<GqlSortOrder>;
  has_readability?: Maybe<GqlSortOrder>;
  has_video?: Maybe<GqlSortOrder>;
  has_audio?: Maybe<GqlSortOrder>;
  length_video?: Maybe<GqlSortOrder>;
  length_audio?: Maybe<GqlSortOrder>;
  word_count_text?: Maybe<GqlSortOrder>;
  score?: Maybe<GqlSortOrder>;
  lastScoredAt?: Maybe<GqlSortOrder>;
  enclosure?: Maybe<GqlSortOrder>;
  data_json_map?: Maybe<GqlSortOrder>;
  readability?: Maybe<GqlSortOrder>;
};

export type GqlArticlePostProcessor = {
  __typename?: 'ArticlePostProcessor';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  type: FieldWrapper<Scalars['String']>;
  context?: Maybe<FieldWrapper<Scalars['String']>>;
  buckets: Array<FieldWrapper<GqlBucket>>;
};


export type GqlArticlePostProcessorBucketsArgs = {
  where?: Maybe<GqlBucketWhereInput>;
  orderBy?: Maybe<Array<GqlBucketOrderByInput>>;
  cursor?: Maybe<GqlBucketWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlBucketScalarFieldEnum>>;
};

export type GqlArticlePostProcessorCountAggregate = {
  __typename?: 'ArticlePostProcessorCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  type: FieldWrapper<Scalars['Int']>;
  context: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlArticlePostProcessorCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  type: Scalars['String'];
  context?: Maybe<Scalars['String']>;
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutPostProcessorsInput>;
};

export type GqlArticlePostProcessorCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  type: Scalars['String'];
  context?: Maybe<Scalars['String']>;
};

export type GqlArticlePostProcessorCreateNestedManyWithoutBucketsInput = {
  create?: Maybe<Array<GqlArticlePostProcessorCreateWithoutBucketsInput>>;
  connectOrCreate?: Maybe<Array<GqlArticlePostProcessorCreateOrConnectWithoutBucketsInput>>;
  connect?: Maybe<Array<GqlArticlePostProcessorWhereUniqueInput>>;
};

export type GqlArticlePostProcessorCreateOrConnectWithoutBucketsInput = {
  where: GqlArticlePostProcessorWhereUniqueInput;
  create: GqlArticlePostProcessorCreateWithoutBucketsInput;
};

export type GqlArticlePostProcessorCreateWithoutBucketsInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  type: Scalars['String'];
  context?: Maybe<Scalars['String']>;
};

export type GqlArticlePostProcessorGroupBy = {
  __typename?: 'ArticlePostProcessorGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  type: FieldWrapper<Scalars['String']>;
  context?: Maybe<FieldWrapper<Scalars['String']>>;
  _count?: Maybe<FieldWrapper<GqlArticlePostProcessorCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlArticlePostProcessorMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlArticlePostProcessorMaxAggregate>>;
};

export type GqlArticlePostProcessorListRelationFilter = {
  every?: Maybe<GqlArticlePostProcessorWhereInput>;
  some?: Maybe<GqlArticlePostProcessorWhereInput>;
  none?: Maybe<GqlArticlePostProcessorWhereInput>;
};

export type GqlArticlePostProcessorMaxAggregate = {
  __typename?: 'ArticlePostProcessorMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
  context?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticlePostProcessorMinAggregate = {
  __typename?: 'ArticlePostProcessorMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
  context?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticlePostProcessorOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  type?: Maybe<GqlSortOrder>;
  context?: Maybe<GqlSortOrder>;
};

export enum GqlArticlePostProcessorScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  Type = 'type',
  Context = 'context'
}

export type GqlArticlePostProcessorScalarWhereInput = {
  AND?: Maybe<Array<GqlArticlePostProcessorScalarWhereInput>>;
  OR?: Maybe<Array<GqlArticlePostProcessorScalarWhereInput>>;
  NOT?: Maybe<Array<GqlArticlePostProcessorScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  type?: Maybe<GqlStringFilter>;
  context?: Maybe<GqlStringNullableFilter>;
};

export type GqlArticlePostProcessorScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlArticlePostProcessorScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlArticlePostProcessorScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlArticlePostProcessorScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  type?: Maybe<GqlStringWithAggregatesFilter>;
  context?: Maybe<GqlStringNullableWithAggregatesFilter>;
};

export type GqlArticlePostProcessorUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  context?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutPostProcessorsInput>;
};

export type GqlArticlePostProcessorUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  context?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
};

export type GqlArticlePostProcessorUpdateManyWithWhereWithoutBucketsInput = {
  where: GqlArticlePostProcessorScalarWhereInput;
  data: GqlArticlePostProcessorUpdateManyMutationInput;
};

export type GqlArticlePostProcessorUpdateManyWithoutBucketsInput = {
  create?: Maybe<Array<GqlArticlePostProcessorCreateWithoutBucketsInput>>;
  connectOrCreate?: Maybe<Array<GqlArticlePostProcessorCreateOrConnectWithoutBucketsInput>>;
  upsert?: Maybe<Array<GqlArticlePostProcessorUpsertWithWhereUniqueWithoutBucketsInput>>;
  connect?: Maybe<Array<GqlArticlePostProcessorWhereUniqueInput>>;
  set?: Maybe<Array<GqlArticlePostProcessorWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlArticlePostProcessorWhereUniqueInput>>;
  delete?: Maybe<Array<GqlArticlePostProcessorWhereUniqueInput>>;
  update?: Maybe<Array<GqlArticlePostProcessorUpdateWithWhereUniqueWithoutBucketsInput>>;
  updateMany?: Maybe<Array<GqlArticlePostProcessorUpdateManyWithWhereWithoutBucketsInput>>;
  deleteMany?: Maybe<Array<GqlArticlePostProcessorScalarWhereInput>>;
};

export type GqlArticlePostProcessorUpdateWithWhereUniqueWithoutBucketsInput = {
  where: GqlArticlePostProcessorWhereUniqueInput;
  data: GqlArticlePostProcessorUpdateWithoutBucketsInput;
};

export type GqlArticlePostProcessorUpdateWithoutBucketsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  context?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
};

export type GqlArticlePostProcessorUpsertWithWhereUniqueWithoutBucketsInput = {
  where: GqlArticlePostProcessorWhereUniqueInput;
  update: GqlArticlePostProcessorUpdateWithoutBucketsInput;
  create: GqlArticlePostProcessorCreateWithoutBucketsInput;
};

export type GqlArticlePostProcessorWhereInput = {
  AND?: Maybe<Array<GqlArticlePostProcessorWhereInput>>;
  OR?: Maybe<Array<GqlArticlePostProcessorWhereInput>>;
  NOT?: Maybe<Array<GqlArticlePostProcessorWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  type?: Maybe<GqlStringFilter>;
  context?: Maybe<GqlStringNullableFilter>;
  buckets?: Maybe<GqlBucketListRelationFilter>;
};

export type GqlArticlePostProcessorWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['String']>;
};

export type GqlArticleRef = {
  __typename?: 'ArticleRef';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  date_released: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  favored: FieldWrapper<Scalars['Boolean']>;
  has_seen: FieldWrapper<Scalars['Boolean']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  data?: Maybe<FieldWrapper<Scalars['JSON']>>;
  articleId: FieldWrapper<Scalars['String']>;
  type: FieldWrapper<Scalars['String']>;
  owner: FieldWrapper<GqlUser>;
  article: FieldWrapper<GqlArticle>;
  relatives_out: Array<FieldWrapper<GqlReferencedArticleRef>>;
  relatives_in: Array<FieldWrapper<GqlReferencedArticleRef>>;
  stream: Array<FieldWrapper<GqlStream>>;
};


export type GqlArticleRefRelatives_OutArgs = {
  where?: Maybe<GqlReferencedArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlReferencedArticleRefOrderByInput>>;
  cursor?: Maybe<GqlReferencedArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlReferencedArticleRefScalarFieldEnum>>;
};


export type GqlArticleRefRelatives_InArgs = {
  where?: Maybe<GqlReferencedArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlReferencedArticleRefOrderByInput>>;
  cursor?: Maybe<GqlReferencedArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlReferencedArticleRefScalarFieldEnum>>;
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
  date_released: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  favored: FieldWrapper<Scalars['Int']>;
  has_seen: FieldWrapper<Scalars['Int']>;
  tags: FieldWrapper<Scalars['Int']>;
  data: FieldWrapper<Scalars['Int']>;
  articleId: FieldWrapper<Scalars['Int']>;
  type: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlArticleRefCreateInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  date_released?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  has_seen?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<Scalars['String']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutArticleRefsInput>;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  relatives_out?: Maybe<GqlReferencedArticleRefCreateNestedManyWithoutSourceInput>;
  relatives_in?: Maybe<GqlReferencedArticleRefCreateNestedManyWithoutTargetInput>;
  stream?: Maybe<GqlStreamCreateNestedManyWithoutArticleRefsInput>;
};

export type GqlArticleRefCreateManyArticleInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  date_released?: Maybe<Scalars['DateTime']>;
  ownerId?: Maybe<Scalars['String']>;
  favored?: Maybe<Scalars['Boolean']>;
  has_seen?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<Scalars['String']>;
};

export type GqlArticleRefCreateManyArticleInputEnvelope = {
  data: Array<GqlArticleRefCreateManyArticleInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlArticleRefCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  date_released?: Maybe<Scalars['DateTime']>;
  ownerId?: Maybe<Scalars['String']>;
  favored?: Maybe<Scalars['Boolean']>;
  has_seen?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  articleId: Scalars['String'];
  type?: Maybe<Scalars['String']>;
};

export type GqlArticleRefCreateManyOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  date_released?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  has_seen?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  articleId: Scalars['String'];
  type?: Maybe<Scalars['String']>;
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

export type GqlArticleRefCreateNestedOneWithoutRelatives_InInput = {
  create?: Maybe<GqlArticleRefCreateWithoutRelatives_InInput>;
  connectOrCreate?: Maybe<GqlArticleRefCreateOrConnectWithoutRelatives_InInput>;
  connect?: Maybe<GqlArticleRefWhereUniqueInput>;
};

export type GqlArticleRefCreateNestedOneWithoutRelatives_OutInput = {
  create?: Maybe<GqlArticleRefCreateWithoutRelatives_OutInput>;
  connectOrCreate?: Maybe<GqlArticleRefCreateOrConnectWithoutRelatives_OutInput>;
  connect?: Maybe<GqlArticleRefWhereUniqueInput>;
};

export type GqlArticleRefCreateOrConnectWithoutArticleInput = {
  where: GqlArticleRefWhereUniqueInput;
  create: GqlArticleRefCreateWithoutArticleInput;
};

export type GqlArticleRefCreateOrConnectWithoutOwnerInput = {
  where: GqlArticleRefWhereUniqueInput;
  create: GqlArticleRefCreateWithoutOwnerInput;
};

export type GqlArticleRefCreateOrConnectWithoutRelatives_InInput = {
  where: GqlArticleRefWhereUniqueInput;
  create: GqlArticleRefCreateWithoutRelatives_InInput;
};

export type GqlArticleRefCreateOrConnectWithoutRelatives_OutInput = {
  where: GqlArticleRefWhereUniqueInput;
  create: GqlArticleRefCreateWithoutRelatives_OutInput;
};

export type GqlArticleRefCreateOrConnectWithoutStreamInput = {
  where: GqlArticleRefWhereUniqueInput;
  create: GqlArticleRefCreateWithoutStreamInput;
};

export type GqlArticleRefCreateWithoutArticleInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  date_released?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  has_seen?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<Scalars['String']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutArticleRefsInput>;
  relatives_out?: Maybe<GqlReferencedArticleRefCreateNestedManyWithoutSourceInput>;
  relatives_in?: Maybe<GqlReferencedArticleRefCreateNestedManyWithoutTargetInput>;
  stream?: Maybe<GqlStreamCreateNestedManyWithoutArticleRefsInput>;
};

export type GqlArticleRefCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  date_released?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  has_seen?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<Scalars['String']>;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  relatives_out?: Maybe<GqlReferencedArticleRefCreateNestedManyWithoutSourceInput>;
  relatives_in?: Maybe<GqlReferencedArticleRefCreateNestedManyWithoutTargetInput>;
  stream?: Maybe<GqlStreamCreateNestedManyWithoutArticleRefsInput>;
};

export type GqlArticleRefCreateWithoutRelatives_InInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  date_released?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  has_seen?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<Scalars['String']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutArticleRefsInput>;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  relatives_out?: Maybe<GqlReferencedArticleRefCreateNestedManyWithoutSourceInput>;
  stream?: Maybe<GqlStreamCreateNestedManyWithoutArticleRefsInput>;
};

export type GqlArticleRefCreateWithoutRelatives_OutInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  date_released?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  has_seen?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<Scalars['String']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutArticleRefsInput>;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  relatives_in?: Maybe<GqlReferencedArticleRefCreateNestedManyWithoutTargetInput>;
  stream?: Maybe<GqlStreamCreateNestedManyWithoutArticleRefsInput>;
};

export type GqlArticleRefCreateWithoutStreamInput = {
  id?: Maybe<Scalars['String']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  date_released?: Maybe<Scalars['DateTime']>;
  favored?: Maybe<Scalars['Boolean']>;
  has_seen?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<Scalars['String']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutArticleRefsInput>;
  article: GqlArticleCreateNestedOneWithoutArticleRefInput;
  relatives_out?: Maybe<GqlReferencedArticleRefCreateNestedManyWithoutSourceInput>;
  relatives_in?: Maybe<GqlReferencedArticleRefCreateNestedManyWithoutTargetInput>;
};

export type GqlArticleRefGroupBy = {
  __typename?: 'ArticleRefGroupBy';
  id: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  date_released: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  favored: FieldWrapper<Scalars['Boolean']>;
  has_seen: FieldWrapper<Scalars['Boolean']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  data?: Maybe<FieldWrapper<Scalars['JSON']>>;
  articleId: FieldWrapper<Scalars['String']>;
  type: FieldWrapper<Scalars['String']>;
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
  date_released?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  favored?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_seen?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  articleId?: Maybe<FieldWrapper<Scalars['String']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleRefMinAggregate = {
  __typename?: 'ArticleRefMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  date_released?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  favored?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  has_seen?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  articleId?: Maybe<FieldWrapper<Scalars['String']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlArticleRefOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  date_released?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  favored?: Maybe<GqlSortOrder>;
  has_seen?: Maybe<GqlSortOrder>;
  tags?: Maybe<GqlSortOrder>;
  data?: Maybe<GqlSortOrder>;
  articleId?: Maybe<GqlSortOrder>;
  type?: Maybe<GqlSortOrder>;
};

export type GqlArticleRefRelationFilter = {
  is?: Maybe<GqlArticleRefWhereInput>;
  isNot?: Maybe<GqlArticleRefWhereInput>;
};

export enum GqlArticleRefScalarFieldEnum {
  Id = 'id',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  DateReleased = 'date_released',
  OwnerId = 'ownerId',
  Favored = 'favored',
  HasSeen = 'has_seen',
  Tags = 'tags',
  Data = 'data',
  ArticleId = 'articleId',
  Type = 'type'
}

export type GqlArticleRefScalarWhereInput = {
  AND?: Maybe<Array<GqlArticleRefScalarWhereInput>>;
  OR?: Maybe<Array<GqlArticleRefScalarWhereInput>>;
  NOT?: Maybe<Array<GqlArticleRefScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  date_released?: Maybe<GqlDateTimeFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  favored?: Maybe<GqlBoolFilter>;
  has_seen?: Maybe<GqlBoolFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  data?: Maybe<GqlJsonNullableFilter>;
  articleId?: Maybe<GqlStringFilter>;
  type?: Maybe<GqlStringFilter>;
};

export type GqlArticleRefScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlArticleRefScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlArticleRefScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlArticleRefScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  date_released?: Maybe<GqlDateTimeWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
  favored?: Maybe<GqlBoolWithAggregatesFilter>;
  has_seen?: Maybe<GqlBoolWithAggregatesFilter>;
  tags?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  data?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  articleId?: Maybe<GqlStringWithAggregatesFilter>;
  type?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlArticleRefUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_released?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  has_seen?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  relatives_out?: Maybe<GqlReferencedArticleRefUpdateManyWithoutSourceInput>;
  relatives_in?: Maybe<GqlReferencedArticleRefUpdateManyWithoutTargetInput>;
  stream?: Maybe<GqlStreamUpdateManyWithoutArticleRefsInput>;
};

export type GqlArticleRefUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_released?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  has_seen?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
};

export type GqlArticleRefUpdateManyWithWhereWithoutArticleInput = {
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

export type GqlArticleRefUpdateOneRequiredWithoutRelatives_InInput = {
  create?: Maybe<GqlArticleRefCreateWithoutRelatives_InInput>;
  connectOrCreate?: Maybe<GqlArticleRefCreateOrConnectWithoutRelatives_InInput>;
  upsert?: Maybe<GqlArticleRefUpsertWithoutRelatives_InInput>;
  connect?: Maybe<GqlArticleRefWhereUniqueInput>;
  update?: Maybe<GqlArticleRefUpdateWithoutRelatives_InInput>;
};

export type GqlArticleRefUpdateOneRequiredWithoutRelatives_OutInput = {
  create?: Maybe<GqlArticleRefCreateWithoutRelatives_OutInput>;
  connectOrCreate?: Maybe<GqlArticleRefCreateOrConnectWithoutRelatives_OutInput>;
  upsert?: Maybe<GqlArticleRefUpsertWithoutRelatives_OutInput>;
  connect?: Maybe<GqlArticleRefWhereUniqueInput>;
  update?: Maybe<GqlArticleRefUpdateWithoutRelatives_OutInput>;
};

export type GqlArticleRefUpdateWithWhereUniqueWithoutArticleInput = {
  where: GqlArticleRefWhereUniqueInput;
  data: GqlArticleRefUpdateWithoutArticleInput;
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
  date_released?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  has_seen?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  relatives_out?: Maybe<GqlReferencedArticleRefUpdateManyWithoutSourceInput>;
  relatives_in?: Maybe<GqlReferencedArticleRefUpdateManyWithoutTargetInput>;
  stream?: Maybe<GqlStreamUpdateManyWithoutArticleRefsInput>;
};

export type GqlArticleRefUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_released?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  has_seen?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  relatives_out?: Maybe<GqlReferencedArticleRefUpdateManyWithoutSourceInput>;
  relatives_in?: Maybe<GqlReferencedArticleRefUpdateManyWithoutTargetInput>;
  stream?: Maybe<GqlStreamUpdateManyWithoutArticleRefsInput>;
};

export type GqlArticleRefUpdateWithoutRelatives_InInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_released?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  has_seen?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  relatives_out?: Maybe<GqlReferencedArticleRefUpdateManyWithoutSourceInput>;
  stream?: Maybe<GqlStreamUpdateManyWithoutArticleRefsInput>;
};

export type GqlArticleRefUpdateWithoutRelatives_OutInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_released?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  has_seen?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  relatives_in?: Maybe<GqlReferencedArticleRefUpdateManyWithoutTargetInput>;
  stream?: Maybe<GqlStreamUpdateManyWithoutArticleRefsInput>;
};

export type GqlArticleRefUpdateWithoutStreamInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_released?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  favored?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  has_seen?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  data?: Maybe<Scalars['JSON']>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutArticleRefsInput>;
  article?: Maybe<GqlArticleUpdateOneRequiredWithoutArticleRefInput>;
  relatives_out?: Maybe<GqlReferencedArticleRefUpdateManyWithoutSourceInput>;
  relatives_in?: Maybe<GqlReferencedArticleRefUpdateManyWithoutTargetInput>;
};

export type GqlArticleRefUpsertWithWhereUniqueWithoutArticleInput = {
  where: GqlArticleRefWhereUniqueInput;
  update: GqlArticleRefUpdateWithoutArticleInput;
  create: GqlArticleRefCreateWithoutArticleInput;
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

export type GqlArticleRefUpsertWithoutRelatives_InInput = {
  update: GqlArticleRefUpdateWithoutRelatives_InInput;
  create: GqlArticleRefCreateWithoutRelatives_InInput;
};

export type GqlArticleRefUpsertWithoutRelatives_OutInput = {
  update: GqlArticleRefUpdateWithoutRelatives_OutInput;
  create: GqlArticleRefCreateWithoutRelatives_OutInput;
};

export type GqlArticleRefWhereInput = {
  AND?: Maybe<Array<GqlArticleRefWhereInput>>;
  OR?: Maybe<Array<GqlArticleRefWhereInput>>;
  NOT?: Maybe<Array<GqlArticleRefWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  date_released?: Maybe<GqlDateTimeFilter>;
  owner?: Maybe<GqlUserRelationFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  favored?: Maybe<GqlBoolFilter>;
  has_seen?: Maybe<GqlBoolFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  data?: Maybe<GqlJsonNullableFilter>;
  article?: Maybe<GqlArticleRelationFilter>;
  articleId?: Maybe<GqlStringFilter>;
  type?: Maybe<GqlStringFilter>;
  relatives_out?: Maybe<GqlReferencedArticleRefListRelationFilter>;
  relatives_in?: Maybe<GqlReferencedArticleRefListRelationFilter>;
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
  UpdatedAt = 'updatedAt',
  Released = 'released',
  DatePublished = 'date_published',
  DateModified = 'date_modified',
  CommentFeedUrl = 'comment_feed_url',
  SourceUrl = 'source_url',
  Url = 'url',
  Author = 'author',
  Title = 'title',
  Tags = 'tags',
  FulltextData = 'fulltext_data',
  ContentRawMime = 'content_raw_mime',
  ContentRaw = 'content_raw',
  ContentText = 'content_text',
  HasHarvest = 'has_harvest',
  HasReadability = 'has_readability',
  HasVideo = 'has_video',
  HasAudio = 'has_audio',
  LengthVideo = 'length_video',
  LengthAudio = 'length_audio',
  WordCountText = 'word_count_text',
  Score = 'score',
  LastScoredAt = 'lastScoredAt',
  Enclosure = 'enclosure',
  DataJsonMap = 'data_json_map',
  Readability = 'readability'
}

export type GqlArticleScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlArticleScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlArticleScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlArticleScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  released?: Maybe<GqlBoolWithAggregatesFilter>;
  date_published?: Maybe<GqlDateTimeWithAggregatesFilter>;
  date_modified?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
  comment_feed_url?: Maybe<GqlStringNullableWithAggregatesFilter>;
  source_url?: Maybe<GqlStringNullableWithAggregatesFilter>;
  url?: Maybe<GqlStringNullableWithAggregatesFilter>;
  author?: Maybe<GqlStringNullableWithAggregatesFilter>;
  title?: Maybe<GqlStringWithAggregatesFilter>;
  tags?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  fulltext_data?: Maybe<GqlStringNullableWithAggregatesFilter>;
  content_raw_mime?: Maybe<GqlStringNullableWithAggregatesFilter>;
  content_raw?: Maybe<GqlStringWithAggregatesFilter>;
  content_text?: Maybe<GqlStringNullableWithAggregatesFilter>;
  has_harvest?: Maybe<GqlBoolNullableWithAggregatesFilter>;
  has_readability?: Maybe<GqlBoolNullableWithAggregatesFilter>;
  has_video?: Maybe<GqlBoolWithAggregatesFilter>;
  has_audio?: Maybe<GqlBoolWithAggregatesFilter>;
  length_video?: Maybe<GqlIntNullableWithAggregatesFilter>;
  length_audio?: Maybe<GqlIntNullableWithAggregatesFilter>;
  word_count_text?: Maybe<GqlIntNullableWithAggregatesFilter>;
  score?: Maybe<GqlFloatNullableWithAggregatesFilter>;
  lastScoredAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  enclosure?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  data_json_map?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  readability?: Maybe<GqlJsonNullableWithAggregatesFilter>;
};

export type GqlArticleSumAggregate = {
  __typename?: 'ArticleSumAggregate';
  length_video?: Maybe<FieldWrapper<Scalars['Int']>>;
  length_audio?: Maybe<FieldWrapper<Scalars['Int']>>;
  word_count_text?: Maybe<FieldWrapper<Scalars['Int']>>;
  score?: Maybe<FieldWrapper<Scalars['Float']>>;
};

export type GqlArticleUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  released?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  date_published?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_modified?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  comment_feed_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  source_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  fulltext_data?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  content_raw_mime?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  content_raw?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content_text?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  has_harvest?: Maybe<GqlNullableBoolFieldUpdateOperationsInput>;
  has_readability?: Maybe<GqlNullableBoolFieldUpdateOperationsInput>;
  has_video?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  has_audio?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  length_video?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  length_audio?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  word_count_text?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  score?: Maybe<GqlNullableFloatFieldUpdateOperationsInput>;
  lastScoredAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  enclosure?: Maybe<Scalars['JSON']>;
  data_json_map?: Maybe<Scalars['JSON']>;
  readability?: Maybe<Scalars['JSON']>;
  articleRef?: Maybe<GqlArticleRefUpdateManyWithoutArticleInput>;
};

export type GqlArticleUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  released?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  date_published?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_modified?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  comment_feed_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  source_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  fulltext_data?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  content_raw_mime?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  content_raw?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content_text?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  has_harvest?: Maybe<GqlNullableBoolFieldUpdateOperationsInput>;
  has_readability?: Maybe<GqlNullableBoolFieldUpdateOperationsInput>;
  has_video?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  has_audio?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  length_video?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  length_audio?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  word_count_text?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  score?: Maybe<GqlNullableFloatFieldUpdateOperationsInput>;
  lastScoredAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  enclosure?: Maybe<Scalars['JSON']>;
  data_json_map?: Maybe<Scalars['JSON']>;
  readability?: Maybe<Scalars['JSON']>;
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
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  released?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  date_published?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  date_modified?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  comment_feed_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  source_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  fulltext_data?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  content_raw_mime?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  content_raw?: Maybe<GqlStringFieldUpdateOperationsInput>;
  content_text?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  has_harvest?: Maybe<GqlNullableBoolFieldUpdateOperationsInput>;
  has_readability?: Maybe<GqlNullableBoolFieldUpdateOperationsInput>;
  has_video?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  has_audio?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  length_video?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  length_audio?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  word_count_text?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  score?: Maybe<GqlNullableFloatFieldUpdateOperationsInput>;
  lastScoredAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  enclosure?: Maybe<Scalars['JSON']>;
  data_json_map?: Maybe<Scalars['JSON']>;
  readability?: Maybe<Scalars['JSON']>;
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
  updatedAt?: Maybe<GqlDateTimeFilter>;
  released?: Maybe<GqlBoolFilter>;
  date_published?: Maybe<GqlDateTimeFilter>;
  date_modified?: Maybe<GqlDateTimeNullableFilter>;
  comment_feed_url?: Maybe<GqlStringNullableFilter>;
  source_url?: Maybe<GqlStringNullableFilter>;
  url?: Maybe<GqlStringNullableFilter>;
  author?: Maybe<GqlStringNullableFilter>;
  title?: Maybe<GqlStringFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  fulltext_data?: Maybe<GqlStringNullableFilter>;
  content_raw_mime?: Maybe<GqlStringNullableFilter>;
  content_raw?: Maybe<GqlStringFilter>;
  content_text?: Maybe<GqlStringNullableFilter>;
  has_harvest?: Maybe<GqlBoolNullableFilter>;
  has_readability?: Maybe<GqlBoolNullableFilter>;
  has_video?: Maybe<GqlBoolFilter>;
  has_audio?: Maybe<GqlBoolFilter>;
  length_video?: Maybe<GqlIntNullableFilter>;
  length_audio?: Maybe<GqlIntNullableFilter>;
  word_count_text?: Maybe<GqlIntNullableFilter>;
  score?: Maybe<GqlFloatNullableFilter>;
  lastScoredAt?: Maybe<GqlDateTimeFilter>;
  enclosure?: Maybe<GqlJsonNullableFilter>;
  articleRef?: Maybe<GqlArticleRefListRelationFilter>;
  data_json_map?: Maybe<GqlJsonNullableFilter>;
  readability?: Maybe<GqlJsonNullableFilter>;
};

export type GqlArticleWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
  url?: Maybe<Scalars['String']>;
};

export type GqlBoolFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['Boolean']>;
};

export type GqlBoolFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<GqlNestedBoolFilter>;
};

export type GqlBoolNullableFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<GqlNestedBoolNullableFilter>;
};

export type GqlBoolNullableWithAggregatesFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<GqlNestedBoolNullableWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntNullableFilter>;
  _min?: Maybe<GqlNestedBoolNullableFilter>;
  _max?: Maybe<GqlNestedBoolNullableFilter>;
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
  title: FieldWrapper<Scalars['String']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed: FieldWrapper<Scalars['Boolean']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  in_focus: FieldWrapper<Scalars['Boolean']>;
  ownerId: FieldWrapper<Scalars['String']>;
  lastPostProcessedAt: FieldWrapper<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  streamId: FieldWrapper<Scalars['String']>;
  owner: FieldWrapper<GqlUser>;
  subscriptions: Array<FieldWrapper<GqlSubscription>>;
  postProcessors: Array<FieldWrapper<GqlArticlePostProcessor>>;
  exporters: Array<FieldWrapper<GqlArticleExporter>>;
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


export type GqlBucketPostProcessorsArgs = {
  where?: Maybe<GqlArticlePostProcessorWhereInput>;
  orderBy?: Maybe<Array<GqlArticlePostProcessorOrderByInput>>;
  cursor?: Maybe<GqlArticlePostProcessorWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticlePostProcessorScalarFieldEnum>>;
};


export type GqlBucketExportersArgs = {
  where?: Maybe<GqlArticleExporterWhereInput>;
  orderBy?: Maybe<Array<GqlArticleExporterOrderByInput>>;
  cursor?: Maybe<GqlArticleExporterWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleExporterScalarFieldEnum>>;
};

export type GqlBucketCountAggregate = {
  __typename?: 'BucketCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  description: FieldWrapper<Scalars['Int']>;
  listed: FieldWrapper<Scalars['Int']>;
  tags: FieldWrapper<Scalars['Int']>;
  in_focus: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  lastPostProcessedAt: FieldWrapper<Scalars['Int']>;
  lastUpdatedAt: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  streamId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlBucketCreateInput = {
  id?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<Scalars['Boolean']>;
  lastPostProcessedAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutBucketInput>;
  postProcessors?: Maybe<GqlArticlePostProcessorCreateNestedManyWithoutBucketsInput>;
  exporters?: Maybe<GqlArticleExporterCreateNestedManyWithoutBucketInput>;
  stream: GqlStreamCreateNestedOneWithoutBucketsInput;
};

export type GqlBucketCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<Scalars['Boolean']>;
  ownerId: Scalars['String'];
  lastPostProcessedAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  streamId: Scalars['String'];
};

export type GqlBucketCreateManyOwnerInput = {
  id?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<Scalars['Boolean']>;
  lastPostProcessedAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  streamId: Scalars['String'];
};

export type GqlBucketCreateManyOwnerInputEnvelope = {
  data: Array<GqlBucketCreateManyOwnerInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlBucketCreateManyStreamInput = {
  id?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<Scalars['Boolean']>;
  ownerId: Scalars['String'];
  lastPostProcessedAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
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

export type GqlBucketCreateNestedManyWithoutPostProcessorsInput = {
  create?: Maybe<Array<GqlBucketCreateWithoutPostProcessorsInput>>;
  connectOrCreate?: Maybe<Array<GqlBucketCreateOrConnectWithoutPostProcessorsInput>>;
  connect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
};

export type GqlBucketCreateNestedManyWithoutStreamInput = {
  create?: Maybe<Array<GqlBucketCreateWithoutStreamInput>>;
  connectOrCreate?: Maybe<Array<GqlBucketCreateOrConnectWithoutStreamInput>>;
  createMany?: Maybe<GqlBucketCreateManyStreamInputEnvelope>;
  connect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
};

export type GqlBucketCreateNestedOneWithoutExportersInput = {
  create?: Maybe<GqlBucketCreateWithoutExportersInput>;
  connectOrCreate?: Maybe<GqlBucketCreateOrConnectWithoutExportersInput>;
  connect?: Maybe<GqlBucketWhereUniqueInput>;
};

export type GqlBucketCreateNestedOneWithoutSubscriptionsInput = {
  create?: Maybe<GqlBucketCreateWithoutSubscriptionsInput>;
  connectOrCreate?: Maybe<GqlBucketCreateOrConnectWithoutSubscriptionsInput>;
  connect?: Maybe<GqlBucketWhereUniqueInput>;
};

export type GqlBucketCreateOrConnectWithoutExportersInput = {
  where: GqlBucketWhereUniqueInput;
  create: GqlBucketCreateWithoutExportersInput;
};

export type GqlBucketCreateOrConnectWithoutOwnerInput = {
  where: GqlBucketWhereUniqueInput;
  create: GqlBucketCreateWithoutOwnerInput;
};

export type GqlBucketCreateOrConnectWithoutPostProcessorsInput = {
  where: GqlBucketWhereUniqueInput;
  create: GqlBucketCreateWithoutPostProcessorsInput;
};

export type GqlBucketCreateOrConnectWithoutStreamInput = {
  where: GqlBucketWhereUniqueInput;
  create: GqlBucketCreateWithoutStreamInput;
};

export type GqlBucketCreateOrConnectWithoutSubscriptionsInput = {
  where: GqlBucketWhereUniqueInput;
  create: GqlBucketCreateWithoutSubscriptionsInput;
};

export type GqlBucketCreateWithoutExportersInput = {
  id?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<Scalars['Boolean']>;
  lastPostProcessedAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutBucketInput>;
  postProcessors?: Maybe<GqlArticlePostProcessorCreateNestedManyWithoutBucketsInput>;
  stream: GqlStreamCreateNestedOneWithoutBucketsInput;
};

export type GqlBucketCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<Scalars['Boolean']>;
  lastPostProcessedAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutBucketInput>;
  postProcessors?: Maybe<GqlArticlePostProcessorCreateNestedManyWithoutBucketsInput>;
  exporters?: Maybe<GqlArticleExporterCreateNestedManyWithoutBucketInput>;
  stream: GqlStreamCreateNestedOneWithoutBucketsInput;
};

export type GqlBucketCreateWithoutPostProcessorsInput = {
  id?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<Scalars['Boolean']>;
  lastPostProcessedAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutBucketInput>;
  exporters?: Maybe<GqlArticleExporterCreateNestedManyWithoutBucketInput>;
  stream: GqlStreamCreateNestedOneWithoutBucketsInput;
};

export type GqlBucketCreateWithoutStreamInput = {
  id?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<Scalars['Boolean']>;
  lastPostProcessedAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutBucketInput>;
  postProcessors?: Maybe<GqlArticlePostProcessorCreateNestedManyWithoutBucketsInput>;
  exporters?: Maybe<GqlArticleExporterCreateNestedManyWithoutBucketInput>;
};

export type GqlBucketCreateWithoutSubscriptionsInput = {
  id?: Maybe<Scalars['String']>;
  title: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  listed?: Maybe<Scalars['Boolean']>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<Scalars['Boolean']>;
  lastPostProcessedAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  owner: GqlUserCreateNestedOneWithoutBucketsInput;
  postProcessors?: Maybe<GqlArticlePostProcessorCreateNestedManyWithoutBucketsInput>;
  exporters?: Maybe<GqlArticleExporterCreateNestedManyWithoutBucketInput>;
  stream: GqlStreamCreateNestedOneWithoutBucketsInput;
};

export type GqlBucketGroupBy = {
  __typename?: 'BucketGroupBy';
  id: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed: FieldWrapper<Scalars['Boolean']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  in_focus: FieldWrapper<Scalars['Boolean']>;
  ownerId: FieldWrapper<Scalars['String']>;
  lastPostProcessedAt: FieldWrapper<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
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
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  in_focus?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  lastPostProcessedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  streamId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlBucketMinAggregate = {
  __typename?: 'BucketMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  listed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  in_focus?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  lastPostProcessedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  streamId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlBucketOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  description?: Maybe<GqlSortOrder>;
  listed?: Maybe<GqlSortOrder>;
  tags?: Maybe<GqlSortOrder>;
  in_focus?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  lastPostProcessedAt?: Maybe<GqlSortOrder>;
  lastUpdatedAt?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  streamId?: Maybe<GqlSortOrder>;
};

export type GqlBucketRelationFilter = {
  is?: Maybe<GqlBucketWhereInput>;
  isNot?: Maybe<GqlBucketWhereInput>;
};

export enum GqlBucketScalarFieldEnum {
  Id = 'id',
  Title = 'title',
  Description = 'description',
  Listed = 'listed',
  Tags = 'tags',
  InFocus = 'in_focus',
  OwnerId = 'ownerId',
  LastPostProcessedAt = 'lastPostProcessedAt',
  LastUpdatedAt = 'lastUpdatedAt',
  CreatedAt = 'createdAt',
  StreamId = 'streamId'
}

export type GqlBucketScalarWhereInput = {
  AND?: Maybe<Array<GqlBucketScalarWhereInput>>;
  OR?: Maybe<Array<GqlBucketScalarWhereInput>>;
  NOT?: Maybe<Array<GqlBucketScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  title?: Maybe<GqlStringFilter>;
  description?: Maybe<GqlStringNullableFilter>;
  listed?: Maybe<GqlBoolFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  in_focus?: Maybe<GqlBoolFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  lastPostProcessedAt?: Maybe<GqlDateTimeFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  streamId?: Maybe<GqlStringFilter>;
};

export type GqlBucketScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlBucketScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlBucketScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlBucketScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  title?: Maybe<GqlStringWithAggregatesFilter>;
  description?: Maybe<GqlStringNullableWithAggregatesFilter>;
  listed?: Maybe<GqlBoolWithAggregatesFilter>;
  tags?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  in_focus?: Maybe<GqlBoolWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
  lastPostProcessedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  streamId?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlBucketUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  lastPostProcessedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutBucketInput>;
  postProcessors?: Maybe<GqlArticlePostProcessorUpdateManyWithoutBucketsInput>;
  exporters?: Maybe<GqlArticleExporterUpdateManyWithoutBucketInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutBucketsInput>;
};

export type GqlBucketUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  lastPostProcessedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
};

export type GqlBucketUpdateManyWithWhereWithoutOwnerInput = {
  where: GqlBucketScalarWhereInput;
  data: GqlBucketUpdateManyMutationInput;
};

export type GqlBucketUpdateManyWithWhereWithoutPostProcessorsInput = {
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

export type GqlBucketUpdateManyWithoutPostProcessorsInput = {
  create?: Maybe<Array<GqlBucketCreateWithoutPostProcessorsInput>>;
  connectOrCreate?: Maybe<Array<GqlBucketCreateOrConnectWithoutPostProcessorsInput>>;
  upsert?: Maybe<Array<GqlBucketUpsertWithWhereUniqueWithoutPostProcessorsInput>>;
  connect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  set?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  delete?: Maybe<Array<GqlBucketWhereUniqueInput>>;
  update?: Maybe<Array<GqlBucketUpdateWithWhereUniqueWithoutPostProcessorsInput>>;
  updateMany?: Maybe<Array<GqlBucketUpdateManyWithWhereWithoutPostProcessorsInput>>;
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

export type GqlBucketUpdateOneRequiredWithoutExportersInput = {
  create?: Maybe<GqlBucketCreateWithoutExportersInput>;
  connectOrCreate?: Maybe<GqlBucketCreateOrConnectWithoutExportersInput>;
  upsert?: Maybe<GqlBucketUpsertWithoutExportersInput>;
  connect?: Maybe<GqlBucketWhereUniqueInput>;
  update?: Maybe<GqlBucketUpdateWithoutExportersInput>;
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

export type GqlBucketUpdateWithWhereUniqueWithoutPostProcessorsInput = {
  where: GqlBucketWhereUniqueInput;
  data: GqlBucketUpdateWithoutPostProcessorsInput;
};

export type GqlBucketUpdateWithWhereUniqueWithoutStreamInput = {
  where: GqlBucketWhereUniqueInput;
  data: GqlBucketUpdateWithoutStreamInput;
};

export type GqlBucketUpdateWithoutExportersInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  lastPostProcessedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutBucketInput>;
  postProcessors?: Maybe<GqlArticlePostProcessorUpdateManyWithoutBucketsInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutBucketsInput>;
};

export type GqlBucketUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  lastPostProcessedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutBucketInput>;
  postProcessors?: Maybe<GqlArticlePostProcessorUpdateManyWithoutBucketsInput>;
  exporters?: Maybe<GqlArticleExporterUpdateManyWithoutBucketInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutBucketsInput>;
};

export type GqlBucketUpdateWithoutPostProcessorsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  lastPostProcessedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutBucketInput>;
  exporters?: Maybe<GqlArticleExporterUpdateManyWithoutBucketInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutBucketsInput>;
};

export type GqlBucketUpdateWithoutStreamInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  lastPostProcessedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutBucketInput>;
  postProcessors?: Maybe<GqlArticlePostProcessorUpdateManyWithoutBucketsInput>;
  exporters?: Maybe<GqlArticleExporterUpdateManyWithoutBucketInput>;
};

export type GqlBucketUpdateWithoutSubscriptionsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  in_focus?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  lastPostProcessedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutBucketsInput>;
  postProcessors?: Maybe<GqlArticlePostProcessorUpdateManyWithoutBucketsInput>;
  exporters?: Maybe<GqlArticleExporterUpdateManyWithoutBucketInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutBucketsInput>;
};

export type GqlBucketUpsertWithWhereUniqueWithoutOwnerInput = {
  where: GqlBucketWhereUniqueInput;
  update: GqlBucketUpdateWithoutOwnerInput;
  create: GqlBucketCreateWithoutOwnerInput;
};

export type GqlBucketUpsertWithWhereUniqueWithoutPostProcessorsInput = {
  where: GqlBucketWhereUniqueInput;
  update: GqlBucketUpdateWithoutPostProcessorsInput;
  create: GqlBucketCreateWithoutPostProcessorsInput;
};

export type GqlBucketUpsertWithWhereUniqueWithoutStreamInput = {
  where: GqlBucketWhereUniqueInput;
  update: GqlBucketUpdateWithoutStreamInput;
  create: GqlBucketCreateWithoutStreamInput;
};

export type GqlBucketUpsertWithoutExportersInput = {
  update: GqlBucketUpdateWithoutExportersInput;
  create: GqlBucketCreateWithoutExportersInput;
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
  title?: Maybe<GqlStringFilter>;
  description?: Maybe<GqlStringNullableFilter>;
  listed?: Maybe<GqlBoolFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  in_focus?: Maybe<GqlBoolFilter>;
  owner?: Maybe<GqlUserRelationFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  subscriptions?: Maybe<GqlSubscriptionListRelationFilter>;
  lastPostProcessedAt?: Maybe<GqlDateTimeFilter>;
  postProcessors?: Maybe<GqlArticlePostProcessorListRelationFilter>;
  exporters?: Maybe<GqlArticleExporterListRelationFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
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

export type GqlDiscoveredFeeds = {
  __typename?: 'DiscoveredFeeds';
  nativeFeeds?: Maybe<Array<FieldWrapper<GqlNativeFeedRef>>>;
  genericFeedRules?: Maybe<Array<FieldWrapper<GqlGenericFeedRule>>>;
};

export type GqlEventHook = {
  __typename?: 'EventHook';
  id: FieldWrapper<Scalars['String']>;
  ownerId: FieldWrapper<Scalars['String']>;
  event: FieldWrapper<Scalars['String']>;
  type: FieldWrapper<Scalars['String']>;
  script_or_url: FieldWrapper<Scalars['String']>;
  script_source_url?: Maybe<FieldWrapper<Scalars['String']>>;
  owner: FieldWrapper<GqlUser>;
};

export type GqlEventHookCountAggregate = {
  __typename?: 'EventHookCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  event: FieldWrapper<Scalars['Int']>;
  type: FieldWrapper<Scalars['Int']>;
  script_or_url: FieldWrapper<Scalars['Int']>;
  script_source_url: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlEventHookCreateInput = {
  id?: Maybe<Scalars['String']>;
  event: Scalars['String'];
  type: Scalars['String'];
  script_or_url: Scalars['String'];
  script_source_url?: Maybe<Scalars['String']>;
  owner: GqlUserCreateNestedOneWithoutEventHooksInput;
};

export type GqlEventHookCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  ownerId: Scalars['String'];
  event: Scalars['String'];
  type: Scalars['String'];
  script_or_url: Scalars['String'];
  script_source_url?: Maybe<Scalars['String']>;
};

export type GqlEventHookCreateManyOwnerInput = {
  id?: Maybe<Scalars['String']>;
  event: Scalars['String'];
  type: Scalars['String'];
  script_or_url: Scalars['String'];
  script_source_url?: Maybe<Scalars['String']>;
};

export type GqlEventHookCreateManyOwnerInputEnvelope = {
  data: Array<GqlEventHookCreateManyOwnerInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlEventHookCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlEventHookCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlEventHookCreateOrConnectWithoutOwnerInput>>;
  createMany?: Maybe<GqlEventHookCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlEventHookWhereUniqueInput>>;
};

export type GqlEventHookCreateOrConnectWithoutOwnerInput = {
  where: GqlEventHookWhereUniqueInput;
  create: GqlEventHookCreateWithoutOwnerInput;
};

export type GqlEventHookCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  event: Scalars['String'];
  type: Scalars['String'];
  script_or_url: Scalars['String'];
  script_source_url?: Maybe<Scalars['String']>;
};

export type GqlEventHookGroupBy = {
  __typename?: 'EventHookGroupBy';
  id: FieldWrapper<Scalars['String']>;
  ownerId: FieldWrapper<Scalars['String']>;
  event: FieldWrapper<Scalars['String']>;
  type: FieldWrapper<Scalars['String']>;
  script_or_url: FieldWrapper<Scalars['String']>;
  script_source_url?: Maybe<FieldWrapper<Scalars['String']>>;
  _count?: Maybe<FieldWrapper<GqlEventHookCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlEventHookMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlEventHookMaxAggregate>>;
};

export type GqlEventHookListRelationFilter = {
  every?: Maybe<GqlEventHookWhereInput>;
  some?: Maybe<GqlEventHookWhereInput>;
  none?: Maybe<GqlEventHookWhereInput>;
};

export type GqlEventHookMaxAggregate = {
  __typename?: 'EventHookMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  event?: Maybe<FieldWrapper<Scalars['String']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
  script_or_url?: Maybe<FieldWrapper<Scalars['String']>>;
  script_source_url?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlEventHookMinAggregate = {
  __typename?: 'EventHookMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  event?: Maybe<FieldWrapper<Scalars['String']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
  script_or_url?: Maybe<FieldWrapper<Scalars['String']>>;
  script_source_url?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlEventHookOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  event?: Maybe<GqlSortOrder>;
  type?: Maybe<GqlSortOrder>;
  script_or_url?: Maybe<GqlSortOrder>;
  script_source_url?: Maybe<GqlSortOrder>;
};

export enum GqlEventHookScalarFieldEnum {
  Id = 'id',
  OwnerId = 'ownerId',
  Event = 'event',
  Type = 'type',
  ScriptOrUrl = 'script_or_url',
  ScriptSourceUrl = 'script_source_url'
}

export type GqlEventHookScalarWhereInput = {
  AND?: Maybe<Array<GqlEventHookScalarWhereInput>>;
  OR?: Maybe<Array<GqlEventHookScalarWhereInput>>;
  NOT?: Maybe<Array<GqlEventHookScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  event?: Maybe<GqlStringFilter>;
  type?: Maybe<GqlStringFilter>;
  script_or_url?: Maybe<GqlStringFilter>;
  script_source_url?: Maybe<GqlStringNullableFilter>;
};

export type GqlEventHookScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlEventHookScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlEventHookScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlEventHookScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
  event?: Maybe<GqlStringWithAggregatesFilter>;
  type?: Maybe<GqlStringWithAggregatesFilter>;
  script_or_url?: Maybe<GqlStringWithAggregatesFilter>;
  script_source_url?: Maybe<GqlStringNullableWithAggregatesFilter>;
};

export type GqlEventHookUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  event?: Maybe<GqlStringFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  script_or_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  script_source_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutEventHooksInput>;
};

export type GqlEventHookUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  event?: Maybe<GqlStringFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  script_or_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  script_source_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
};

export type GqlEventHookUpdateManyWithWhereWithoutOwnerInput = {
  where: GqlEventHookScalarWhereInput;
  data: GqlEventHookUpdateManyMutationInput;
};

export type GqlEventHookUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlEventHookCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlEventHookCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<GqlEventHookUpsertWithWhereUniqueWithoutOwnerInput>>;
  createMany?: Maybe<GqlEventHookCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlEventHookWhereUniqueInput>>;
  set?: Maybe<Array<GqlEventHookWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlEventHookWhereUniqueInput>>;
  delete?: Maybe<Array<GqlEventHookWhereUniqueInput>>;
  update?: Maybe<Array<GqlEventHookUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<GqlEventHookUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<GqlEventHookScalarWhereInput>>;
};

export type GqlEventHookUpdateWithWhereUniqueWithoutOwnerInput = {
  where: GqlEventHookWhereUniqueInput;
  data: GqlEventHookUpdateWithoutOwnerInput;
};

export type GqlEventHookUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  event?: Maybe<GqlStringFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  script_or_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  script_source_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
};

export type GqlEventHookUpsertWithWhereUniqueWithoutOwnerInput = {
  where: GqlEventHookWhereUniqueInput;
  update: GqlEventHookUpdateWithoutOwnerInput;
  create: GqlEventHookCreateWithoutOwnerInput;
};

export type GqlEventHookWhereInput = {
  AND?: Maybe<Array<GqlEventHookWhereInput>>;
  OR?: Maybe<Array<GqlEventHookWhereInput>>;
  NOT?: Maybe<Array<GqlEventHookWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  owner?: Maybe<GqlUserRelationFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  event?: Maybe<GqlStringFilter>;
  type?: Maybe<GqlStringFilter>;
  script_or_url?: Maybe<GqlStringFilter>;
  script_source_url?: Maybe<GqlStringNullableFilter>;
};

export type GqlEventHookWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
  script_source_url?: Maybe<Scalars['String']>;
};

export type GqlFeed = {
  __typename?: 'Feed';
  id: FieldWrapper<Scalars['String']>;
  feed_url: FieldWrapper<Scalars['String']>;
  home_page_url?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  lang?: Maybe<FieldWrapper<Scalars['String']>>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  is_private: FieldWrapper<Scalars['Boolean']>;
  ownerId: FieldWrapper<Scalars['String']>;
  expired: FieldWrapper<Scalars['Boolean']>;
  broken: FieldWrapper<Scalars['Boolean']>;
  inactive: FieldWrapper<Scalars['Boolean']>;
  managed: FieldWrapper<Scalars['Boolean']>;
  filter?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  status: FieldWrapper<Scalars['String']>;
  retention_size?: Maybe<FieldWrapper<Scalars['Int']>>;
  harvest_site: FieldWrapper<Scalars['Boolean']>;
  allowHarvestFailure: FieldWrapper<Scalars['Boolean']>;
  harvest_prerender: FieldWrapper<Scalars['Boolean']>;
  streamId: FieldWrapper<Scalars['String']>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  owner: FieldWrapper<GqlUser>;
  managed_by_plugin?: Maybe<FieldWrapper<GqlPlugin>>;
  stream: FieldWrapper<GqlStream>;
  events: Array<FieldWrapper<GqlFeedEvent>>;
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
  retention_size?: Maybe<FieldWrapper<Scalars['Float']>>;
};

export type GqlFeedCountAggregate = {
  __typename?: 'FeedCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  feed_url: FieldWrapper<Scalars['Int']>;
  home_page_url: FieldWrapper<Scalars['Int']>;
  domain: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  lang: FieldWrapper<Scalars['Int']>;
  tags: FieldWrapper<Scalars['Int']>;
  author: FieldWrapper<Scalars['Int']>;
  is_private: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  expired: FieldWrapper<Scalars['Int']>;
  broken: FieldWrapper<Scalars['Int']>;
  inactive: FieldWrapper<Scalars['Int']>;
  managed: FieldWrapper<Scalars['Int']>;
  managed_by_plugin_id: FieldWrapper<Scalars['Int']>;
  filter: FieldWrapper<Scalars['Int']>;
  description: FieldWrapper<Scalars['Int']>;
  status: FieldWrapper<Scalars['Int']>;
  harvestIntervalMinutes: FieldWrapper<Scalars['Int']>;
  nextHarvestAt: FieldWrapper<Scalars['Int']>;
  retention_size: FieldWrapper<Scalars['Int']>;
  harvest_site: FieldWrapper<Scalars['Int']>;
  allowHarvestFailure: FieldWrapper<Scalars['Int']>;
  harvest_prerender: FieldWrapper<Scalars['Int']>;
  streamId: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  lastUpdatedAt: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlFeedCreateInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  domain: Scalars['String'];
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  is_private?: Maybe<Scalars['Boolean']>;
  expired?: Maybe<Scalars['Boolean']>;
  broken?: Maybe<Scalars['Boolean']>;
  inactive?: Maybe<Scalars['Boolean']>;
  managed?: Maybe<Scalars['Boolean']>;
  filter?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  retention_size?: Maybe<Scalars['Int']>;
  harvest_site?: Maybe<Scalars['Boolean']>;
  allowHarvestFailure?: Maybe<Scalars['Boolean']>;
  harvest_prerender?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutFeedsInput>;
  managed_by_plugin?: Maybe<GqlPluginCreateNestedOneWithoutFeedsInput>;
  stream: GqlStreamCreateNestedOneWithoutFeedsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  domain: Scalars['String'];
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  is_private?: Maybe<Scalars['Boolean']>;
  ownerId?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  broken?: Maybe<Scalars['Boolean']>;
  inactive?: Maybe<Scalars['Boolean']>;
  managed?: Maybe<Scalars['Boolean']>;
  managed_by_plugin_id?: Maybe<Scalars['String']>;
  filter?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  retention_size?: Maybe<Scalars['Int']>;
  harvest_site?: Maybe<Scalars['Boolean']>;
  allowHarvestFailure?: Maybe<Scalars['Boolean']>;
  harvest_prerender?: Maybe<Scalars['Boolean']>;
  streamId: Scalars['String'];
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
};

export type GqlFeedCreateManyManaged_By_PluginInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  domain: Scalars['String'];
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  is_private?: Maybe<Scalars['Boolean']>;
  ownerId?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  broken?: Maybe<Scalars['Boolean']>;
  inactive?: Maybe<Scalars['Boolean']>;
  managed?: Maybe<Scalars['Boolean']>;
  filter?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  retention_size?: Maybe<Scalars['Int']>;
  harvest_site?: Maybe<Scalars['Boolean']>;
  allowHarvestFailure?: Maybe<Scalars['Boolean']>;
  harvest_prerender?: Maybe<Scalars['Boolean']>;
  streamId: Scalars['String'];
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
};

export type GqlFeedCreateManyManaged_By_PluginInputEnvelope = {
  data: Array<GqlFeedCreateManyManaged_By_PluginInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlFeedCreateManyOwnerInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  domain: Scalars['String'];
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  is_private?: Maybe<Scalars['Boolean']>;
  expired?: Maybe<Scalars['Boolean']>;
  broken?: Maybe<Scalars['Boolean']>;
  inactive?: Maybe<Scalars['Boolean']>;
  managed?: Maybe<Scalars['Boolean']>;
  managed_by_plugin_id?: Maybe<Scalars['String']>;
  filter?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  retention_size?: Maybe<Scalars['Int']>;
  harvest_site?: Maybe<Scalars['Boolean']>;
  allowHarvestFailure?: Maybe<Scalars['Boolean']>;
  harvest_prerender?: Maybe<Scalars['Boolean']>;
  streamId: Scalars['String'];
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
};

export type GqlFeedCreateManyOwnerInputEnvelope = {
  data: Array<GqlFeedCreateManyOwnerInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlFeedCreateManyStreamInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  domain: Scalars['String'];
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  is_private?: Maybe<Scalars['Boolean']>;
  ownerId?: Maybe<Scalars['String']>;
  expired?: Maybe<Scalars['Boolean']>;
  broken?: Maybe<Scalars['Boolean']>;
  inactive?: Maybe<Scalars['Boolean']>;
  managed?: Maybe<Scalars['Boolean']>;
  managed_by_plugin_id?: Maybe<Scalars['String']>;
  filter?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  retention_size?: Maybe<Scalars['Int']>;
  harvest_site?: Maybe<Scalars['Boolean']>;
  allowHarvestFailure?: Maybe<Scalars['Boolean']>;
  harvest_prerender?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
};

export type GqlFeedCreateManyStreamInputEnvelope = {
  data: Array<GqlFeedCreateManyStreamInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlFeedCreateNestedManyWithoutManaged_By_PluginInput = {
  create?: Maybe<Array<GqlFeedCreateWithoutManaged_By_PluginInput>>;
  connectOrCreate?: Maybe<Array<GqlFeedCreateOrConnectWithoutManaged_By_PluginInput>>;
  createMany?: Maybe<GqlFeedCreateManyManaged_By_PluginInputEnvelope>;
  connect?: Maybe<Array<GqlFeedWhereUniqueInput>>;
};

export type GqlFeedCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlFeedCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlFeedCreateOrConnectWithoutOwnerInput>>;
  createMany?: Maybe<GqlFeedCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlFeedWhereUniqueInput>>;
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

export type GqlFeedCreateOrConnectWithoutManaged_By_PluginInput = {
  where: GqlFeedWhereUniqueInput;
  create: GqlFeedCreateWithoutManaged_By_PluginInput;
};

export type GqlFeedCreateOrConnectWithoutOwnerInput = {
  where: GqlFeedWhereUniqueInput;
  create: GqlFeedCreateWithoutOwnerInput;
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
  domain: Scalars['String'];
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  is_private?: Maybe<Scalars['Boolean']>;
  expired?: Maybe<Scalars['Boolean']>;
  broken?: Maybe<Scalars['Boolean']>;
  inactive?: Maybe<Scalars['Boolean']>;
  managed?: Maybe<Scalars['Boolean']>;
  filter?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  retention_size?: Maybe<Scalars['Int']>;
  harvest_site?: Maybe<Scalars['Boolean']>;
  allowHarvestFailure?: Maybe<Scalars['Boolean']>;
  harvest_prerender?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutFeedsInput>;
  managed_by_plugin?: Maybe<GqlPluginCreateNestedOneWithoutFeedsInput>;
  stream: GqlStreamCreateNestedOneWithoutFeedsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedCreateWithoutManaged_By_PluginInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  domain: Scalars['String'];
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  is_private?: Maybe<Scalars['Boolean']>;
  expired?: Maybe<Scalars['Boolean']>;
  broken?: Maybe<Scalars['Boolean']>;
  inactive?: Maybe<Scalars['Boolean']>;
  managed?: Maybe<Scalars['Boolean']>;
  filter?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  retention_size?: Maybe<Scalars['Int']>;
  harvest_site?: Maybe<Scalars['Boolean']>;
  allowHarvestFailure?: Maybe<Scalars['Boolean']>;
  harvest_prerender?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutFeedsInput>;
  stream: GqlStreamCreateNestedOneWithoutFeedsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  domain: Scalars['String'];
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  is_private?: Maybe<Scalars['Boolean']>;
  expired?: Maybe<Scalars['Boolean']>;
  broken?: Maybe<Scalars['Boolean']>;
  inactive?: Maybe<Scalars['Boolean']>;
  managed?: Maybe<Scalars['Boolean']>;
  filter?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  retention_size?: Maybe<Scalars['Int']>;
  harvest_site?: Maybe<Scalars['Boolean']>;
  allowHarvestFailure?: Maybe<Scalars['Boolean']>;
  harvest_prerender?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  managed_by_plugin?: Maybe<GqlPluginCreateNestedOneWithoutFeedsInput>;
  stream: GqlStreamCreateNestedOneWithoutFeedsInput;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedCreateWithoutStreamInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  domain: Scalars['String'];
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  is_private?: Maybe<Scalars['Boolean']>;
  expired?: Maybe<Scalars['Boolean']>;
  broken?: Maybe<Scalars['Boolean']>;
  inactive?: Maybe<Scalars['Boolean']>;
  managed?: Maybe<Scalars['Boolean']>;
  filter?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  retention_size?: Maybe<Scalars['Int']>;
  harvest_site?: Maybe<Scalars['Boolean']>;
  allowHarvestFailure?: Maybe<Scalars['Boolean']>;
  harvest_prerender?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutFeedsInput>;
  managed_by_plugin?: Maybe<GqlPluginCreateNestedOneWithoutFeedsInput>;
  subscriptions?: Maybe<GqlSubscriptionCreateNestedManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedCreateWithoutSubscriptionsInput = {
  id?: Maybe<Scalars['String']>;
  feed_url: Scalars['String'];
  home_page_url?: Maybe<Scalars['String']>;
  domain: Scalars['String'];
  title?: Maybe<Scalars['String']>;
  lang?: Maybe<Scalars['String']>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<Scalars['String']>;
  is_private?: Maybe<Scalars['Boolean']>;
  expired?: Maybe<Scalars['Boolean']>;
  broken?: Maybe<Scalars['Boolean']>;
  inactive?: Maybe<Scalars['Boolean']>;
  managed?: Maybe<Scalars['Boolean']>;
  filter?: Maybe<Scalars['String']>;
  description?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<Scalars['Int']>;
  nextHarvestAt?: Maybe<Scalars['DateTime']>;
  retention_size?: Maybe<Scalars['Int']>;
  harvest_site?: Maybe<Scalars['Boolean']>;
  allowHarvestFailure?: Maybe<Scalars['Boolean']>;
  harvest_prerender?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutFeedsInput>;
  managed_by_plugin?: Maybe<GqlPluginCreateNestedOneWithoutFeedsInput>;
  stream: GqlStreamCreateNestedOneWithoutFeedsInput;
  events?: Maybe<GqlFeedEventCreateNestedManyWithoutFeedInput>;
};

export type GqlFeedEvent = {
  __typename?: 'FeedEvent';
  id: FieldWrapper<Scalars['String']>;
  message: FieldWrapper<Scalars['String']>;
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
  message: Scalars['String'];
  createdAt?: Maybe<Scalars['DateTime']>;
  is_error?: Maybe<Scalars['Boolean']>;
  feed: GqlFeedCreateNestedOneWithoutEventsInput;
};

export type GqlFeedEventCreateManyFeedInput = {
  id?: Maybe<Scalars['String']>;
  message: Scalars['String'];
  createdAt?: Maybe<Scalars['DateTime']>;
  is_error?: Maybe<Scalars['Boolean']>;
};

export type GqlFeedEventCreateManyFeedInputEnvelope = {
  data: Array<GqlFeedEventCreateManyFeedInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlFeedEventCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  message: Scalars['String'];
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
  message: Scalars['String'];
  createdAt?: Maybe<Scalars['DateTime']>;
  is_error?: Maybe<Scalars['Boolean']>;
};

export type GqlFeedEventGroupBy = {
  __typename?: 'FeedEventGroupBy';
  id: FieldWrapper<Scalars['String']>;
  message: FieldWrapper<Scalars['String']>;
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
  message?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  is_error?: Maybe<FieldWrapper<Scalars['Boolean']>>;
};

export type GqlFeedEventMinAggregate = {
  __typename?: 'FeedEventMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  message?: Maybe<FieldWrapper<Scalars['String']>>;
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
  message?: Maybe<GqlStringFilter>;
  feedId?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  is_error?: Maybe<GqlBoolFilter>;
};

export type GqlFeedEventScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlFeedEventScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlFeedEventScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlFeedEventScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  message?: Maybe<GqlStringWithAggregatesFilter>;
  feedId?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  is_error?: Maybe<GqlBoolWithAggregatesFilter>;
};

export type GqlFeedEventUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  message?: Maybe<GqlStringFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  is_error?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  feed?: Maybe<GqlFeedUpdateOneRequiredWithoutEventsInput>;
};

export type GqlFeedEventUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  message?: Maybe<GqlStringFieldUpdateOperationsInput>;
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
  message?: Maybe<GqlStringFieldUpdateOperationsInput>;
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
  message?: Maybe<GqlStringFilter>;
  feed?: Maybe<GqlFeedRelationFilter>;
  feedId?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  is_error?: Maybe<GqlBoolFilter>;
};

export type GqlFeedEventWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlFeedFeed_UrlOwnerIdCompoundUniqueInput = {
  feed_url: Scalars['String'];
  ownerId: Scalars['String'];
};

export type GqlFeedGroupBy = {
  __typename?: 'FeedGroupBy';
  id: FieldWrapper<Scalars['String']>;
  feed_url: FieldWrapper<Scalars['String']>;
  home_page_url?: Maybe<FieldWrapper<Scalars['String']>>;
  domain: FieldWrapper<Scalars['String']>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  lang?: Maybe<FieldWrapper<Scalars['String']>>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  is_private: FieldWrapper<Scalars['Boolean']>;
  ownerId: FieldWrapper<Scalars['String']>;
  expired: FieldWrapper<Scalars['Boolean']>;
  broken: FieldWrapper<Scalars['Boolean']>;
  inactive: FieldWrapper<Scalars['Boolean']>;
  managed: FieldWrapper<Scalars['Boolean']>;
  managed_by_plugin_id?: Maybe<FieldWrapper<Scalars['String']>>;
  filter?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  status: FieldWrapper<Scalars['String']>;
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  nextHarvestAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  retention_size?: Maybe<FieldWrapper<Scalars['Int']>>;
  harvest_site: FieldWrapper<Scalars['Boolean']>;
  allowHarvestFailure: FieldWrapper<Scalars['Boolean']>;
  harvest_prerender: FieldWrapper<Scalars['Boolean']>;
  streamId: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
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
  domain?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  lang?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  is_private?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  expired?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  broken?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  inactive?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  managed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  managed_by_plugin_id?: Maybe<FieldWrapper<Scalars['String']>>;
  filter?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  status?: Maybe<FieldWrapper<Scalars['String']>>;
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  nextHarvestAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  retention_size?: Maybe<FieldWrapper<Scalars['Int']>>;
  harvest_site?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  allowHarvestFailure?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  harvest_prerender?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  streamId?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlFeedMinAggregate = {
  __typename?: 'FeedMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  feed_url?: Maybe<FieldWrapper<Scalars['String']>>;
  home_page_url?: Maybe<FieldWrapper<Scalars['String']>>;
  domain?: Maybe<FieldWrapper<Scalars['String']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  lang?: Maybe<FieldWrapper<Scalars['String']>>;
  author?: Maybe<FieldWrapper<Scalars['String']>>;
  is_private?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  expired?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  broken?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  inactive?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  managed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  managed_by_plugin_id?: Maybe<FieldWrapper<Scalars['String']>>;
  filter?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  status?: Maybe<FieldWrapper<Scalars['String']>>;
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  nextHarvestAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  retention_size?: Maybe<FieldWrapper<Scalars['Int']>>;
  harvest_site?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  allowHarvestFailure?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  harvest_prerender?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  streamId?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlFeedOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  feed_url?: Maybe<GqlSortOrder>;
  home_page_url?: Maybe<GqlSortOrder>;
  domain?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  lang?: Maybe<GqlSortOrder>;
  tags?: Maybe<GqlSortOrder>;
  author?: Maybe<GqlSortOrder>;
  is_private?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  expired?: Maybe<GqlSortOrder>;
  broken?: Maybe<GqlSortOrder>;
  inactive?: Maybe<GqlSortOrder>;
  managed?: Maybe<GqlSortOrder>;
  managed_by_plugin_id?: Maybe<GqlSortOrder>;
  filter?: Maybe<GqlSortOrder>;
  description?: Maybe<GqlSortOrder>;
  status?: Maybe<GqlSortOrder>;
  harvestIntervalMinutes?: Maybe<GqlSortOrder>;
  nextHarvestAt?: Maybe<GqlSortOrder>;
  retention_size?: Maybe<GqlSortOrder>;
  harvest_site?: Maybe<GqlSortOrder>;
  allowHarvestFailure?: Maybe<GqlSortOrder>;
  harvest_prerender?: Maybe<GqlSortOrder>;
  streamId?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  lastUpdatedAt?: Maybe<GqlSortOrder>;
};

export type GqlFeedRelationFilter = {
  is?: Maybe<GqlFeedWhereInput>;
  isNot?: Maybe<GqlFeedWhereInput>;
};

export enum GqlFeedScalarFieldEnum {
  Id = 'id',
  FeedUrl = 'feed_url',
  HomePageUrl = 'home_page_url',
  Domain = 'domain',
  Title = 'title',
  Lang = 'lang',
  Tags = 'tags',
  Author = 'author',
  IsPrivate = 'is_private',
  OwnerId = 'ownerId',
  Expired = 'expired',
  Broken = 'broken',
  Inactive = 'inactive',
  Managed = 'managed',
  ManagedByPluginId = 'managed_by_plugin_id',
  Filter = 'filter',
  Description = 'description',
  Status = 'status',
  HarvestIntervalMinutes = 'harvestIntervalMinutes',
  NextHarvestAt = 'nextHarvestAt',
  RetentionSize = 'retention_size',
  HarvestSite = 'harvest_site',
  AllowHarvestFailure = 'allowHarvestFailure',
  HarvestPrerender = 'harvest_prerender',
  StreamId = 'streamId',
  CreatedAt = 'createdAt',
  LastUpdatedAt = 'lastUpdatedAt'
}

export type GqlFeedScalarWhereInput = {
  AND?: Maybe<Array<GqlFeedScalarWhereInput>>;
  OR?: Maybe<Array<GqlFeedScalarWhereInput>>;
  NOT?: Maybe<Array<GqlFeedScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  feed_url?: Maybe<GqlStringFilter>;
  home_page_url?: Maybe<GqlStringNullableFilter>;
  domain?: Maybe<GqlStringFilter>;
  title?: Maybe<GqlStringNullableFilter>;
  lang?: Maybe<GqlStringNullableFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  author?: Maybe<GqlStringNullableFilter>;
  is_private?: Maybe<GqlBoolFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  expired?: Maybe<GqlBoolFilter>;
  broken?: Maybe<GqlBoolFilter>;
  inactive?: Maybe<GqlBoolFilter>;
  managed?: Maybe<GqlBoolFilter>;
  managed_by_plugin_id?: Maybe<GqlStringNullableFilter>;
  filter?: Maybe<GqlStringNullableFilter>;
  description?: Maybe<GqlStringNullableFilter>;
  status?: Maybe<GqlStringFilter>;
  harvestIntervalMinutes?: Maybe<GqlIntNullableFilter>;
  nextHarvestAt?: Maybe<GqlDateTimeNullableFilter>;
  retention_size?: Maybe<GqlIntNullableFilter>;
  harvest_site?: Maybe<GqlBoolFilter>;
  allowHarvestFailure?: Maybe<GqlBoolFilter>;
  harvest_prerender?: Maybe<GqlBoolFilter>;
  streamId?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableFilter>;
};

export type GqlFeedScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlFeedScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlFeedScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlFeedScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  feed_url?: Maybe<GqlStringWithAggregatesFilter>;
  home_page_url?: Maybe<GqlStringNullableWithAggregatesFilter>;
  domain?: Maybe<GqlStringWithAggregatesFilter>;
  title?: Maybe<GqlStringNullableWithAggregatesFilter>;
  lang?: Maybe<GqlStringNullableWithAggregatesFilter>;
  tags?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  author?: Maybe<GqlStringNullableWithAggregatesFilter>;
  is_private?: Maybe<GqlBoolWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
  expired?: Maybe<GqlBoolWithAggregatesFilter>;
  broken?: Maybe<GqlBoolWithAggregatesFilter>;
  inactive?: Maybe<GqlBoolWithAggregatesFilter>;
  managed?: Maybe<GqlBoolWithAggregatesFilter>;
  managed_by_plugin_id?: Maybe<GqlStringNullableWithAggregatesFilter>;
  filter?: Maybe<GqlStringNullableWithAggregatesFilter>;
  description?: Maybe<GqlStringNullableWithAggregatesFilter>;
  status?: Maybe<GqlStringWithAggregatesFilter>;
  harvestIntervalMinutes?: Maybe<GqlIntNullableWithAggregatesFilter>;
  nextHarvestAt?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
  retention_size?: Maybe<GqlIntNullableWithAggregatesFilter>;
  harvest_site?: Maybe<GqlBoolWithAggregatesFilter>;
  allowHarvestFailure?: Maybe<GqlBoolWithAggregatesFilter>;
  harvest_prerender?: Maybe<GqlBoolWithAggregatesFilter>;
  streamId?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
};

export type GqlFeedSumAggregate = {
  __typename?: 'FeedSumAggregate';
  harvestIntervalMinutes?: Maybe<FieldWrapper<Scalars['Int']>>;
  retention_size?: Maybe<FieldWrapper<Scalars['Int']>>;
};

export type GqlFeedUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  domain?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  is_private?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  broken?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  managed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  filter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  retention_size?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  harvest_site?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  allowHarvestFailure?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  harvest_prerender?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutFeedsInput>;
  managed_by_plugin?: Maybe<GqlPluginUpdateOneWithoutFeedsInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutFeedsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  domain?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  is_private?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  broken?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  managed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  filter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  retention_size?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  harvest_site?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  allowHarvestFailure?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  harvest_prerender?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
};

export type GqlFeedUpdateManyWithWhereWithoutManaged_By_PluginInput = {
  where: GqlFeedScalarWhereInput;
  data: GqlFeedUpdateManyMutationInput;
};

export type GqlFeedUpdateManyWithWhereWithoutOwnerInput = {
  where: GqlFeedScalarWhereInput;
  data: GqlFeedUpdateManyMutationInput;
};

export type GqlFeedUpdateManyWithWhereWithoutStreamInput = {
  where: GqlFeedScalarWhereInput;
  data: GqlFeedUpdateManyMutationInput;
};

export type GqlFeedUpdateManyWithoutManaged_By_PluginInput = {
  create?: Maybe<Array<GqlFeedCreateWithoutManaged_By_PluginInput>>;
  connectOrCreate?: Maybe<Array<GqlFeedCreateOrConnectWithoutManaged_By_PluginInput>>;
  upsert?: Maybe<Array<GqlFeedUpsertWithWhereUniqueWithoutManaged_By_PluginInput>>;
  createMany?: Maybe<GqlFeedCreateManyManaged_By_PluginInputEnvelope>;
  connect?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  set?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  delete?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  update?: Maybe<Array<GqlFeedUpdateWithWhereUniqueWithoutManaged_By_PluginInput>>;
  updateMany?: Maybe<Array<GqlFeedUpdateManyWithWhereWithoutManaged_By_PluginInput>>;
  deleteMany?: Maybe<Array<GqlFeedScalarWhereInput>>;
};

export type GqlFeedUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlFeedCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlFeedCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<GqlFeedUpsertWithWhereUniqueWithoutOwnerInput>>;
  createMany?: Maybe<GqlFeedCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  set?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  delete?: Maybe<Array<GqlFeedWhereUniqueInput>>;
  update?: Maybe<Array<GqlFeedUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<GqlFeedUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<GqlFeedScalarWhereInput>>;
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

export type GqlFeedUpdateWithWhereUniqueWithoutManaged_By_PluginInput = {
  where: GqlFeedWhereUniqueInput;
  data: GqlFeedUpdateWithoutManaged_By_PluginInput;
};

export type GqlFeedUpdateWithWhereUniqueWithoutOwnerInput = {
  where: GqlFeedWhereUniqueInput;
  data: GqlFeedUpdateWithoutOwnerInput;
};

export type GqlFeedUpdateWithWhereUniqueWithoutStreamInput = {
  where: GqlFeedWhereUniqueInput;
  data: GqlFeedUpdateWithoutStreamInput;
};

export type GqlFeedUpdateWithoutEventsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  domain?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  is_private?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  broken?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  managed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  filter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  retention_size?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  harvest_site?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  allowHarvestFailure?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  harvest_prerender?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutFeedsInput>;
  managed_by_plugin?: Maybe<GqlPluginUpdateOneWithoutFeedsInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutFeedsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpdateWithoutManaged_By_PluginInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  domain?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  is_private?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  broken?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  managed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  filter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  retention_size?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  harvest_site?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  allowHarvestFailure?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  harvest_prerender?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutFeedsInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutFeedsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  domain?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  is_private?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  broken?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  managed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  filter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  retention_size?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  harvest_site?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  allowHarvestFailure?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  harvest_prerender?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  managed_by_plugin?: Maybe<GqlPluginUpdateOneWithoutFeedsInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutFeedsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpdateWithoutStreamInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  domain?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  is_private?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  broken?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  managed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  filter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  retention_size?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  harvest_site?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  allowHarvestFailure?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  harvest_prerender?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutFeedsInput>;
  managed_by_plugin?: Maybe<GqlPluginUpdateOneWithoutFeedsInput>;
  subscriptions?: Maybe<GqlSubscriptionUpdateManyWithoutFeedInput>;
  events?: Maybe<GqlFeedEventUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpdateWithoutSubscriptionsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feed_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  home_page_url?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  domain?: Maybe<GqlStringFieldUpdateOperationsInput>;
  title?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lang?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  author?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  is_private?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  expired?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  broken?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  managed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  filter?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  status?: Maybe<GqlStringFieldUpdateOperationsInput>;
  harvestIntervalMinutes?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  nextHarvestAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  retention_size?: Maybe<GqlNullableIntFieldUpdateOperationsInput>;
  harvest_site?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  allowHarvestFailure?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  harvest_prerender?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutFeedsInput>;
  managed_by_plugin?: Maybe<GqlPluginUpdateOneWithoutFeedsInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutFeedsInput>;
  events?: Maybe<GqlFeedEventUpdateManyWithoutFeedInput>;
};

export type GqlFeedUpsertWithWhereUniqueWithoutManaged_By_PluginInput = {
  where: GqlFeedWhereUniqueInput;
  update: GqlFeedUpdateWithoutManaged_By_PluginInput;
  create: GqlFeedCreateWithoutManaged_By_PluginInput;
};

export type GqlFeedUpsertWithWhereUniqueWithoutOwnerInput = {
  where: GqlFeedWhereUniqueInput;
  update: GqlFeedUpdateWithoutOwnerInput;
  create: GqlFeedCreateWithoutOwnerInput;
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
  domain?: Maybe<GqlStringFilter>;
  title?: Maybe<GqlStringNullableFilter>;
  lang?: Maybe<GqlStringNullableFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  author?: Maybe<GqlStringNullableFilter>;
  is_private?: Maybe<GqlBoolFilter>;
  owner?: Maybe<GqlUserRelationFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  expired?: Maybe<GqlBoolFilter>;
  broken?: Maybe<GqlBoolFilter>;
  inactive?: Maybe<GqlBoolFilter>;
  managed?: Maybe<GqlBoolFilter>;
  managed_by_plugin?: Maybe<GqlPluginRelationFilter>;
  managed_by_plugin_id?: Maybe<GqlStringNullableFilter>;
  filter?: Maybe<GqlStringNullableFilter>;
  description?: Maybe<GqlStringNullableFilter>;
  status?: Maybe<GqlStringFilter>;
  harvestIntervalMinutes?: Maybe<GqlIntNullableFilter>;
  nextHarvestAt?: Maybe<GqlDateTimeNullableFilter>;
  retention_size?: Maybe<GqlIntNullableFilter>;
  harvest_site?: Maybe<GqlBoolFilter>;
  allowHarvestFailure?: Maybe<GqlBoolFilter>;
  harvest_prerender?: Maybe<GqlBoolFilter>;
  stream?: Maybe<GqlStreamRelationFilter>;
  streamId?: Maybe<GqlStringFilter>;
  subscriptions?: Maybe<GqlSubscriptionListRelationFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableFilter>;
  events?: Maybe<GqlFeedEventListRelationFilter>;
};

export type GqlFeedWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
  feed_url_ownerId?: Maybe<GqlFeedFeed_UrlOwnerIdCompoundUniqueInput>;
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

export type GqlGenericFeedRule = {
  __typename?: 'GenericFeedRule';
  feed_url: FieldWrapper<Scalars['String']>;
  linkXPath: FieldWrapper<Scalars['String']>;
  extendContext: FieldWrapper<Scalars['String']>;
  contextXPath: FieldWrapper<Scalars['String']>;
  count: FieldWrapper<Scalars['Float']>;
  score: FieldWrapper<Scalars['Float']>;
  samples: Array<FieldWrapper<GqlArticle>>;
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
  createArticleExporter: FieldWrapper<GqlArticleExporter>;
  createManyArticleExporter: FieldWrapper<GqlAffectedRowsOutput>;
  deleteArticleExporter?: Maybe<FieldWrapper<GqlArticleExporter>>;
  updateArticleExporter?: Maybe<FieldWrapper<GqlArticleExporter>>;
  deleteManyArticleExporter: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyArticleExporter: FieldWrapper<GqlAffectedRowsOutput>;
  upsertArticleExporter: FieldWrapper<GqlArticleExporter>;
  createArticleExporterTarget: FieldWrapper<GqlArticleExporterTarget>;
  createManyArticleExporterTarget: FieldWrapper<GqlAffectedRowsOutput>;
  deleteArticleExporterTarget?: Maybe<FieldWrapper<GqlArticleExporterTarget>>;
  updateArticleExporterTarget?: Maybe<FieldWrapper<GqlArticleExporterTarget>>;
  deleteManyArticleExporterTarget: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyArticleExporterTarget: FieldWrapper<GqlAffectedRowsOutput>;
  upsertArticleExporterTarget: FieldWrapper<GqlArticleExporterTarget>;
  createArticlePostProcessor: FieldWrapper<GqlArticlePostProcessor>;
  createManyArticlePostProcessor: FieldWrapper<GqlAffectedRowsOutput>;
  deleteArticlePostProcessor?: Maybe<FieldWrapper<GqlArticlePostProcessor>>;
  updateArticlePostProcessor?: Maybe<FieldWrapper<GqlArticlePostProcessor>>;
  deleteManyArticlePostProcessor: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyArticlePostProcessor: FieldWrapper<GqlAffectedRowsOutput>;
  upsertArticlePostProcessor: FieldWrapper<GqlArticlePostProcessor>;
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
  createEventHook: FieldWrapper<GqlEventHook>;
  createManyEventHook: FieldWrapper<GqlAffectedRowsOutput>;
  deleteEventHook?: Maybe<FieldWrapper<GqlEventHook>>;
  updateEventHook?: Maybe<FieldWrapper<GqlEventHook>>;
  deleteManyEventHook: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyEventHook: FieldWrapper<GqlAffectedRowsOutput>;
  upsertEventHook: FieldWrapper<GqlEventHook>;
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
  createNoFollowUrl: FieldWrapper<GqlNoFollowUrl>;
  createManyNoFollowUrl: FieldWrapper<GqlAffectedRowsOutput>;
  deleteNoFollowUrl?: Maybe<FieldWrapper<GqlNoFollowUrl>>;
  updateNoFollowUrl?: Maybe<FieldWrapper<GqlNoFollowUrl>>;
  deleteManyNoFollowUrl: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyNoFollowUrl: FieldWrapper<GqlAffectedRowsOutput>;
  upsertNoFollowUrl: FieldWrapper<GqlNoFollowUrl>;
  createNotebook: FieldWrapper<GqlNotebook>;
  createManyNotebook: FieldWrapper<GqlAffectedRowsOutput>;
  deleteNotebook?: Maybe<FieldWrapper<GqlNotebook>>;
  updateNotebook?: Maybe<FieldWrapper<GqlNotebook>>;
  deleteManyNotebook: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyNotebook: FieldWrapper<GqlAffectedRowsOutput>;
  upsertNotebook: FieldWrapper<GqlNotebook>;
  createPlugin: FieldWrapper<GqlPlugin>;
  createManyPlugin: FieldWrapper<GqlAffectedRowsOutput>;
  deletePlugin?: Maybe<FieldWrapper<GqlPlugin>>;
  updatePlugin?: Maybe<FieldWrapper<GqlPlugin>>;
  deleteManyPlugin: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyPlugin: FieldWrapper<GqlAffectedRowsOutput>;
  upsertPlugin: FieldWrapper<GqlPlugin>;
  createProfileSettings: FieldWrapper<GqlProfileSettings>;
  createManyProfileSettings: FieldWrapper<GqlAffectedRowsOutput>;
  deleteProfileSettings?: Maybe<FieldWrapper<GqlProfileSettings>>;
  updateProfileSettings?: Maybe<FieldWrapper<GqlProfileSettings>>;
  deleteManyProfileSettings: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyProfileSettings: FieldWrapper<GqlAffectedRowsOutput>;
  upsertProfileSettings: FieldWrapper<GqlProfileSettings>;
  createReferencedArticleRef: FieldWrapper<GqlReferencedArticleRef>;
  createManyReferencedArticleRef: FieldWrapper<GqlAffectedRowsOutput>;
  deleteReferencedArticleRef?: Maybe<FieldWrapper<GqlReferencedArticleRef>>;
  updateReferencedArticleRef?: Maybe<FieldWrapper<GqlReferencedArticleRef>>;
  deleteManyReferencedArticleRef: FieldWrapper<GqlAffectedRowsOutput>;
  updateManyReferencedArticleRef: FieldWrapper<GqlAffectedRowsOutput>;
  upsertReferencedArticleRef: FieldWrapper<GqlReferencedArticleRef>;
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
  subscribeToFeed: FieldWrapper<GqlSubscription>;
  getOauthRedirect: FieldWrapper<Scalars['String']>;
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


export type GqlMutationCreateArticleExporterArgs = {
  data: GqlArticleExporterCreateInput;
};


export type GqlMutationCreateManyArticleExporterArgs = {
  data: Array<GqlArticleExporterCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteArticleExporterArgs = {
  where: GqlArticleExporterWhereUniqueInput;
};


export type GqlMutationUpdateArticleExporterArgs = {
  data: GqlArticleExporterUpdateInput;
  where: GqlArticleExporterWhereUniqueInput;
};


export type GqlMutationDeleteManyArticleExporterArgs = {
  where?: Maybe<GqlArticleExporterWhereInput>;
};


export type GqlMutationUpdateManyArticleExporterArgs = {
  data: GqlArticleExporterUpdateManyMutationInput;
  where?: Maybe<GqlArticleExporterWhereInput>;
};


export type GqlMutationUpsertArticleExporterArgs = {
  where: GqlArticleExporterWhereUniqueInput;
  create: GqlArticleExporterCreateInput;
  update: GqlArticleExporterUpdateInput;
};


export type GqlMutationCreateArticleExporterTargetArgs = {
  data: GqlArticleExporterTargetCreateInput;
};


export type GqlMutationCreateManyArticleExporterTargetArgs = {
  data: Array<GqlArticleExporterTargetCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteArticleExporterTargetArgs = {
  where: GqlArticleExporterTargetWhereUniqueInput;
};


export type GqlMutationUpdateArticleExporterTargetArgs = {
  data: GqlArticleExporterTargetUpdateInput;
  where: GqlArticleExporterTargetWhereUniqueInput;
};


export type GqlMutationDeleteManyArticleExporterTargetArgs = {
  where?: Maybe<GqlArticleExporterTargetWhereInput>;
};


export type GqlMutationUpdateManyArticleExporterTargetArgs = {
  data: GqlArticleExporterTargetUpdateManyMutationInput;
  where?: Maybe<GqlArticleExporterTargetWhereInput>;
};


export type GqlMutationUpsertArticleExporterTargetArgs = {
  where: GqlArticleExporterTargetWhereUniqueInput;
  create: GqlArticleExporterTargetCreateInput;
  update: GqlArticleExporterTargetUpdateInput;
};


export type GqlMutationCreateArticlePostProcessorArgs = {
  data: GqlArticlePostProcessorCreateInput;
};


export type GqlMutationCreateManyArticlePostProcessorArgs = {
  data: Array<GqlArticlePostProcessorCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteArticlePostProcessorArgs = {
  where: GqlArticlePostProcessorWhereUniqueInput;
};


export type GqlMutationUpdateArticlePostProcessorArgs = {
  data: GqlArticlePostProcessorUpdateInput;
  where: GqlArticlePostProcessorWhereUniqueInput;
};


export type GqlMutationDeleteManyArticlePostProcessorArgs = {
  where?: Maybe<GqlArticlePostProcessorWhereInput>;
};


export type GqlMutationUpdateManyArticlePostProcessorArgs = {
  data: GqlArticlePostProcessorUpdateManyMutationInput;
  where?: Maybe<GqlArticlePostProcessorWhereInput>;
};


export type GqlMutationUpsertArticlePostProcessorArgs = {
  where: GqlArticlePostProcessorWhereUniqueInput;
  create: GqlArticlePostProcessorCreateInput;
  update: GqlArticlePostProcessorUpdateInput;
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


export type GqlMutationCreateEventHookArgs = {
  data: GqlEventHookCreateInput;
};


export type GqlMutationCreateManyEventHookArgs = {
  data: Array<GqlEventHookCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteEventHookArgs = {
  where: GqlEventHookWhereUniqueInput;
};


export type GqlMutationUpdateEventHookArgs = {
  data: GqlEventHookUpdateInput;
  where: GqlEventHookWhereUniqueInput;
};


export type GqlMutationDeleteManyEventHookArgs = {
  where?: Maybe<GqlEventHookWhereInput>;
};


export type GqlMutationUpdateManyEventHookArgs = {
  data: GqlEventHookUpdateManyMutationInput;
  where?: Maybe<GqlEventHookWhereInput>;
};


export type GqlMutationUpsertEventHookArgs = {
  where: GqlEventHookWhereUniqueInput;
  create: GqlEventHookCreateInput;
  update: GqlEventHookUpdateInput;
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


export type GqlMutationCreateNoFollowUrlArgs = {
  data: GqlNoFollowUrlCreateInput;
};


export type GqlMutationCreateManyNoFollowUrlArgs = {
  data: Array<GqlNoFollowUrlCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteNoFollowUrlArgs = {
  where: GqlNoFollowUrlWhereUniqueInput;
};


export type GqlMutationUpdateNoFollowUrlArgs = {
  data: GqlNoFollowUrlUpdateInput;
  where: GqlNoFollowUrlWhereUniqueInput;
};


export type GqlMutationDeleteManyNoFollowUrlArgs = {
  where?: Maybe<GqlNoFollowUrlWhereInput>;
};


export type GqlMutationUpdateManyNoFollowUrlArgs = {
  data: GqlNoFollowUrlUpdateManyMutationInput;
  where?: Maybe<GqlNoFollowUrlWhereInput>;
};


export type GqlMutationUpsertNoFollowUrlArgs = {
  where: GqlNoFollowUrlWhereUniqueInput;
  create: GqlNoFollowUrlCreateInput;
  update: GqlNoFollowUrlUpdateInput;
};


export type GqlMutationCreateNotebookArgs = {
  data: GqlNotebookCreateInput;
};


export type GqlMutationCreateManyNotebookArgs = {
  data: Array<GqlNotebookCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteNotebookArgs = {
  where: GqlNotebookWhereUniqueInput;
};


export type GqlMutationUpdateNotebookArgs = {
  data: GqlNotebookUpdateInput;
  where: GqlNotebookWhereUniqueInput;
};


export type GqlMutationDeleteManyNotebookArgs = {
  where?: Maybe<GqlNotebookWhereInput>;
};


export type GqlMutationUpdateManyNotebookArgs = {
  data: GqlNotebookUpdateManyMutationInput;
  where?: Maybe<GqlNotebookWhereInput>;
};


export type GqlMutationUpsertNotebookArgs = {
  where: GqlNotebookWhereUniqueInput;
  create: GqlNotebookCreateInput;
  update: GqlNotebookUpdateInput;
};


export type GqlMutationCreatePluginArgs = {
  data: GqlPluginCreateInput;
};


export type GqlMutationCreateManyPluginArgs = {
  data: Array<GqlPluginCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeletePluginArgs = {
  where: GqlPluginWhereUniqueInput;
};


export type GqlMutationUpdatePluginArgs = {
  data: GqlPluginUpdateInput;
  where: GqlPluginWhereUniqueInput;
};


export type GqlMutationDeleteManyPluginArgs = {
  where?: Maybe<GqlPluginWhereInput>;
};


export type GqlMutationUpdateManyPluginArgs = {
  data: GqlPluginUpdateManyMutationInput;
  where?: Maybe<GqlPluginWhereInput>;
};


export type GqlMutationUpsertPluginArgs = {
  where: GqlPluginWhereUniqueInput;
  create: GqlPluginCreateInput;
  update: GqlPluginUpdateInput;
};


export type GqlMutationCreateProfileSettingsArgs = {
  data: GqlProfileSettingsCreateInput;
};


export type GqlMutationCreateManyProfileSettingsArgs = {
  data: Array<GqlProfileSettingsCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteProfileSettingsArgs = {
  where: GqlProfileSettingsWhereUniqueInput;
};


export type GqlMutationUpdateProfileSettingsArgs = {
  data: GqlProfileSettingsUpdateInput;
  where: GqlProfileSettingsWhereUniqueInput;
};


export type GqlMutationDeleteManyProfileSettingsArgs = {
  where?: Maybe<GqlProfileSettingsWhereInput>;
};


export type GqlMutationUpdateManyProfileSettingsArgs = {
  data: GqlProfileSettingsUpdateManyMutationInput;
  where?: Maybe<GqlProfileSettingsWhereInput>;
};


export type GqlMutationUpsertProfileSettingsArgs = {
  where: GqlProfileSettingsWhereUniqueInput;
  create: GqlProfileSettingsCreateInput;
  update: GqlProfileSettingsUpdateInput;
};


export type GqlMutationCreateReferencedArticleRefArgs = {
  data: GqlReferencedArticleRefCreateInput;
};


export type GqlMutationCreateManyReferencedArticleRefArgs = {
  data: Array<GqlReferencedArticleRefCreateManyInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};


export type GqlMutationDeleteReferencedArticleRefArgs = {
  where: GqlReferencedArticleRefWhereUniqueInput;
};


export type GqlMutationUpdateReferencedArticleRefArgs = {
  data: GqlReferencedArticleRefUpdateInput;
  where: GqlReferencedArticleRefWhereUniqueInput;
};


export type GqlMutationDeleteManyReferencedArticleRefArgs = {
  where?: Maybe<GqlReferencedArticleRefWhereInput>;
};


export type GqlMutationUpdateManyReferencedArticleRefArgs = {
  data: GqlReferencedArticleRefUpdateManyMutationInput;
  where?: Maybe<GqlReferencedArticleRefWhereInput>;
};


export type GqlMutationUpsertReferencedArticleRefArgs = {
  where: GqlReferencedArticleRefWhereUniqueInput;
  create: GqlReferencedArticleRefCreateInput;
  update: GqlReferencedArticleRefUpdateInput;
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


export type GqlMutationSubscribeToFeedArgs = {
  email: Scalars['String'];
  bucketId: Scalars['String'];
  feedUrl: Scalars['String'];
};

export type GqlNativeFeedRef = {
  __typename?: 'NativeFeedRef';
  feed_url: FieldWrapper<Scalars['String']>;
  feed_type: FieldWrapper<Scalars['String']>;
  home_page_url: FieldWrapper<Scalars['String']>;
  title: FieldWrapper<Scalars['String']>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlNestedBoolFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<GqlNestedBoolFilter>;
};

export type GqlNestedBoolNullableFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<GqlNestedBoolNullableFilter>;
};

export type GqlNestedBoolNullableWithAggregatesFilter = {
  equals?: Maybe<Scalars['Boolean']>;
  not?: Maybe<GqlNestedBoolNullableWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntNullableFilter>;
  _min?: Maybe<GqlNestedBoolNullableFilter>;
  _max?: Maybe<GqlNestedBoolNullableFilter>;
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

export type GqlNoFollowUrl = {
  __typename?: 'NoFollowUrl';
  id: FieldWrapper<Scalars['String']>;
  url_prefix: FieldWrapper<Scalars['String']>;
};

export type GqlNoFollowUrlCountAggregate = {
  __typename?: 'NoFollowUrlCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  url_prefix: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlNoFollowUrlCreateInput = {
  id?: Maybe<Scalars['String']>;
  url_prefix: Scalars['String'];
};

export type GqlNoFollowUrlCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  url_prefix: Scalars['String'];
};

export type GqlNoFollowUrlGroupBy = {
  __typename?: 'NoFollowUrlGroupBy';
  id: FieldWrapper<Scalars['String']>;
  url_prefix: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlNoFollowUrlCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlNoFollowUrlMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlNoFollowUrlMaxAggregate>>;
};

export type GqlNoFollowUrlMaxAggregate = {
  __typename?: 'NoFollowUrlMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  url_prefix?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlNoFollowUrlMinAggregate = {
  __typename?: 'NoFollowUrlMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  url_prefix?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlNoFollowUrlOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  url_prefix?: Maybe<GqlSortOrder>;
};

export enum GqlNoFollowUrlScalarFieldEnum {
  Id = 'id',
  UrlPrefix = 'url_prefix'
}

export type GqlNoFollowUrlScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlNoFollowUrlScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlNoFollowUrlScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlNoFollowUrlScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  url_prefix?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlNoFollowUrlUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  url_prefix?: Maybe<GqlStringFieldUpdateOperationsInput>;
};

export type GqlNoFollowUrlUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  url_prefix?: Maybe<GqlStringFieldUpdateOperationsInput>;
};

export type GqlNoFollowUrlWhereInput = {
  AND?: Maybe<Array<GqlNoFollowUrlWhereInput>>;
  OR?: Maybe<Array<GqlNoFollowUrlWhereInput>>;
  NOT?: Maybe<Array<GqlNoFollowUrlWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  url_prefix?: Maybe<GqlStringFilter>;
};

export type GqlNoFollowUrlWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
  url_prefix?: Maybe<Scalars['String']>;
};

export type GqlNotebook = {
  __typename?: 'Notebook';
  id: FieldWrapper<Scalars['String']>;
  name: FieldWrapper<Scalars['String']>;
  description: FieldWrapper<Scalars['String']>;
  readonly: FieldWrapper<Scalars['Boolean']>;
  listed: FieldWrapper<Scalars['Boolean']>;
  streamId: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  stream: FieldWrapper<GqlStream>;
  owner: FieldWrapper<GqlUser>;
};

export type GqlNotebookCountAggregate = {
  __typename?: 'NotebookCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  name: FieldWrapper<Scalars['Int']>;
  description: FieldWrapper<Scalars['Int']>;
  readonly: FieldWrapper<Scalars['Int']>;
  listed: FieldWrapper<Scalars['Int']>;
  streamId: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  updatedAt: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlNotebookCreateInput = {
  id?: Maybe<Scalars['String']>;
  name: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  readonly?: Maybe<Scalars['Boolean']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  stream: GqlStreamCreateNestedOneWithoutNotebooksInput;
  owner: GqlUserCreateNestedOneWithoutNotebooksInput;
};

export type GqlNotebookCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  name: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  readonly?: Maybe<Scalars['Boolean']>;
  listed?: Maybe<Scalars['Boolean']>;
  streamId: Scalars['String'];
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  ownerId: Scalars['String'];
};

export type GqlNotebookCreateManyOwnerInput = {
  id?: Maybe<Scalars['String']>;
  name: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  readonly?: Maybe<Scalars['Boolean']>;
  listed?: Maybe<Scalars['Boolean']>;
  streamId: Scalars['String'];
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
};

export type GqlNotebookCreateManyOwnerInputEnvelope = {
  data: Array<GqlNotebookCreateManyOwnerInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlNotebookCreateManyStreamInput = {
  id?: Maybe<Scalars['String']>;
  name: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  readonly?: Maybe<Scalars['Boolean']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  ownerId: Scalars['String'];
};

export type GqlNotebookCreateManyStreamInputEnvelope = {
  data: Array<GqlNotebookCreateManyStreamInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlNotebookCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlNotebookCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlNotebookCreateOrConnectWithoutOwnerInput>>;
  createMany?: Maybe<GqlNotebookCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlNotebookWhereUniqueInput>>;
};

export type GqlNotebookCreateNestedManyWithoutStreamInput = {
  create?: Maybe<Array<GqlNotebookCreateWithoutStreamInput>>;
  connectOrCreate?: Maybe<Array<GqlNotebookCreateOrConnectWithoutStreamInput>>;
  createMany?: Maybe<GqlNotebookCreateManyStreamInputEnvelope>;
  connect?: Maybe<Array<GqlNotebookWhereUniqueInput>>;
};

export type GqlNotebookCreateOrConnectWithoutOwnerInput = {
  where: GqlNotebookWhereUniqueInput;
  create: GqlNotebookCreateWithoutOwnerInput;
};

export type GqlNotebookCreateOrConnectWithoutStreamInput = {
  where: GqlNotebookWhereUniqueInput;
  create: GqlNotebookCreateWithoutStreamInput;
};

export type GqlNotebookCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  name: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  readonly?: Maybe<Scalars['Boolean']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  stream: GqlStreamCreateNestedOneWithoutNotebooksInput;
};

export type GqlNotebookCreateWithoutStreamInput = {
  id?: Maybe<Scalars['String']>;
  name: Scalars['String'];
  description?: Maybe<Scalars['String']>;
  readonly?: Maybe<Scalars['Boolean']>;
  listed?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  updatedAt?: Maybe<Scalars['DateTime']>;
  owner: GqlUserCreateNestedOneWithoutNotebooksInput;
};

export type GqlNotebookGroupBy = {
  __typename?: 'NotebookGroupBy';
  id: FieldWrapper<Scalars['String']>;
  name: FieldWrapper<Scalars['String']>;
  description: FieldWrapper<Scalars['String']>;
  readonly: FieldWrapper<Scalars['Boolean']>;
  listed: FieldWrapper<Scalars['Boolean']>;
  streamId: FieldWrapper<Scalars['String']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  updatedAt: FieldWrapper<Scalars['DateTime']>;
  ownerId: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlNotebookCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlNotebookMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlNotebookMaxAggregate>>;
};

export type GqlNotebookListRelationFilter = {
  every?: Maybe<GqlNotebookWhereInput>;
  some?: Maybe<GqlNotebookWhereInput>;
  none?: Maybe<GqlNotebookWhereInput>;
};

export type GqlNotebookMaxAggregate = {
  __typename?: 'NotebookMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  name?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  readonly?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  listed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  streamId?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlNotebookMinAggregate = {
  __typename?: 'NotebookMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  name?: Maybe<FieldWrapper<Scalars['String']>>;
  description?: Maybe<FieldWrapper<Scalars['String']>>;
  readonly?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  listed?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  streamId?: Maybe<FieldWrapper<Scalars['String']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  updatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlNotebookNameOwnerIdCompoundUniqueInput = {
  name: Scalars['String'];
  ownerId: Scalars['String'];
};

export type GqlNotebookOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  name?: Maybe<GqlSortOrder>;
  description?: Maybe<GqlSortOrder>;
  readonly?: Maybe<GqlSortOrder>;
  listed?: Maybe<GqlSortOrder>;
  streamId?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  updatedAt?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
};

export enum GqlNotebookScalarFieldEnum {
  Id = 'id',
  Name = 'name',
  Description = 'description',
  Readonly = 'readonly',
  Listed = 'listed',
  StreamId = 'streamId',
  CreatedAt = 'createdAt',
  UpdatedAt = 'updatedAt',
  OwnerId = 'ownerId'
}

export type GqlNotebookScalarWhereInput = {
  AND?: Maybe<Array<GqlNotebookScalarWhereInput>>;
  OR?: Maybe<Array<GqlNotebookScalarWhereInput>>;
  NOT?: Maybe<Array<GqlNotebookScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  name?: Maybe<GqlStringFilter>;
  description?: Maybe<GqlStringFilter>;
  readonly?: Maybe<GqlBoolFilter>;
  listed?: Maybe<GqlBoolFilter>;
  streamId?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  ownerId?: Maybe<GqlStringFilter>;
};

export type GqlNotebookScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlNotebookScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlNotebookScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlNotebookScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  name?: Maybe<GqlStringWithAggregatesFilter>;
  description?: Maybe<GqlStringWithAggregatesFilter>;
  readonly?: Maybe<GqlBoolWithAggregatesFilter>;
  listed?: Maybe<GqlBoolWithAggregatesFilter>;
  streamId?: Maybe<GqlStringWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  updatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlNotebookUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
  readonly?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutNotebooksInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutNotebooksInput>;
};

export type GqlNotebookUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
  readonly?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
};

export type GqlNotebookUpdateManyWithWhereWithoutOwnerInput = {
  where: GqlNotebookScalarWhereInput;
  data: GqlNotebookUpdateManyMutationInput;
};

export type GqlNotebookUpdateManyWithWhereWithoutStreamInput = {
  where: GqlNotebookScalarWhereInput;
  data: GqlNotebookUpdateManyMutationInput;
};

export type GqlNotebookUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlNotebookCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlNotebookCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<GqlNotebookUpsertWithWhereUniqueWithoutOwnerInput>>;
  createMany?: Maybe<GqlNotebookCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlNotebookWhereUniqueInput>>;
  set?: Maybe<Array<GqlNotebookWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlNotebookWhereUniqueInput>>;
  delete?: Maybe<Array<GqlNotebookWhereUniqueInput>>;
  update?: Maybe<Array<GqlNotebookUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<GqlNotebookUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<GqlNotebookScalarWhereInput>>;
};

export type GqlNotebookUpdateManyWithoutStreamInput = {
  create?: Maybe<Array<GqlNotebookCreateWithoutStreamInput>>;
  connectOrCreate?: Maybe<Array<GqlNotebookCreateOrConnectWithoutStreamInput>>;
  upsert?: Maybe<Array<GqlNotebookUpsertWithWhereUniqueWithoutStreamInput>>;
  createMany?: Maybe<GqlNotebookCreateManyStreamInputEnvelope>;
  connect?: Maybe<Array<GqlNotebookWhereUniqueInput>>;
  set?: Maybe<Array<GqlNotebookWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlNotebookWhereUniqueInput>>;
  delete?: Maybe<Array<GqlNotebookWhereUniqueInput>>;
  update?: Maybe<Array<GqlNotebookUpdateWithWhereUniqueWithoutStreamInput>>;
  updateMany?: Maybe<Array<GqlNotebookUpdateManyWithWhereWithoutStreamInput>>;
  deleteMany?: Maybe<Array<GqlNotebookScalarWhereInput>>;
};

export type GqlNotebookUpdateWithWhereUniqueWithoutOwnerInput = {
  where: GqlNotebookWhereUniqueInput;
  data: GqlNotebookUpdateWithoutOwnerInput;
};

export type GqlNotebookUpdateWithWhereUniqueWithoutStreamInput = {
  where: GqlNotebookWhereUniqueInput;
  data: GqlNotebookUpdateWithoutStreamInput;
};

export type GqlNotebookUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
  readonly?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  stream?: Maybe<GqlStreamUpdateOneRequiredWithoutNotebooksInput>;
};

export type GqlNotebookUpdateWithoutStreamInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  description?: Maybe<GqlStringFieldUpdateOperationsInput>;
  readonly?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  listed?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  updatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutNotebooksInput>;
};

export type GqlNotebookUpsertWithWhereUniqueWithoutOwnerInput = {
  where: GqlNotebookWhereUniqueInput;
  update: GqlNotebookUpdateWithoutOwnerInput;
  create: GqlNotebookCreateWithoutOwnerInput;
};

export type GqlNotebookUpsertWithWhereUniqueWithoutStreamInput = {
  where: GqlNotebookWhereUniqueInput;
  update: GqlNotebookUpdateWithoutStreamInput;
  create: GqlNotebookCreateWithoutStreamInput;
};

export type GqlNotebookWhereInput = {
  AND?: Maybe<Array<GqlNotebookWhereInput>>;
  OR?: Maybe<Array<GqlNotebookWhereInput>>;
  NOT?: Maybe<Array<GqlNotebookWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  name?: Maybe<GqlStringFilter>;
  description?: Maybe<GqlStringFilter>;
  readonly?: Maybe<GqlBoolFilter>;
  listed?: Maybe<GqlBoolFilter>;
  stream?: Maybe<GqlStreamRelationFilter>;
  streamId?: Maybe<GqlStringFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  updatedAt?: Maybe<GqlDateTimeFilter>;
  owner?: Maybe<GqlUserRelationFilter>;
  ownerId?: Maybe<GqlStringFilter>;
};

export type GqlNotebookWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
  name_ownerId?: Maybe<GqlNotebookNameOwnerIdCompoundUniqueInput>;
};

export type GqlNullableBoolFieldUpdateOperationsInput = {
  set?: Maybe<Scalars['Boolean']>;
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

export type GqlPlugin = {
  __typename?: 'Plugin';
  id: FieldWrapper<Scalars['String']>;
  source_url: FieldWrapper<Scalars['String']>;
  type: FieldWrapper<Scalars['String']>;
  user_params: FieldWrapper<Scalars['JSON']>;
  ownerId: FieldWrapper<Scalars['String']>;
  source?: Maybe<FieldWrapper<Scalars['JSON']>>;
  source_sha1?: Maybe<FieldWrapper<Scalars['String']>>;
  lastUpdatedAt: FieldWrapper<Scalars['DateTime']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  owner: FieldWrapper<GqlUser>;
  feeds: Array<FieldWrapper<GqlFeed>>;
};


export type GqlPluginFeedsArgs = {
  where?: Maybe<GqlFeedWhereInput>;
  orderBy?: Maybe<Array<GqlFeedOrderByInput>>;
  cursor?: Maybe<GqlFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlFeedScalarFieldEnum>>;
};

export type GqlPluginCountAggregate = {
  __typename?: 'PluginCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  source_url: FieldWrapper<Scalars['Int']>;
  type: FieldWrapper<Scalars['Int']>;
  user_params: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  source: FieldWrapper<Scalars['Int']>;
  source_sha1: FieldWrapper<Scalars['Int']>;
  lastUpdatedAt: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlPluginCreateInput = {
  id?: Maybe<Scalars['String']>;
  source_url: Scalars['String'];
  type: Scalars['String'];
  user_params: Scalars['JSON'];
  source?: Maybe<Scalars['JSON']>;
  source_sha1?: Maybe<Scalars['String']>;
  lastUpdatedAt: Scalars['DateTime'];
  createdAt?: Maybe<Scalars['DateTime']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutPluginsInput>;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutManaged_By_PluginInput>;
};

export type GqlPluginCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  source_url: Scalars['String'];
  type: Scalars['String'];
  user_params: Scalars['JSON'];
  ownerId?: Maybe<Scalars['String']>;
  source?: Maybe<Scalars['JSON']>;
  source_sha1?: Maybe<Scalars['String']>;
  lastUpdatedAt: Scalars['DateTime'];
  createdAt?: Maybe<Scalars['DateTime']>;
};

export type GqlPluginCreateManyOwnerInput = {
  id?: Maybe<Scalars['String']>;
  source_url: Scalars['String'];
  type: Scalars['String'];
  user_params: Scalars['JSON'];
  source?: Maybe<Scalars['JSON']>;
  source_sha1?: Maybe<Scalars['String']>;
  lastUpdatedAt: Scalars['DateTime'];
  createdAt?: Maybe<Scalars['DateTime']>;
};

export type GqlPluginCreateManyOwnerInputEnvelope = {
  data: Array<GqlPluginCreateManyOwnerInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlPluginCreateNestedManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlPluginCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlPluginCreateOrConnectWithoutOwnerInput>>;
  createMany?: Maybe<GqlPluginCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlPluginWhereUniqueInput>>;
};

export type GqlPluginCreateNestedOneWithoutFeedsInput = {
  create?: Maybe<GqlPluginCreateWithoutFeedsInput>;
  connectOrCreate?: Maybe<GqlPluginCreateOrConnectWithoutFeedsInput>;
  connect?: Maybe<GqlPluginWhereUniqueInput>;
};

export type GqlPluginCreateOrConnectWithoutFeedsInput = {
  where: GqlPluginWhereUniqueInput;
  create: GqlPluginCreateWithoutFeedsInput;
};

export type GqlPluginCreateOrConnectWithoutOwnerInput = {
  where: GqlPluginWhereUniqueInput;
  create: GqlPluginCreateWithoutOwnerInput;
};

export type GqlPluginCreateWithoutFeedsInput = {
  id?: Maybe<Scalars['String']>;
  source_url: Scalars['String'];
  type: Scalars['String'];
  user_params: Scalars['JSON'];
  source?: Maybe<Scalars['JSON']>;
  source_sha1?: Maybe<Scalars['String']>;
  lastUpdatedAt: Scalars['DateTime'];
  createdAt?: Maybe<Scalars['DateTime']>;
  owner?: Maybe<GqlUserCreateNestedOneWithoutPluginsInput>;
};

export type GqlPluginCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  source_url: Scalars['String'];
  type: Scalars['String'];
  user_params: Scalars['JSON'];
  source?: Maybe<Scalars['JSON']>;
  source_sha1?: Maybe<Scalars['String']>;
  lastUpdatedAt: Scalars['DateTime'];
  createdAt?: Maybe<Scalars['DateTime']>;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutManaged_By_PluginInput>;
};

export type GqlPluginGroupBy = {
  __typename?: 'PluginGroupBy';
  id: FieldWrapper<Scalars['String']>;
  source_url: FieldWrapper<Scalars['String']>;
  type: FieldWrapper<Scalars['String']>;
  user_params: FieldWrapper<Scalars['JSON']>;
  ownerId: FieldWrapper<Scalars['String']>;
  source?: Maybe<FieldWrapper<Scalars['JSON']>>;
  source_sha1?: Maybe<FieldWrapper<Scalars['String']>>;
  lastUpdatedAt: FieldWrapper<Scalars['DateTime']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  _count?: Maybe<FieldWrapper<GqlPluginCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlPluginMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlPluginMaxAggregate>>;
};

export type GqlPluginListRelationFilter = {
  every?: Maybe<GqlPluginWhereInput>;
  some?: Maybe<GqlPluginWhereInput>;
  none?: Maybe<GqlPluginWhereInput>;
};

export type GqlPluginMaxAggregate = {
  __typename?: 'PluginMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  source_url?: Maybe<FieldWrapper<Scalars['String']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  source_sha1?: Maybe<FieldWrapper<Scalars['String']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlPluginMinAggregate = {
  __typename?: 'PluginMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  source_url?: Maybe<FieldWrapper<Scalars['String']>>;
  type?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  source_sha1?: Maybe<FieldWrapper<Scalars['String']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
};

export type GqlPluginOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  source_url?: Maybe<GqlSortOrder>;
  type?: Maybe<GqlSortOrder>;
  user_params?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  source?: Maybe<GqlSortOrder>;
  source_sha1?: Maybe<GqlSortOrder>;
  lastUpdatedAt?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
};

export type GqlPluginRelationFilter = {
  is?: Maybe<GqlPluginWhereInput>;
  isNot?: Maybe<GqlPluginWhereInput>;
};

export enum GqlPluginScalarFieldEnum {
  Id = 'id',
  SourceUrl = 'source_url',
  Type = 'type',
  UserParams = 'user_params',
  OwnerId = 'ownerId',
  Source = 'source',
  SourceSha1 = 'source_sha1',
  LastUpdatedAt = 'lastUpdatedAt',
  CreatedAt = 'createdAt'
}

export type GqlPluginScalarWhereInput = {
  AND?: Maybe<Array<GqlPluginScalarWhereInput>>;
  OR?: Maybe<Array<GqlPluginScalarWhereInput>>;
  NOT?: Maybe<Array<GqlPluginScalarWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  source_url?: Maybe<GqlStringFilter>;
  type?: Maybe<GqlStringFilter>;
  user_params?: Maybe<GqlJsonFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  source?: Maybe<GqlJsonNullableFilter>;
  source_sha1?: Maybe<GqlStringNullableFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
};

export type GqlPluginScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlPluginScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlPluginScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlPluginScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  source_url?: Maybe<GqlStringWithAggregatesFilter>;
  type?: Maybe<GqlStringWithAggregatesFilter>;
  user_params?: Maybe<GqlJsonWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
  source?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  source_sha1?: Maybe<GqlStringNullableWithAggregatesFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
};

export type GqlPluginUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  source_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  user_params?: Maybe<Scalars['JSON']>;
  source?: Maybe<Scalars['JSON']>;
  source_sha1?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutPluginsInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutManaged_By_PluginInput>;
};

export type GqlPluginUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  source_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  user_params?: Maybe<Scalars['JSON']>;
  source?: Maybe<Scalars['JSON']>;
  source_sha1?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
};

export type GqlPluginUpdateManyWithWhereWithoutOwnerInput = {
  where: GqlPluginScalarWhereInput;
  data: GqlPluginUpdateManyMutationInput;
};

export type GqlPluginUpdateManyWithoutOwnerInput = {
  create?: Maybe<Array<GqlPluginCreateWithoutOwnerInput>>;
  connectOrCreate?: Maybe<Array<GqlPluginCreateOrConnectWithoutOwnerInput>>;
  upsert?: Maybe<Array<GqlPluginUpsertWithWhereUniqueWithoutOwnerInput>>;
  createMany?: Maybe<GqlPluginCreateManyOwnerInputEnvelope>;
  connect?: Maybe<Array<GqlPluginWhereUniqueInput>>;
  set?: Maybe<Array<GqlPluginWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlPluginWhereUniqueInput>>;
  delete?: Maybe<Array<GqlPluginWhereUniqueInput>>;
  update?: Maybe<Array<GqlPluginUpdateWithWhereUniqueWithoutOwnerInput>>;
  updateMany?: Maybe<Array<GqlPluginUpdateManyWithWhereWithoutOwnerInput>>;
  deleteMany?: Maybe<Array<GqlPluginScalarWhereInput>>;
};

export type GqlPluginUpdateOneWithoutFeedsInput = {
  create?: Maybe<GqlPluginCreateWithoutFeedsInput>;
  connectOrCreate?: Maybe<GqlPluginCreateOrConnectWithoutFeedsInput>;
  upsert?: Maybe<GqlPluginUpsertWithoutFeedsInput>;
  connect?: Maybe<GqlPluginWhereUniqueInput>;
  disconnect?: Maybe<Scalars['Boolean']>;
  delete?: Maybe<Scalars['Boolean']>;
  update?: Maybe<GqlPluginUpdateWithoutFeedsInput>;
};

export type GqlPluginUpdateWithWhereUniqueWithoutOwnerInput = {
  where: GqlPluginWhereUniqueInput;
  data: GqlPluginUpdateWithoutOwnerInput;
};

export type GqlPluginUpdateWithoutFeedsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  source_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  user_params?: Maybe<Scalars['JSON']>;
  source?: Maybe<Scalars['JSON']>;
  source_sha1?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutPluginsInput>;
};

export type GqlPluginUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  source_url?: Maybe<GqlStringFieldUpdateOperationsInput>;
  type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  user_params?: Maybe<Scalars['JSON']>;
  source?: Maybe<Scalars['JSON']>;
  source_sha1?: Maybe<GqlNullableStringFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutManaged_By_PluginInput>;
};

export type GqlPluginUpsertWithWhereUniqueWithoutOwnerInput = {
  where: GqlPluginWhereUniqueInput;
  update: GqlPluginUpdateWithoutOwnerInput;
  create: GqlPluginCreateWithoutOwnerInput;
};

export type GqlPluginUpsertWithoutFeedsInput = {
  update: GqlPluginUpdateWithoutFeedsInput;
  create: GqlPluginCreateWithoutFeedsInput;
};

export type GqlPluginWhereInput = {
  AND?: Maybe<Array<GqlPluginWhereInput>>;
  OR?: Maybe<Array<GqlPluginWhereInput>>;
  NOT?: Maybe<Array<GqlPluginWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  source_url?: Maybe<GqlStringFilter>;
  type?: Maybe<GqlStringFilter>;
  user_params?: Maybe<GqlJsonFilter>;
  owner?: Maybe<GqlUserRelationFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  source?: Maybe<GqlJsonNullableFilter>;
  source_sha1?: Maybe<GqlStringNullableFilter>;
  feeds?: Maybe<GqlFeedListRelationFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
};

export type GqlPluginWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlProfileSettings = {
  __typename?: 'ProfileSettings';
  id: FieldWrapper<Scalars['String']>;
  useFulltext: FieldWrapper<Scalars['Boolean']>;
  useBetterRead: FieldWrapper<Scalars['Boolean']>;
  showNativeTags: FieldWrapper<Scalars['Boolean']>;
  showContentTags: FieldWrapper<Scalars['Boolean']>;
  queryEngines?: Maybe<FieldWrapper<Scalars['JSON']>>;
  user?: Maybe<FieldWrapper<GqlUser>>;
};

export type GqlProfileSettingsCountAggregate = {
  __typename?: 'ProfileSettingsCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  useFulltext: FieldWrapper<Scalars['Int']>;
  useBetterRead: FieldWrapper<Scalars['Int']>;
  showNativeTags: FieldWrapper<Scalars['Int']>;
  showContentTags: FieldWrapper<Scalars['Int']>;
  queryEngines: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlProfileSettingsCreateInput = {
  id?: Maybe<Scalars['String']>;
  useFulltext?: Maybe<Scalars['Boolean']>;
  useBetterRead?: Maybe<Scalars['Boolean']>;
  showNativeTags?: Maybe<Scalars['Boolean']>;
  showContentTags?: Maybe<Scalars['Boolean']>;
  queryEngines?: Maybe<Scalars['JSON']>;
  user?: Maybe<GqlUserCreateNestedOneWithoutSettingsInput>;
};

export type GqlProfileSettingsCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  useFulltext?: Maybe<Scalars['Boolean']>;
  useBetterRead?: Maybe<Scalars['Boolean']>;
  showNativeTags?: Maybe<Scalars['Boolean']>;
  showContentTags?: Maybe<Scalars['Boolean']>;
  queryEngines?: Maybe<Scalars['JSON']>;
};

export type GqlProfileSettingsCreateNestedOneWithoutUserInput = {
  create?: Maybe<GqlProfileSettingsCreateWithoutUserInput>;
  connectOrCreate?: Maybe<GqlProfileSettingsCreateOrConnectWithoutUserInput>;
  connect?: Maybe<GqlProfileSettingsWhereUniqueInput>;
};

export type GqlProfileSettingsCreateOrConnectWithoutUserInput = {
  where: GqlProfileSettingsWhereUniqueInput;
  create: GqlProfileSettingsCreateWithoutUserInput;
};

export type GqlProfileSettingsCreateWithoutUserInput = {
  id?: Maybe<Scalars['String']>;
  useFulltext?: Maybe<Scalars['Boolean']>;
  useBetterRead?: Maybe<Scalars['Boolean']>;
  showNativeTags?: Maybe<Scalars['Boolean']>;
  showContentTags?: Maybe<Scalars['Boolean']>;
  queryEngines?: Maybe<Scalars['JSON']>;
};

export type GqlProfileSettingsGroupBy = {
  __typename?: 'ProfileSettingsGroupBy';
  id: FieldWrapper<Scalars['String']>;
  useFulltext: FieldWrapper<Scalars['Boolean']>;
  useBetterRead: FieldWrapper<Scalars['Boolean']>;
  showNativeTags: FieldWrapper<Scalars['Boolean']>;
  showContentTags: FieldWrapper<Scalars['Boolean']>;
  queryEngines?: Maybe<FieldWrapper<Scalars['JSON']>>;
  _count?: Maybe<FieldWrapper<GqlProfileSettingsCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlProfileSettingsMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlProfileSettingsMaxAggregate>>;
};

export type GqlProfileSettingsMaxAggregate = {
  __typename?: 'ProfileSettingsMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  useFulltext?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  useBetterRead?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  showNativeTags?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  showContentTags?: Maybe<FieldWrapper<Scalars['Boolean']>>;
};

export type GqlProfileSettingsMinAggregate = {
  __typename?: 'ProfileSettingsMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  useFulltext?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  useBetterRead?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  showNativeTags?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  showContentTags?: Maybe<FieldWrapper<Scalars['Boolean']>>;
};

export type GqlProfileSettingsOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  useFulltext?: Maybe<GqlSortOrder>;
  useBetterRead?: Maybe<GqlSortOrder>;
  showNativeTags?: Maybe<GqlSortOrder>;
  showContentTags?: Maybe<GqlSortOrder>;
  queryEngines?: Maybe<GqlSortOrder>;
};

export type GqlProfileSettingsRelationFilter = {
  is?: Maybe<GqlProfileSettingsWhereInput>;
  isNot?: Maybe<GqlProfileSettingsWhereInput>;
};

export enum GqlProfileSettingsScalarFieldEnum {
  Id = 'id',
  UseFulltext = 'useFulltext',
  UseBetterRead = 'useBetterRead',
  ShowNativeTags = 'showNativeTags',
  ShowContentTags = 'showContentTags',
  QueryEngines = 'queryEngines'
}

export type GqlProfileSettingsScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlProfileSettingsScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlProfileSettingsScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlProfileSettingsScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  useFulltext?: Maybe<GqlBoolWithAggregatesFilter>;
  useBetterRead?: Maybe<GqlBoolWithAggregatesFilter>;
  showNativeTags?: Maybe<GqlBoolWithAggregatesFilter>;
  showContentTags?: Maybe<GqlBoolWithAggregatesFilter>;
  queryEngines?: Maybe<GqlJsonNullableWithAggregatesFilter>;
};

export type GqlProfileSettingsUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  useFulltext?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  useBetterRead?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  showNativeTags?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  showContentTags?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  queryEngines?: Maybe<Scalars['JSON']>;
  user?: Maybe<GqlUserUpdateOneWithoutSettingsInput>;
};

export type GqlProfileSettingsUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  useFulltext?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  useBetterRead?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  showNativeTags?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  showContentTags?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  queryEngines?: Maybe<Scalars['JSON']>;
};

export type GqlProfileSettingsUpdateOneRequiredWithoutUserInput = {
  create?: Maybe<GqlProfileSettingsCreateWithoutUserInput>;
  connectOrCreate?: Maybe<GqlProfileSettingsCreateOrConnectWithoutUserInput>;
  upsert?: Maybe<GqlProfileSettingsUpsertWithoutUserInput>;
  connect?: Maybe<GqlProfileSettingsWhereUniqueInput>;
  update?: Maybe<GqlProfileSettingsUpdateWithoutUserInput>;
};

export type GqlProfileSettingsUpdateWithoutUserInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  useFulltext?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  useBetterRead?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  showNativeTags?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  showContentTags?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  queryEngines?: Maybe<Scalars['JSON']>;
};

export type GqlProfileSettingsUpsertWithoutUserInput = {
  update: GqlProfileSettingsUpdateWithoutUserInput;
  create: GqlProfileSettingsCreateWithoutUserInput;
};

export type GqlProfileSettingsWhereInput = {
  AND?: Maybe<Array<GqlProfileSettingsWhereInput>>;
  OR?: Maybe<Array<GqlProfileSettingsWhereInput>>;
  NOT?: Maybe<Array<GqlProfileSettingsWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  useFulltext?: Maybe<GqlBoolFilter>;
  useBetterRead?: Maybe<GqlBoolFilter>;
  showNativeTags?: Maybe<GqlBoolFilter>;
  showContentTags?: Maybe<GqlBoolFilter>;
  queryEngines?: Maybe<GqlJsonNullableFilter>;
  user?: Maybe<GqlUserRelationFilter>;
};

export type GqlProfileSettingsWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlQuery = {
  __typename?: 'Query';
  article?: Maybe<FieldWrapper<GqlArticle>>;
  findFirstArticle?: Maybe<FieldWrapper<GqlArticle>>;
  articles: Array<FieldWrapper<GqlArticle>>;
  aggregateArticle: FieldWrapper<GqlAggregateArticle>;
  groupByArticle: Array<FieldWrapper<GqlArticleGroupBy>>;
  articleExporter?: Maybe<FieldWrapper<GqlArticleExporter>>;
  findFirstArticleExporter?: Maybe<FieldWrapper<GqlArticleExporter>>;
  articleExporters: Array<FieldWrapper<GqlArticleExporter>>;
  aggregateArticleExporter: FieldWrapper<GqlAggregateArticleExporter>;
  groupByArticleExporter: Array<FieldWrapper<GqlArticleExporterGroupBy>>;
  articleExporterTarget?: Maybe<FieldWrapper<GqlArticleExporterTarget>>;
  findFirstArticleExporterTarget?: Maybe<FieldWrapper<GqlArticleExporterTarget>>;
  articleExporterTargets: Array<FieldWrapper<GqlArticleExporterTarget>>;
  aggregateArticleExporterTarget: FieldWrapper<GqlAggregateArticleExporterTarget>;
  groupByArticleExporterTarget: Array<FieldWrapper<GqlArticleExporterTargetGroupBy>>;
  articlePostProcessor?: Maybe<FieldWrapper<GqlArticlePostProcessor>>;
  findFirstArticlePostProcessor?: Maybe<FieldWrapper<GqlArticlePostProcessor>>;
  articlePostProcessors: Array<FieldWrapper<GqlArticlePostProcessor>>;
  aggregateArticlePostProcessor: FieldWrapper<GqlAggregateArticlePostProcessor>;
  groupByArticlePostProcessor: Array<FieldWrapper<GqlArticlePostProcessorGroupBy>>;
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
  eventHook?: Maybe<FieldWrapper<GqlEventHook>>;
  findFirstEventHook?: Maybe<FieldWrapper<GqlEventHook>>;
  eventHooks: Array<FieldWrapper<GqlEventHook>>;
  aggregateEventHook: FieldWrapper<GqlAggregateEventHook>;
  groupByEventHook: Array<FieldWrapper<GqlEventHookGroupBy>>;
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
  noFollowUrl?: Maybe<FieldWrapper<GqlNoFollowUrl>>;
  findFirstNoFollowUrl?: Maybe<FieldWrapper<GqlNoFollowUrl>>;
  noFollowUrls: Array<FieldWrapper<GqlNoFollowUrl>>;
  aggregateNoFollowUrl: FieldWrapper<GqlAggregateNoFollowUrl>;
  groupByNoFollowUrl: Array<FieldWrapper<GqlNoFollowUrlGroupBy>>;
  notebook?: Maybe<FieldWrapper<GqlNotebook>>;
  findFirstNotebook?: Maybe<FieldWrapper<GqlNotebook>>;
  notebooks: Array<FieldWrapper<GqlNotebook>>;
  aggregateNotebook: FieldWrapper<GqlAggregateNotebook>;
  groupByNotebook: Array<FieldWrapper<GqlNotebookGroupBy>>;
  plugin?: Maybe<FieldWrapper<GqlPlugin>>;
  findFirstPlugin?: Maybe<FieldWrapper<GqlPlugin>>;
  plugins: Array<FieldWrapper<GqlPlugin>>;
  aggregatePlugin: FieldWrapper<GqlAggregatePlugin>;
  groupByPlugin: Array<FieldWrapper<GqlPluginGroupBy>>;
  findUniqueProfileSettings?: Maybe<FieldWrapper<GqlProfileSettings>>;
  findFirstProfileSettings?: Maybe<FieldWrapper<GqlProfileSettings>>;
  findManyProfileSettings: Array<FieldWrapper<GqlProfileSettings>>;
  aggregateProfileSettings: FieldWrapper<GqlAggregateProfileSettings>;
  groupByProfileSettings: Array<FieldWrapper<GqlProfileSettingsGroupBy>>;
  referencedArticleRef?: Maybe<FieldWrapper<GqlReferencedArticleRef>>;
  findFirstReferencedArticleRef?: Maybe<FieldWrapper<GqlReferencedArticleRef>>;
  referencedArticleRefs: Array<FieldWrapper<GqlReferencedArticleRef>>;
  aggregateReferencedArticleRef: FieldWrapper<GqlAggregateReferencedArticleRef>;
  groupByReferencedArticleRef: Array<FieldWrapper<GqlReferencedArticleRefGroupBy>>;
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
  discoverFeedsByUrl: FieldWrapper<GqlDiscoveredFeeds>;
  articlesForFeedUrl: Array<FieldWrapper<GqlArticle>>;
  metadataForNativeFeedByUrl: FieldWrapper<GqlFeed>;
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


export type GqlQueryArticleExporterArgs = {
  where: GqlArticleExporterWhereUniqueInput;
};


export type GqlQueryFindFirstArticleExporterArgs = {
  where?: Maybe<GqlArticleExporterWhereInput>;
  orderBy?: Maybe<Array<GqlArticleExporterOrderByInput>>;
  cursor?: Maybe<GqlArticleExporterWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleExporterScalarFieldEnum>>;
};


export type GqlQueryArticleExportersArgs = {
  where?: Maybe<GqlArticleExporterWhereInput>;
  orderBy?: Maybe<Array<GqlArticleExporterOrderByInput>>;
  cursor?: Maybe<GqlArticleExporterWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleExporterScalarFieldEnum>>;
};


export type GqlQueryAggregateArticleExporterArgs = {
  where?: Maybe<GqlArticleExporterWhereInput>;
  orderBy?: Maybe<Array<GqlArticleExporterOrderByInput>>;
  cursor?: Maybe<GqlArticleExporterWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByArticleExporterArgs = {
  where?: Maybe<GqlArticleExporterWhereInput>;
  orderBy?: Maybe<Array<GqlArticleExporterOrderByInput>>;
  by: Array<GqlArticleExporterScalarFieldEnum>;
  having?: Maybe<GqlArticleExporterScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryArticleExporterTargetArgs = {
  where: GqlArticleExporterTargetWhereUniqueInput;
};


export type GqlQueryFindFirstArticleExporterTargetArgs = {
  where?: Maybe<GqlArticleExporterTargetWhereInput>;
  orderBy?: Maybe<Array<GqlArticleExporterTargetOrderByInput>>;
  cursor?: Maybe<GqlArticleExporterTargetWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleExporterTargetScalarFieldEnum>>;
};


export type GqlQueryArticleExporterTargetsArgs = {
  where?: Maybe<GqlArticleExporterTargetWhereInput>;
  orderBy?: Maybe<Array<GqlArticleExporterTargetOrderByInput>>;
  cursor?: Maybe<GqlArticleExporterTargetWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleExporterTargetScalarFieldEnum>>;
};


export type GqlQueryAggregateArticleExporterTargetArgs = {
  where?: Maybe<GqlArticleExporterTargetWhereInput>;
  orderBy?: Maybe<Array<GqlArticleExporterTargetOrderByInput>>;
  cursor?: Maybe<GqlArticleExporterTargetWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByArticleExporterTargetArgs = {
  where?: Maybe<GqlArticleExporterTargetWhereInput>;
  orderBy?: Maybe<Array<GqlArticleExporterTargetOrderByInput>>;
  by: Array<GqlArticleExporterTargetScalarFieldEnum>;
  having?: Maybe<GqlArticleExporterTargetScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryArticlePostProcessorArgs = {
  where: GqlArticlePostProcessorWhereUniqueInput;
};


export type GqlQueryFindFirstArticlePostProcessorArgs = {
  where?: Maybe<GqlArticlePostProcessorWhereInput>;
  orderBy?: Maybe<Array<GqlArticlePostProcessorOrderByInput>>;
  cursor?: Maybe<GqlArticlePostProcessorWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticlePostProcessorScalarFieldEnum>>;
};


export type GqlQueryArticlePostProcessorsArgs = {
  where?: Maybe<GqlArticlePostProcessorWhereInput>;
  orderBy?: Maybe<Array<GqlArticlePostProcessorOrderByInput>>;
  cursor?: Maybe<GqlArticlePostProcessorWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticlePostProcessorScalarFieldEnum>>;
};


export type GqlQueryAggregateArticlePostProcessorArgs = {
  where?: Maybe<GqlArticlePostProcessorWhereInput>;
  orderBy?: Maybe<Array<GqlArticlePostProcessorOrderByInput>>;
  cursor?: Maybe<GqlArticlePostProcessorWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByArticlePostProcessorArgs = {
  where?: Maybe<GqlArticlePostProcessorWhereInput>;
  orderBy?: Maybe<Array<GqlArticlePostProcessorOrderByInput>>;
  by: Array<GqlArticlePostProcessorScalarFieldEnum>;
  having?: Maybe<GqlArticlePostProcessorScalarWhereWithAggregatesInput>;
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


export type GqlQueryEventHookArgs = {
  where: GqlEventHookWhereUniqueInput;
};


export type GqlQueryFindFirstEventHookArgs = {
  where?: Maybe<GqlEventHookWhereInput>;
  orderBy?: Maybe<Array<GqlEventHookOrderByInput>>;
  cursor?: Maybe<GqlEventHookWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlEventHookScalarFieldEnum>>;
};


export type GqlQueryEventHooksArgs = {
  where?: Maybe<GqlEventHookWhereInput>;
  orderBy?: Maybe<Array<GqlEventHookOrderByInput>>;
  cursor?: Maybe<GqlEventHookWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlEventHookScalarFieldEnum>>;
};


export type GqlQueryAggregateEventHookArgs = {
  where?: Maybe<GqlEventHookWhereInput>;
  orderBy?: Maybe<Array<GqlEventHookOrderByInput>>;
  cursor?: Maybe<GqlEventHookWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByEventHookArgs = {
  where?: Maybe<GqlEventHookWhereInput>;
  orderBy?: Maybe<Array<GqlEventHookOrderByInput>>;
  by: Array<GqlEventHookScalarFieldEnum>;
  having?: Maybe<GqlEventHookScalarWhereWithAggregatesInput>;
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


export type GqlQueryNoFollowUrlArgs = {
  where: GqlNoFollowUrlWhereUniqueInput;
};


export type GqlQueryFindFirstNoFollowUrlArgs = {
  where?: Maybe<GqlNoFollowUrlWhereInput>;
  orderBy?: Maybe<Array<GqlNoFollowUrlOrderByInput>>;
  cursor?: Maybe<GqlNoFollowUrlWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlNoFollowUrlScalarFieldEnum>>;
};


export type GqlQueryNoFollowUrlsArgs = {
  where?: Maybe<GqlNoFollowUrlWhereInput>;
  orderBy?: Maybe<Array<GqlNoFollowUrlOrderByInput>>;
  cursor?: Maybe<GqlNoFollowUrlWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlNoFollowUrlScalarFieldEnum>>;
};


export type GqlQueryAggregateNoFollowUrlArgs = {
  where?: Maybe<GqlNoFollowUrlWhereInput>;
  orderBy?: Maybe<Array<GqlNoFollowUrlOrderByInput>>;
  cursor?: Maybe<GqlNoFollowUrlWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByNoFollowUrlArgs = {
  where?: Maybe<GqlNoFollowUrlWhereInput>;
  orderBy?: Maybe<Array<GqlNoFollowUrlOrderByInput>>;
  by: Array<GqlNoFollowUrlScalarFieldEnum>;
  having?: Maybe<GqlNoFollowUrlScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryNotebookArgs = {
  where: GqlNotebookWhereUniqueInput;
};


export type GqlQueryFindFirstNotebookArgs = {
  where?: Maybe<GqlNotebookWhereInput>;
  orderBy?: Maybe<Array<GqlNotebookOrderByInput>>;
  cursor?: Maybe<GqlNotebookWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlNotebookScalarFieldEnum>>;
};


export type GqlQueryNotebooksArgs = {
  where?: Maybe<GqlNotebookWhereInput>;
  orderBy?: Maybe<Array<GqlNotebookOrderByInput>>;
  cursor?: Maybe<GqlNotebookWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlNotebookScalarFieldEnum>>;
};


export type GqlQueryAggregateNotebookArgs = {
  where?: Maybe<GqlNotebookWhereInput>;
  orderBy?: Maybe<Array<GqlNotebookOrderByInput>>;
  cursor?: Maybe<GqlNotebookWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByNotebookArgs = {
  where?: Maybe<GqlNotebookWhereInput>;
  orderBy?: Maybe<Array<GqlNotebookOrderByInput>>;
  by: Array<GqlNotebookScalarFieldEnum>;
  having?: Maybe<GqlNotebookScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryPluginArgs = {
  where: GqlPluginWhereUniqueInput;
};


export type GqlQueryFindFirstPluginArgs = {
  where?: Maybe<GqlPluginWhereInput>;
  orderBy?: Maybe<Array<GqlPluginOrderByInput>>;
  cursor?: Maybe<GqlPluginWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlPluginScalarFieldEnum>>;
};


export type GqlQueryPluginsArgs = {
  where?: Maybe<GqlPluginWhereInput>;
  orderBy?: Maybe<Array<GqlPluginOrderByInput>>;
  cursor?: Maybe<GqlPluginWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlPluginScalarFieldEnum>>;
};


export type GqlQueryAggregatePluginArgs = {
  where?: Maybe<GqlPluginWhereInput>;
  orderBy?: Maybe<Array<GqlPluginOrderByInput>>;
  cursor?: Maybe<GqlPluginWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByPluginArgs = {
  where?: Maybe<GqlPluginWhereInput>;
  orderBy?: Maybe<Array<GqlPluginOrderByInput>>;
  by: Array<GqlPluginScalarFieldEnum>;
  having?: Maybe<GqlPluginScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryFindUniqueProfileSettingsArgs = {
  where: GqlProfileSettingsWhereUniqueInput;
};


export type GqlQueryFindFirstProfileSettingsArgs = {
  where?: Maybe<GqlProfileSettingsWhereInput>;
  orderBy?: Maybe<Array<GqlProfileSettingsOrderByInput>>;
  cursor?: Maybe<GqlProfileSettingsWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlProfileSettingsScalarFieldEnum>>;
};


export type GqlQueryFindManyProfileSettingsArgs = {
  where?: Maybe<GqlProfileSettingsWhereInput>;
  orderBy?: Maybe<Array<GqlProfileSettingsOrderByInput>>;
  cursor?: Maybe<GqlProfileSettingsWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlProfileSettingsScalarFieldEnum>>;
};


export type GqlQueryAggregateProfileSettingsArgs = {
  where?: Maybe<GqlProfileSettingsWhereInput>;
  orderBy?: Maybe<Array<GqlProfileSettingsOrderByInput>>;
  cursor?: Maybe<GqlProfileSettingsWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByProfileSettingsArgs = {
  where?: Maybe<GqlProfileSettingsWhereInput>;
  orderBy?: Maybe<Array<GqlProfileSettingsOrderByInput>>;
  by: Array<GqlProfileSettingsScalarFieldEnum>;
  having?: Maybe<GqlProfileSettingsScalarWhereWithAggregatesInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryReferencedArticleRefArgs = {
  where: GqlReferencedArticleRefWhereUniqueInput;
};


export type GqlQueryFindFirstReferencedArticleRefArgs = {
  where?: Maybe<GqlReferencedArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlReferencedArticleRefOrderByInput>>;
  cursor?: Maybe<GqlReferencedArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlReferencedArticleRefScalarFieldEnum>>;
};


export type GqlQueryReferencedArticleRefsArgs = {
  where?: Maybe<GqlReferencedArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlReferencedArticleRefOrderByInput>>;
  cursor?: Maybe<GqlReferencedArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlReferencedArticleRefScalarFieldEnum>>;
};


export type GqlQueryAggregateReferencedArticleRefArgs = {
  where?: Maybe<GqlReferencedArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlReferencedArticleRefOrderByInput>>;
  cursor?: Maybe<GqlReferencedArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
};


export type GqlQueryGroupByReferencedArticleRefArgs = {
  where?: Maybe<GqlReferencedArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlReferencedArticleRefOrderByInput>>;
  by: Array<GqlReferencedArticleRefScalarFieldEnum>;
  having?: Maybe<GqlReferencedArticleRefScalarWhereWithAggregatesInput>;
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


export type GqlQueryDiscoverFeedsByUrlArgs = {
  prerender?: Maybe<Scalars['Boolean']>;
  url: Scalars['String'];
};


export type GqlQueryArticlesForFeedUrlArgs = {
  feedUrl: Scalars['String'];
};


export type GqlQueryMetadataForNativeFeedByUrlArgs = {
  feedUrl: Scalars['String'];
};

export enum GqlQueryMode {
  Default = 'default',
  Insensitive = 'insensitive'
}

export type GqlReferencedArticleRef = {
  __typename?: 'ReferencedArticleRef';
  sourceId: FieldWrapper<Scalars['String']>;
  targetId: FieldWrapper<Scalars['String']>;
  reference_type: FieldWrapper<Scalars['String']>;
  source: FieldWrapper<GqlArticleRef>;
  target: FieldWrapper<GqlArticleRef>;
};

export type GqlReferencedArticleRefCountAggregate = {
  __typename?: 'ReferencedArticleRefCountAggregate';
  sourceId: FieldWrapper<Scalars['Int']>;
  targetId: FieldWrapper<Scalars['Int']>;
  reference_type: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlReferencedArticleRefCreateInput = {
  reference_type: Scalars['String'];
  source: GqlArticleRefCreateNestedOneWithoutRelatives_OutInput;
  target: GqlArticleRefCreateNestedOneWithoutRelatives_InInput;
};

export type GqlReferencedArticleRefCreateManyInput = {
  sourceId: Scalars['String'];
  targetId: Scalars['String'];
  reference_type: Scalars['String'];
};

export type GqlReferencedArticleRefCreateManySourceInput = {
  targetId: Scalars['String'];
  reference_type: Scalars['String'];
};

export type GqlReferencedArticleRefCreateManySourceInputEnvelope = {
  data: Array<GqlReferencedArticleRefCreateManySourceInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlReferencedArticleRefCreateManyTargetInput = {
  sourceId: Scalars['String'];
  reference_type: Scalars['String'];
};

export type GqlReferencedArticleRefCreateManyTargetInputEnvelope = {
  data: Array<GqlReferencedArticleRefCreateManyTargetInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlReferencedArticleRefCreateNestedManyWithoutSourceInput = {
  create?: Maybe<Array<GqlReferencedArticleRefCreateWithoutSourceInput>>;
  connectOrCreate?: Maybe<Array<GqlReferencedArticleRefCreateOrConnectWithoutSourceInput>>;
  createMany?: Maybe<GqlReferencedArticleRefCreateManySourceInputEnvelope>;
  connect?: Maybe<Array<GqlReferencedArticleRefWhereUniqueInput>>;
};

export type GqlReferencedArticleRefCreateNestedManyWithoutTargetInput = {
  create?: Maybe<Array<GqlReferencedArticleRefCreateWithoutTargetInput>>;
  connectOrCreate?: Maybe<Array<GqlReferencedArticleRefCreateOrConnectWithoutTargetInput>>;
  createMany?: Maybe<GqlReferencedArticleRefCreateManyTargetInputEnvelope>;
  connect?: Maybe<Array<GqlReferencedArticleRefWhereUniqueInput>>;
};

export type GqlReferencedArticleRefCreateOrConnectWithoutSourceInput = {
  where: GqlReferencedArticleRefWhereUniqueInput;
  create: GqlReferencedArticleRefCreateWithoutSourceInput;
};

export type GqlReferencedArticleRefCreateOrConnectWithoutTargetInput = {
  where: GqlReferencedArticleRefWhereUniqueInput;
  create: GqlReferencedArticleRefCreateWithoutTargetInput;
};

export type GqlReferencedArticleRefCreateWithoutSourceInput = {
  reference_type: Scalars['String'];
  target: GqlArticleRefCreateNestedOneWithoutRelatives_InInput;
};

export type GqlReferencedArticleRefCreateWithoutTargetInput = {
  reference_type: Scalars['String'];
  source: GqlArticleRefCreateNestedOneWithoutRelatives_OutInput;
};

export type GqlReferencedArticleRefGroupBy = {
  __typename?: 'ReferencedArticleRefGroupBy';
  sourceId: FieldWrapper<Scalars['String']>;
  targetId: FieldWrapper<Scalars['String']>;
  reference_type: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlReferencedArticleRefCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlReferencedArticleRefMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlReferencedArticleRefMaxAggregate>>;
};

export type GqlReferencedArticleRefListRelationFilter = {
  every?: Maybe<GqlReferencedArticleRefWhereInput>;
  some?: Maybe<GqlReferencedArticleRefWhereInput>;
  none?: Maybe<GqlReferencedArticleRefWhereInput>;
};

export type GqlReferencedArticleRefMaxAggregate = {
  __typename?: 'ReferencedArticleRefMaxAggregate';
  sourceId?: Maybe<FieldWrapper<Scalars['String']>>;
  targetId?: Maybe<FieldWrapper<Scalars['String']>>;
  reference_type?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlReferencedArticleRefMinAggregate = {
  __typename?: 'ReferencedArticleRefMinAggregate';
  sourceId?: Maybe<FieldWrapper<Scalars['String']>>;
  targetId?: Maybe<FieldWrapper<Scalars['String']>>;
  reference_type?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlReferencedArticleRefOrderByInput = {
  sourceId?: Maybe<GqlSortOrder>;
  targetId?: Maybe<GqlSortOrder>;
  reference_type?: Maybe<GqlSortOrder>;
};

export enum GqlReferencedArticleRefScalarFieldEnum {
  SourceId = 'sourceId',
  TargetId = 'targetId',
  ReferenceType = 'reference_type'
}

export type GqlReferencedArticleRefScalarWhereInput = {
  AND?: Maybe<Array<GqlReferencedArticleRefScalarWhereInput>>;
  OR?: Maybe<Array<GqlReferencedArticleRefScalarWhereInput>>;
  NOT?: Maybe<Array<GqlReferencedArticleRefScalarWhereInput>>;
  sourceId?: Maybe<GqlStringFilter>;
  targetId?: Maybe<GqlStringFilter>;
  reference_type?: Maybe<GqlStringFilter>;
};

export type GqlReferencedArticleRefScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlReferencedArticleRefScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlReferencedArticleRefScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlReferencedArticleRefScalarWhereWithAggregatesInput>>;
  sourceId?: Maybe<GqlStringWithAggregatesFilter>;
  targetId?: Maybe<GqlStringWithAggregatesFilter>;
  reference_type?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlReferencedArticleRefSourceIdTargetIdCompoundUniqueInput = {
  sourceId: Scalars['String'];
  targetId: Scalars['String'];
};

export type GqlReferencedArticleRefUpdateInput = {
  reference_type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  source?: Maybe<GqlArticleRefUpdateOneRequiredWithoutRelatives_OutInput>;
  target?: Maybe<GqlArticleRefUpdateOneRequiredWithoutRelatives_InInput>;
};

export type GqlReferencedArticleRefUpdateManyMutationInput = {
  reference_type?: Maybe<GqlStringFieldUpdateOperationsInput>;
};

export type GqlReferencedArticleRefUpdateManyWithWhereWithoutSourceInput = {
  where: GqlReferencedArticleRefScalarWhereInput;
  data: GqlReferencedArticleRefUpdateManyMutationInput;
};

export type GqlReferencedArticleRefUpdateManyWithWhereWithoutTargetInput = {
  where: GqlReferencedArticleRefScalarWhereInput;
  data: GqlReferencedArticleRefUpdateManyMutationInput;
};

export type GqlReferencedArticleRefUpdateManyWithoutSourceInput = {
  create?: Maybe<Array<GqlReferencedArticleRefCreateWithoutSourceInput>>;
  connectOrCreate?: Maybe<Array<GqlReferencedArticleRefCreateOrConnectWithoutSourceInput>>;
  upsert?: Maybe<Array<GqlReferencedArticleRefUpsertWithWhereUniqueWithoutSourceInput>>;
  createMany?: Maybe<GqlReferencedArticleRefCreateManySourceInputEnvelope>;
  connect?: Maybe<Array<GqlReferencedArticleRefWhereUniqueInput>>;
  set?: Maybe<Array<GqlReferencedArticleRefWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlReferencedArticleRefWhereUniqueInput>>;
  delete?: Maybe<Array<GqlReferencedArticleRefWhereUniqueInput>>;
  update?: Maybe<Array<GqlReferencedArticleRefUpdateWithWhereUniqueWithoutSourceInput>>;
  updateMany?: Maybe<Array<GqlReferencedArticleRefUpdateManyWithWhereWithoutSourceInput>>;
  deleteMany?: Maybe<Array<GqlReferencedArticleRefScalarWhereInput>>;
};

export type GqlReferencedArticleRefUpdateManyWithoutTargetInput = {
  create?: Maybe<Array<GqlReferencedArticleRefCreateWithoutTargetInput>>;
  connectOrCreate?: Maybe<Array<GqlReferencedArticleRefCreateOrConnectWithoutTargetInput>>;
  upsert?: Maybe<Array<GqlReferencedArticleRefUpsertWithWhereUniqueWithoutTargetInput>>;
  createMany?: Maybe<GqlReferencedArticleRefCreateManyTargetInputEnvelope>;
  connect?: Maybe<Array<GqlReferencedArticleRefWhereUniqueInput>>;
  set?: Maybe<Array<GqlReferencedArticleRefWhereUniqueInput>>;
  disconnect?: Maybe<Array<GqlReferencedArticleRefWhereUniqueInput>>;
  delete?: Maybe<Array<GqlReferencedArticleRefWhereUniqueInput>>;
  update?: Maybe<Array<GqlReferencedArticleRefUpdateWithWhereUniqueWithoutTargetInput>>;
  updateMany?: Maybe<Array<GqlReferencedArticleRefUpdateManyWithWhereWithoutTargetInput>>;
  deleteMany?: Maybe<Array<GqlReferencedArticleRefScalarWhereInput>>;
};

export type GqlReferencedArticleRefUpdateWithWhereUniqueWithoutSourceInput = {
  where: GqlReferencedArticleRefWhereUniqueInput;
  data: GqlReferencedArticleRefUpdateWithoutSourceInput;
};

export type GqlReferencedArticleRefUpdateWithWhereUniqueWithoutTargetInput = {
  where: GqlReferencedArticleRefWhereUniqueInput;
  data: GqlReferencedArticleRefUpdateWithoutTargetInput;
};

export type GqlReferencedArticleRefUpdateWithoutSourceInput = {
  reference_type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  target?: Maybe<GqlArticleRefUpdateOneRequiredWithoutRelatives_InInput>;
};

export type GqlReferencedArticleRefUpdateWithoutTargetInput = {
  reference_type?: Maybe<GqlStringFieldUpdateOperationsInput>;
  source?: Maybe<GqlArticleRefUpdateOneRequiredWithoutRelatives_OutInput>;
};

export type GqlReferencedArticleRefUpsertWithWhereUniqueWithoutSourceInput = {
  where: GqlReferencedArticleRefWhereUniqueInput;
  update: GqlReferencedArticleRefUpdateWithoutSourceInput;
  create: GqlReferencedArticleRefCreateWithoutSourceInput;
};

export type GqlReferencedArticleRefUpsertWithWhereUniqueWithoutTargetInput = {
  where: GqlReferencedArticleRefWhereUniqueInput;
  update: GqlReferencedArticleRefUpdateWithoutTargetInput;
  create: GqlReferencedArticleRefCreateWithoutTargetInput;
};

export type GqlReferencedArticleRefWhereInput = {
  AND?: Maybe<Array<GqlReferencedArticleRefWhereInput>>;
  OR?: Maybe<Array<GqlReferencedArticleRefWhereInput>>;
  NOT?: Maybe<Array<GqlReferencedArticleRefWhereInput>>;
  source?: Maybe<GqlArticleRefRelationFilter>;
  sourceId?: Maybe<GqlStringFilter>;
  target?: Maybe<GqlArticleRefRelationFilter>;
  targetId?: Maybe<GqlStringFilter>;
  reference_type?: Maybe<GqlStringFilter>;
};

export type GqlReferencedArticleRefWhereUniqueInput = {
  sourceId_targetId?: Maybe<GqlReferencedArticleRefSourceIdTargetIdCompoundUniqueInput>;
};

export enum GqlSortOrder {
  Asc = 'asc',
  Desc = 'desc'
}

export type GqlStream = {
  __typename?: 'Stream';
  id: FieldWrapper<Scalars['String']>;
  articleRefs: Array<FieldWrapper<GqlArticleRef>>;
  feeds: Array<FieldWrapper<GqlFeed>>;
  buckets: Array<FieldWrapper<GqlBucket>>;
  notebooks: Array<FieldWrapper<GqlNotebook>>;
};


export type GqlStreamArticleRefsArgs = {
  where?: Maybe<GqlArticleRefWhereInput>;
  orderBy?: Maybe<Array<GqlArticleRefOrderByInput>>;
  cursor?: Maybe<GqlArticleRefWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlArticleRefScalarFieldEnum>>;
};


export type GqlStreamFeedsArgs = {
  where?: Maybe<GqlFeedWhereInput>;
  orderBy?: Maybe<Array<GqlFeedOrderByInput>>;
  cursor?: Maybe<GqlFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlFeedScalarFieldEnum>>;
};


export type GqlStreamBucketsArgs = {
  where?: Maybe<GqlBucketWhereInput>;
  orderBy?: Maybe<Array<GqlBucketOrderByInput>>;
  cursor?: Maybe<GqlBucketWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlBucketScalarFieldEnum>>;
};


export type GqlStreamNotebooksArgs = {
  where?: Maybe<GqlNotebookWhereInput>;
  orderBy?: Maybe<Array<GqlNotebookOrderByInput>>;
  cursor?: Maybe<GqlNotebookWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlNotebookScalarFieldEnum>>;
};

export type GqlStreamCountAggregate = {
  __typename?: 'StreamCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlStreamCreateInput = {
  id?: Maybe<Scalars['String']>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutStreamInput>;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutStreamInput>;
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutStreamInput>;
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutStreamInput>;
};

export type GqlStreamCreateManyInput = {
  id?: Maybe<Scalars['String']>;
};

export type GqlStreamCreateNestedManyWithoutArticleRefsInput = {
  create?: Maybe<Array<GqlStreamCreateWithoutArticleRefsInput>>;
  connectOrCreate?: Maybe<Array<GqlStreamCreateOrConnectWithoutArticleRefsInput>>;
  connect?: Maybe<Array<GqlStreamWhereUniqueInput>>;
};

export type GqlStreamCreateNestedOneWithoutBucketsInput = {
  create?: Maybe<GqlStreamCreateWithoutBucketsInput>;
  connectOrCreate?: Maybe<GqlStreamCreateOrConnectWithoutBucketsInput>;
  connect?: Maybe<GqlStreamWhereUniqueInput>;
};

export type GqlStreamCreateNestedOneWithoutFeedsInput = {
  create?: Maybe<GqlStreamCreateWithoutFeedsInput>;
  connectOrCreate?: Maybe<GqlStreamCreateOrConnectWithoutFeedsInput>;
  connect?: Maybe<GqlStreamWhereUniqueInput>;
};

export type GqlStreamCreateNestedOneWithoutNotebooksInput = {
  create?: Maybe<GqlStreamCreateWithoutNotebooksInput>;
  connectOrCreate?: Maybe<GqlStreamCreateOrConnectWithoutNotebooksInput>;
  connect?: Maybe<GqlStreamWhereUniqueInput>;
};

export type GqlStreamCreateOrConnectWithoutArticleRefsInput = {
  where: GqlStreamWhereUniqueInput;
  create: GqlStreamCreateWithoutArticleRefsInput;
};

export type GqlStreamCreateOrConnectWithoutBucketsInput = {
  where: GqlStreamWhereUniqueInput;
  create: GqlStreamCreateWithoutBucketsInput;
};

export type GqlStreamCreateOrConnectWithoutFeedsInput = {
  where: GqlStreamWhereUniqueInput;
  create: GqlStreamCreateWithoutFeedsInput;
};

export type GqlStreamCreateOrConnectWithoutNotebooksInput = {
  where: GqlStreamWhereUniqueInput;
  create: GqlStreamCreateWithoutNotebooksInput;
};

export type GqlStreamCreateWithoutArticleRefsInput = {
  id?: Maybe<Scalars['String']>;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutStreamInput>;
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutStreamInput>;
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutStreamInput>;
};

export type GqlStreamCreateWithoutBucketsInput = {
  id?: Maybe<Scalars['String']>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutStreamInput>;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutStreamInput>;
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutStreamInput>;
};

export type GqlStreamCreateWithoutFeedsInput = {
  id?: Maybe<Scalars['String']>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutStreamInput>;
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutStreamInput>;
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutStreamInput>;
};

export type GqlStreamCreateWithoutNotebooksInput = {
  id?: Maybe<Scalars['String']>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutStreamInput>;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutStreamInput>;
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutStreamInput>;
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
  feeds?: Maybe<GqlFeedUpdateManyWithoutStreamInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutStreamInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutStreamInput>;
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

export type GqlStreamUpdateOneRequiredWithoutBucketsInput = {
  create?: Maybe<GqlStreamCreateWithoutBucketsInput>;
  connectOrCreate?: Maybe<GqlStreamCreateOrConnectWithoutBucketsInput>;
  upsert?: Maybe<GqlStreamUpsertWithoutBucketsInput>;
  connect?: Maybe<GqlStreamWhereUniqueInput>;
  update?: Maybe<GqlStreamUpdateWithoutBucketsInput>;
};

export type GqlStreamUpdateOneRequiredWithoutFeedsInput = {
  create?: Maybe<GqlStreamCreateWithoutFeedsInput>;
  connectOrCreate?: Maybe<GqlStreamCreateOrConnectWithoutFeedsInput>;
  upsert?: Maybe<GqlStreamUpsertWithoutFeedsInput>;
  connect?: Maybe<GqlStreamWhereUniqueInput>;
  update?: Maybe<GqlStreamUpdateWithoutFeedsInput>;
};

export type GqlStreamUpdateOneRequiredWithoutNotebooksInput = {
  create?: Maybe<GqlStreamCreateWithoutNotebooksInput>;
  connectOrCreate?: Maybe<GqlStreamCreateOrConnectWithoutNotebooksInput>;
  upsert?: Maybe<GqlStreamUpsertWithoutNotebooksInput>;
  connect?: Maybe<GqlStreamWhereUniqueInput>;
  update?: Maybe<GqlStreamUpdateWithoutNotebooksInput>;
};

export type GqlStreamUpdateWithWhereUniqueWithoutArticleRefsInput = {
  where: GqlStreamWhereUniqueInput;
  data: GqlStreamUpdateWithoutArticleRefsInput;
};

export type GqlStreamUpdateWithoutArticleRefsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutStreamInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutStreamInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutStreamInput>;
};

export type GqlStreamUpdateWithoutBucketsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutStreamInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutStreamInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutStreamInput>;
};

export type GqlStreamUpdateWithoutFeedsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutStreamInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutStreamInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutStreamInput>;
};

export type GqlStreamUpdateWithoutNotebooksInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutStreamInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutStreamInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutStreamInput>;
};

export type GqlStreamUpsertWithWhereUniqueWithoutArticleRefsInput = {
  where: GqlStreamWhereUniqueInput;
  update: GqlStreamUpdateWithoutArticleRefsInput;
  create: GqlStreamCreateWithoutArticleRefsInput;
};

export type GqlStreamUpsertWithoutBucketsInput = {
  update: GqlStreamUpdateWithoutBucketsInput;
  create: GqlStreamCreateWithoutBucketsInput;
};

export type GqlStreamUpsertWithoutFeedsInput = {
  update: GqlStreamUpdateWithoutFeedsInput;
  create: GqlStreamCreateWithoutFeedsInput;
};

export type GqlStreamUpsertWithoutNotebooksInput = {
  update: GqlStreamUpdateWithoutNotebooksInput;
  create: GqlStreamCreateWithoutNotebooksInput;
};

export type GqlStreamWhereInput = {
  AND?: Maybe<Array<GqlStreamWhereInput>>;
  OR?: Maybe<Array<GqlStreamWhereInput>>;
  NOT?: Maybe<Array<GqlStreamWhereInput>>;
  id?: Maybe<GqlStringFilter>;
  articleRefs?: Maybe<GqlArticleRefListRelationFilter>;
  feeds?: Maybe<GqlFeedListRelationFilter>;
  buckets?: Maybe<GqlBucketListRelationFilter>;
  notebooks?: Maybe<GqlNotebookListRelationFilter>;
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
  mode?: Maybe<GqlQueryMode>;
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
  mode?: Maybe<GqlQueryMode>;
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
  mode?: Maybe<GqlQueryMode>;
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
  mode?: Maybe<GqlQueryMode>;
  not?: Maybe<GqlNestedStringWithAggregatesFilter>;
  _count?: Maybe<GqlNestedIntFilter>;
  _min?: Maybe<GqlNestedStringFilter>;
  _max?: Maybe<GqlNestedStringFilter>;
};

export type GqlSubscription = {
  __typename?: 'Subscription';
  id: FieldWrapper<Scalars['String']>;
  inactive: FieldWrapper<Scalars['Boolean']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  title: FieldWrapper<Scalars['String']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
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
  inactive: FieldWrapper<Scalars['Int']>;
  createdAt: FieldWrapper<Scalars['Int']>;
  lastUpdatedAt: FieldWrapper<Scalars['Int']>;
  title: FieldWrapper<Scalars['Int']>;
  tags: FieldWrapper<Scalars['Int']>;
  feedId: FieldWrapper<Scalars['Int']>;
  ownerId: FieldWrapper<Scalars['Int']>;
  bucketId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlSubscriptionCreateInput = {
  id?: Maybe<Scalars['String']>;
  inactive?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  feed: GqlFeedCreateNestedOneWithoutSubscriptionsInput;
  owner: GqlUserCreateNestedOneWithoutSubscriptionInput;
  bucket: GqlBucketCreateNestedOneWithoutSubscriptionsInput;
};

export type GqlSubscriptionCreateManyBucketInput = {
  id?: Maybe<Scalars['String']>;
  inactive?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  feedId: Scalars['String'];
  ownerId: Scalars['String'];
};

export type GqlSubscriptionCreateManyBucketInputEnvelope = {
  data: Array<GqlSubscriptionCreateManyBucketInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlSubscriptionCreateManyFeedInput = {
  id?: Maybe<Scalars['String']>;
  inactive?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  ownerId: Scalars['String'];
  bucketId: Scalars['String'];
};

export type GqlSubscriptionCreateManyFeedInputEnvelope = {
  data: Array<GqlSubscriptionCreateManyFeedInput>;
  skipDuplicates?: Maybe<Scalars['Boolean']>;
};

export type GqlSubscriptionCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  inactive?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  feedId: Scalars['String'];
  ownerId: Scalars['String'];
  bucketId: Scalars['String'];
};

export type GqlSubscriptionCreateManyOwnerInput = {
  id?: Maybe<Scalars['String']>;
  inactive?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  feedId: Scalars['String'];
  bucketId: Scalars['String'];
};

export type GqlSubscriptionCreateManyOwnerInputEnvelope = {
  data: Array<GqlSubscriptionCreateManyOwnerInput>;
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
  inactive?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  feed: GqlFeedCreateNestedOneWithoutSubscriptionsInput;
  owner: GqlUserCreateNestedOneWithoutSubscriptionInput;
};

export type GqlSubscriptionCreateWithoutFeedInput = {
  id?: Maybe<Scalars['String']>;
  inactive?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  owner: GqlUserCreateNestedOneWithoutSubscriptionInput;
  bucket: GqlBucketCreateNestedOneWithoutSubscriptionsInput;
};

export type GqlSubscriptionCreateWithoutOwnerInput = {
  id?: Maybe<Scalars['String']>;
  inactive?: Maybe<Scalars['Boolean']>;
  createdAt?: Maybe<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<Scalars['DateTime']>;
  title: Scalars['String'];
  tags?: Maybe<Scalars['JSON']>;
  feed: GqlFeedCreateNestedOneWithoutSubscriptionsInput;
  bucket: GqlBucketCreateNestedOneWithoutSubscriptionsInput;
};

export type GqlSubscriptionGroupBy = {
  __typename?: 'SubscriptionGroupBy';
  id: FieldWrapper<Scalars['String']>;
  inactive: FieldWrapper<Scalars['Boolean']>;
  createdAt: FieldWrapper<Scalars['DateTime']>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  title: FieldWrapper<Scalars['String']>;
  tags?: Maybe<FieldWrapper<Scalars['JSON']>>;
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
  inactive?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlSubscriptionMinAggregate = {
  __typename?: 'SubscriptionMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  inactive?: Maybe<FieldWrapper<Scalars['Boolean']>>;
  createdAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  lastUpdatedAt?: Maybe<FieldWrapper<Scalars['DateTime']>>;
  title?: Maybe<FieldWrapper<Scalars['String']>>;
  feedId?: Maybe<FieldWrapper<Scalars['String']>>;
  ownerId?: Maybe<FieldWrapper<Scalars['String']>>;
  bucketId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlSubscriptionOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  inactive?: Maybe<GqlSortOrder>;
  createdAt?: Maybe<GqlSortOrder>;
  lastUpdatedAt?: Maybe<GqlSortOrder>;
  title?: Maybe<GqlSortOrder>;
  tags?: Maybe<GqlSortOrder>;
  feedId?: Maybe<GqlSortOrder>;
  ownerId?: Maybe<GqlSortOrder>;
  bucketId?: Maybe<GqlSortOrder>;
};

export enum GqlSubscriptionScalarFieldEnum {
  Id = 'id',
  Inactive = 'inactive',
  CreatedAt = 'createdAt',
  LastUpdatedAt = 'lastUpdatedAt',
  Title = 'title',
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
  inactive?: Maybe<GqlBoolFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableFilter>;
  title?: Maybe<GqlStringFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
  feedId?: Maybe<GqlStringFilter>;
  ownerId?: Maybe<GqlStringFilter>;
  bucketId?: Maybe<GqlStringFilter>;
};

export type GqlSubscriptionScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlSubscriptionScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlSubscriptionScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlSubscriptionScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  inactive?: Maybe<GqlBoolWithAggregatesFilter>;
  createdAt?: Maybe<GqlDateTimeWithAggregatesFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableWithAggregatesFilter>;
  title?: Maybe<GqlStringWithAggregatesFilter>;
  tags?: Maybe<GqlJsonNullableWithAggregatesFilter>;
  feedId?: Maybe<GqlStringWithAggregatesFilter>;
  ownerId?: Maybe<GqlStringWithAggregatesFilter>;
  bucketId?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlSubscriptionUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  feed?: Maybe<GqlFeedUpdateOneRequiredWithoutSubscriptionsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutSubscriptionInput>;
  bucket?: Maybe<GqlBucketUpdateOneRequiredWithoutSubscriptionsInput>;
};

export type GqlSubscriptionUpdateManyMutationInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
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
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  feed?: Maybe<GqlFeedUpdateOneRequiredWithoutSubscriptionsInput>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutSubscriptionInput>;
};

export type GqlSubscriptionUpdateWithoutFeedInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
  owner?: Maybe<GqlUserUpdateOneRequiredWithoutSubscriptionInput>;
  bucket?: Maybe<GqlBucketUpdateOneRequiredWithoutSubscriptionsInput>;
};

export type GqlSubscriptionUpdateWithoutOwnerInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  inactive?: Maybe<GqlBoolFieldUpdateOperationsInput>;
  createdAt?: Maybe<GqlDateTimeFieldUpdateOperationsInput>;
  lastUpdatedAt?: Maybe<GqlNullableDateTimeFieldUpdateOperationsInput>;
  title?: Maybe<GqlStringFieldUpdateOperationsInput>;
  tags?: Maybe<Scalars['JSON']>;
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
  inactive?: Maybe<GqlBoolFilter>;
  createdAt?: Maybe<GqlDateTimeFilter>;
  lastUpdatedAt?: Maybe<GqlDateTimeNullableFilter>;
  title?: Maybe<GqlStringFilter>;
  tags?: Maybe<GqlJsonNullableFilter>;
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
  settingsId: FieldWrapper<Scalars['String']>;
  buckets: Array<FieldWrapper<GqlBucket>>;
  articleRefs: Array<FieldWrapper<GqlArticleRef>>;
  notebooks: Array<FieldWrapper<GqlNotebook>>;
  subscription: Array<FieldWrapper<GqlSubscription>>;
  settings: FieldWrapper<GqlProfileSettings>;
  feeds: Array<FieldWrapper<GqlFeed>>;
  eventHooks: Array<FieldWrapper<GqlEventHook>>;
  plugins: Array<FieldWrapper<GqlPlugin>>;
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


export type GqlUserNotebooksArgs = {
  where?: Maybe<GqlNotebookWhereInput>;
  orderBy?: Maybe<Array<GqlNotebookOrderByInput>>;
  cursor?: Maybe<GqlNotebookWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlNotebookScalarFieldEnum>>;
};


export type GqlUserSubscriptionArgs = {
  where?: Maybe<GqlSubscriptionWhereInput>;
  orderBy?: Maybe<Array<GqlSubscriptionOrderByInput>>;
  cursor?: Maybe<GqlSubscriptionWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlSubscriptionScalarFieldEnum>>;
};


export type GqlUserFeedsArgs = {
  where?: Maybe<GqlFeedWhereInput>;
  orderBy?: Maybe<Array<GqlFeedOrderByInput>>;
  cursor?: Maybe<GqlFeedWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlFeedScalarFieldEnum>>;
};


export type GqlUserEventHooksArgs = {
  where?: Maybe<GqlEventHookWhereInput>;
  orderBy?: Maybe<Array<GqlEventHookOrderByInput>>;
  cursor?: Maybe<GqlEventHookWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlEventHookScalarFieldEnum>>;
};


export type GqlUserPluginsArgs = {
  where?: Maybe<GqlPluginWhereInput>;
  orderBy?: Maybe<Array<GqlPluginOrderByInput>>;
  cursor?: Maybe<GqlPluginWhereUniqueInput>;
  take?: Maybe<Scalars['Int']>;
  skip?: Maybe<Scalars['Int']>;
  distinct?: Maybe<Array<GqlPluginScalarFieldEnum>>;
};

export type GqlUserCountAggregate = {
  __typename?: 'UserCountAggregate';
  id: FieldWrapper<Scalars['Int']>;
  email: FieldWrapper<Scalars['Int']>;
  name: FieldWrapper<Scalars['Int']>;
  settingsId: FieldWrapper<Scalars['Int']>;
  _all: FieldWrapper<Scalars['Int']>;
};

export type GqlUserCreateInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
  settings: GqlProfileSettingsCreateNestedOneWithoutUserInput;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookCreateNestedManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateManyInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  settingsId: Scalars['String'];
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

export type GqlUserCreateNestedOneWithoutEventHooksInput = {
  create?: Maybe<GqlUserCreateWithoutEventHooksInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutEventHooksInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
};

export type GqlUserCreateNestedOneWithoutFeedsInput = {
  create?: Maybe<GqlUserCreateWithoutFeedsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutFeedsInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
};

export type GqlUserCreateNestedOneWithoutNotebooksInput = {
  create?: Maybe<GqlUserCreateWithoutNotebooksInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutNotebooksInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
};

export type GqlUserCreateNestedOneWithoutPluginsInput = {
  create?: Maybe<GqlUserCreateWithoutPluginsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutPluginsInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
};

export type GqlUserCreateNestedOneWithoutSettingsInput = {
  create?: Maybe<GqlUserCreateWithoutSettingsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutSettingsInput>;
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

export type GqlUserCreateOrConnectWithoutEventHooksInput = {
  where: GqlUserWhereUniqueInput;
  create: GqlUserCreateWithoutEventHooksInput;
};

export type GqlUserCreateOrConnectWithoutFeedsInput = {
  where: GqlUserWhereUniqueInput;
  create: GqlUserCreateWithoutFeedsInput;
};

export type GqlUserCreateOrConnectWithoutNotebooksInput = {
  where: GqlUserWhereUniqueInput;
  create: GqlUserCreateWithoutNotebooksInput;
};

export type GqlUserCreateOrConnectWithoutPluginsInput = {
  where: GqlUserWhereUniqueInput;
  create: GqlUserCreateWithoutPluginsInput;
};

export type GqlUserCreateOrConnectWithoutSettingsInput = {
  where: GqlUserWhereUniqueInput;
  create: GqlUserCreateWithoutSettingsInput;
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
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
  settings: GqlProfileSettingsCreateNestedOneWithoutUserInput;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookCreateNestedManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateWithoutBucketsInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
  settings: GqlProfileSettingsCreateNestedOneWithoutUserInput;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookCreateNestedManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateWithoutEventHooksInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
  settings: GqlProfileSettingsCreateNestedOneWithoutUserInput;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateWithoutFeedsInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
  settings: GqlProfileSettingsCreateNestedOneWithoutUserInput;
  eventHooks?: Maybe<GqlEventHookCreateNestedManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateWithoutNotebooksInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
  settings: GqlProfileSettingsCreateNestedOneWithoutUserInput;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookCreateNestedManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateWithoutPluginsInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
  settings: GqlProfileSettingsCreateNestedOneWithoutUserInput;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateWithoutSettingsInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionCreateNestedManyWithoutOwnerInput>;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookCreateNestedManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserCreateWithoutSubscriptionInput = {
  id?: Maybe<Scalars['String']>;
  email: Scalars['String'];
  name: Scalars['String'];
  buckets?: Maybe<GqlBucketCreateNestedManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefCreateNestedManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookCreateNestedManyWithoutOwnerInput>;
  settings: GqlProfileSettingsCreateNestedOneWithoutUserInput;
  feeds?: Maybe<GqlFeedCreateNestedManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookCreateNestedManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginCreateNestedManyWithoutOwnerInput>;
};

export type GqlUserGroupBy = {
  __typename?: 'UserGroupBy';
  id: FieldWrapper<Scalars['String']>;
  email: FieldWrapper<Scalars['String']>;
  name: FieldWrapper<Scalars['String']>;
  settingsId: FieldWrapper<Scalars['String']>;
  _count?: Maybe<FieldWrapper<GqlUserCountAggregate>>;
  _min?: Maybe<FieldWrapper<GqlUserMinAggregate>>;
  _max?: Maybe<FieldWrapper<GqlUserMaxAggregate>>;
};

export type GqlUserMaxAggregate = {
  __typename?: 'UserMaxAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  email?: Maybe<FieldWrapper<Scalars['String']>>;
  name?: Maybe<FieldWrapper<Scalars['String']>>;
  settingsId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlUserMinAggregate = {
  __typename?: 'UserMinAggregate';
  id?: Maybe<FieldWrapper<Scalars['String']>>;
  email?: Maybe<FieldWrapper<Scalars['String']>>;
  name?: Maybe<FieldWrapper<Scalars['String']>>;
  settingsId?: Maybe<FieldWrapper<Scalars['String']>>;
};

export type GqlUserOrderByInput = {
  id?: Maybe<GqlSortOrder>;
  email?: Maybe<GqlSortOrder>;
  name?: Maybe<GqlSortOrder>;
  settingsId?: Maybe<GqlSortOrder>;
};

export type GqlUserRelationFilter = {
  is?: Maybe<GqlUserWhereInput>;
  isNot?: Maybe<GqlUserWhereInput>;
};

export enum GqlUserScalarFieldEnum {
  Id = 'id',
  Email = 'email',
  Name = 'name',
  SettingsId = 'settingsId'
}

export type GqlUserScalarWhereWithAggregatesInput = {
  AND?: Maybe<Array<GqlUserScalarWhereWithAggregatesInput>>;
  OR?: Maybe<Array<GqlUserScalarWhereWithAggregatesInput>>;
  NOT?: Maybe<Array<GqlUserScalarWhereWithAggregatesInput>>;
  id?: Maybe<GqlStringWithAggregatesFilter>;
  email?: Maybe<GqlStringWithAggregatesFilter>;
  name?: Maybe<GqlStringWithAggregatesFilter>;
  settingsId?: Maybe<GqlStringWithAggregatesFilter>;
};

export type GqlUserUpdateInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
  settings?: Maybe<GqlProfileSettingsUpdateOneRequiredWithoutUserInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookUpdateManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginUpdateManyWithoutOwnerInput>;
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

export type GqlUserUpdateOneRequiredWithoutEventHooksInput = {
  create?: Maybe<GqlUserCreateWithoutEventHooksInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutEventHooksInput>;
  upsert?: Maybe<GqlUserUpsertWithoutEventHooksInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
  update?: Maybe<GqlUserUpdateWithoutEventHooksInput>;
};

export type GqlUserUpdateOneRequiredWithoutFeedsInput = {
  create?: Maybe<GqlUserCreateWithoutFeedsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutFeedsInput>;
  upsert?: Maybe<GqlUserUpsertWithoutFeedsInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
  update?: Maybe<GqlUserUpdateWithoutFeedsInput>;
};

export type GqlUserUpdateOneRequiredWithoutNotebooksInput = {
  create?: Maybe<GqlUserCreateWithoutNotebooksInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutNotebooksInput>;
  upsert?: Maybe<GqlUserUpsertWithoutNotebooksInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
  update?: Maybe<GqlUserUpdateWithoutNotebooksInput>;
};

export type GqlUserUpdateOneRequiredWithoutPluginsInput = {
  create?: Maybe<GqlUserCreateWithoutPluginsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutPluginsInput>;
  upsert?: Maybe<GqlUserUpsertWithoutPluginsInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
  update?: Maybe<GqlUserUpdateWithoutPluginsInput>;
};

export type GqlUserUpdateOneRequiredWithoutSubscriptionInput = {
  create?: Maybe<GqlUserCreateWithoutSubscriptionInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutSubscriptionInput>;
  upsert?: Maybe<GqlUserUpsertWithoutSubscriptionInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
  update?: Maybe<GqlUserUpdateWithoutSubscriptionInput>;
};

export type GqlUserUpdateOneWithoutSettingsInput = {
  create?: Maybe<GqlUserCreateWithoutSettingsInput>;
  connectOrCreate?: Maybe<GqlUserCreateOrConnectWithoutSettingsInput>;
  upsert?: Maybe<GqlUserUpsertWithoutSettingsInput>;
  connect?: Maybe<GqlUserWhereUniqueInput>;
  disconnect?: Maybe<Scalars['Boolean']>;
  delete?: Maybe<Scalars['Boolean']>;
  update?: Maybe<GqlUserUpdateWithoutSettingsInput>;
};

export type GqlUserUpdateWithoutArticleRefsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
  settings?: Maybe<GqlProfileSettingsUpdateOneRequiredWithoutUserInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookUpdateManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpdateWithoutBucketsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
  settings?: Maybe<GqlProfileSettingsUpdateOneRequiredWithoutUserInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookUpdateManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpdateWithoutEventHooksInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
  settings?: Maybe<GqlProfileSettingsUpdateOneRequiredWithoutUserInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpdateWithoutFeedsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
  settings?: Maybe<GqlProfileSettingsUpdateOneRequiredWithoutUserInput>;
  eventHooks?: Maybe<GqlEventHookUpdateManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpdateWithoutNotebooksInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
  settings?: Maybe<GqlProfileSettingsUpdateOneRequiredWithoutUserInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookUpdateManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpdateWithoutPluginsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
  settings?: Maybe<GqlProfileSettingsUpdateOneRequiredWithoutUserInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpdateWithoutSettingsInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutOwnerInput>;
  subscription?: Maybe<GqlSubscriptionUpdateManyWithoutOwnerInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookUpdateManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpdateWithoutSubscriptionInput = {
  id?: Maybe<GqlStringFieldUpdateOperationsInput>;
  email?: Maybe<GqlStringFieldUpdateOperationsInput>;
  name?: Maybe<GqlStringFieldUpdateOperationsInput>;
  buckets?: Maybe<GqlBucketUpdateManyWithoutOwnerInput>;
  articleRefs?: Maybe<GqlArticleRefUpdateManyWithoutOwnerInput>;
  notebooks?: Maybe<GqlNotebookUpdateManyWithoutOwnerInput>;
  settings?: Maybe<GqlProfileSettingsUpdateOneRequiredWithoutUserInput>;
  feeds?: Maybe<GqlFeedUpdateManyWithoutOwnerInput>;
  eventHooks?: Maybe<GqlEventHookUpdateManyWithoutOwnerInput>;
  plugins?: Maybe<GqlPluginUpdateManyWithoutOwnerInput>;
};

export type GqlUserUpsertWithoutArticleRefsInput = {
  update: GqlUserUpdateWithoutArticleRefsInput;
  create: GqlUserCreateWithoutArticleRefsInput;
};

export type GqlUserUpsertWithoutBucketsInput = {
  update: GqlUserUpdateWithoutBucketsInput;
  create: GqlUserCreateWithoutBucketsInput;
};

export type GqlUserUpsertWithoutEventHooksInput = {
  update: GqlUserUpdateWithoutEventHooksInput;
  create: GqlUserCreateWithoutEventHooksInput;
};

export type GqlUserUpsertWithoutFeedsInput = {
  update: GqlUserUpdateWithoutFeedsInput;
  create: GqlUserCreateWithoutFeedsInput;
};

export type GqlUserUpsertWithoutNotebooksInput = {
  update: GqlUserUpdateWithoutNotebooksInput;
  create: GqlUserCreateWithoutNotebooksInput;
};

export type GqlUserUpsertWithoutPluginsInput = {
  update: GqlUserUpdateWithoutPluginsInput;
  create: GqlUserCreateWithoutPluginsInput;
};

export type GqlUserUpsertWithoutSettingsInput = {
  update: GqlUserUpdateWithoutSettingsInput;
  create: GqlUserCreateWithoutSettingsInput;
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
  notebooks?: Maybe<GqlNotebookListRelationFilter>;
  subscription?: Maybe<GqlSubscriptionListRelationFilter>;
  settings?: Maybe<GqlProfileSettingsRelationFilter>;
  settingsId?: Maybe<GqlStringFilter>;
  feeds?: Maybe<GqlFeedListRelationFilter>;
  eventHooks?: Maybe<GqlEventHookListRelationFilter>;
  plugins?: Maybe<GqlPluginListRelationFilter>;
};

export type GqlUserWhereUniqueInput = {
  id?: Maybe<Scalars['String']>;
  email?: Maybe<Scalars['String']>;
};
