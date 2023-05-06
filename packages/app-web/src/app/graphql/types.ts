import {
  GqlApiUrls,
  GqlArticle,
  GqlAuthentication,
  GqlBucket,
  GqlEnclosure,
  GqlFeature,
  GqlFeatureBooleanValue,
  GqlFeatureIntValue,
  GqlFeedDiscoveryDocument,
  GqlFeedDiscoveryResponse,
  GqlFetchOptions,
  GqlFilteredRemoteNativeFeedItem,
  GqlGenericFeed,
  GqlImporter,
  GqlNativeFeed,
  GqlPagination,
  GqlParserOptions,
  GqlPlan,
  GqlPlanSubscription,
  GqlProfile,
  GqlRefineOptions,
  GqlRemoteNativeFeed,
  GqlSelectors,
  GqlTransientGenericFeed,
  GqlTransientNativeFeed,
  GqlUser,
  GqlUserSecret,
  GqlWebDocument,
  Maybe,
} from '../../generated/graphql';

export type BasicBucket = Pick<
  GqlBucket,
  | 'id'
  | 'title'
  | 'description'
  | 'imageUrl'
  | 'streamId'
  | 'websiteUrl'
  | 'createdAt'
  | 'tags'
  | 'visibility'
  | 'ownerId'
  | 'importersCount'
  | 'webhook'
  | 'histogram'
>;

export type Bucket = BasicBucket & {
  importers?: Maybe<
    Array<
      BasicImporter & {
        nativeFeed: BasicNativeFeed & {
          genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
        };
      }
    >
  >;
};

export type BucketData = Pick<
  BasicBucket,
  'description' | 'imageUrl' | 'tags' | 'title' | 'visibility' | 'websiteUrl'
>;

export type Content = Pick<
  GqlWebDocument,
  | 'title'
  | 'description'
  | 'contentTitle'
  | 'contentText'
  | 'contentRaw'
  | 'contentRawMime'
  | 'url'
  | 'imageUrl'
  | 'publishedAt'
  | 'updatedAt'
  | 'tags'
>;

export type BasicArticle = Pick<
  GqlArticle,
  'id' | 'status' | 'type' | 'streamId' | 'createdAt'
>;
export type BasicContent = Pick<
  GqlWebDocument,
  | 'title'
  | 'description'
  | 'contentTitle'
  | 'contentText'
  | 'contentRaw'
  | 'contentRawMime'
  | 'url'
  | 'imageUrl'
  | 'publishedAt'
  | 'startingAt'
  | 'updatedAt'
  | 'tags'
  | 'createdAt'
> & {
  enclosures?: Maybe<Array<BasicEnclosure>>;
};
export type Article = BasicArticle & { webDocument: BasicContent };

export type BasicWebDocument = Pick<
  GqlWebDocument,
  'id' | 'title' | 'description' | 'url' | 'imageUrl' | 'createdAt'
>;
export type BasicContext = {
  articles: Array<
    BasicArticle & {
      webDocument: BasicContent;
    }
  >;
  links: Array<BasicWebDocument>;
};

export type ArticleWithContext = BasicArticle & {
  webDocument: BasicContent;
  bucket?: BasicBucket;
  // nativeFeed: BasicNativeFeed;
  context: BasicContext;
};

export type ActualAuthentication = Pick<GqlAuthentication, 'token' | 'corrId'>;

export type BasicNativeFeed = Pick<
  GqlNativeFeed,
  | 'id'
  | 'title'
  | 'description'
  | 'domain'
  | 'websiteUrl'
  | 'imageUrl'
  | 'iconUrl'
  | 'feedUrl'
  | 'status'
  | 'streamId'
  | 'lastUpdatedAt'
  | 'lastChangedAt'
  | 'ownerId'
  | 'createdAt'
>;
export type NativeFeed = BasicNativeFeed & {
  genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
  importers: Array<
    BasicImporter & {
      bucket: BasicBucket;
    }
  >;
};
export type TransientNativeFeed = Pick<
  GqlTransientNativeFeed,
  'url' | 'type' | 'description' | 'title'
>;
export type Selectors = Pick<
  GqlSelectors,
  | 'linkXPath'
  | 'extendContext'
  | 'dateXPath'
  | 'contextXPath'
  | 'dateIsStartOfEvent'
  | 'paginationXPath'
>;
export type BasicEnclosure = Pick<
  GqlEnclosure,
  'size' | 'duration' | 'type' | 'url'
>;
export type TransientGenericFeed = Pick<
  GqlTransientGenericFeed,
  'feedUrl' | 'hash' | 'score' | 'count'
> & {
  selectors: Selectors;
  samples: Array<BasicWebDocument>;
};

export type FeedDiscoveryResult = Pick<
  GqlFeedDiscoveryResponse,
  'failed' | 'errorMessage' | 'websiteUrl'
> & {
  fetchOptions: Pick<
    GqlFetchOptions,
    'prerender' | 'websiteUrl' | 'prerenderScript' | 'prerenderWaitUntil'
  >;
  genericFeeds: {
    parserOptions: Pick<GqlParserOptions, 'strictMode'>;
    feeds: Array<TransientGenericFeed>;
  };
  nativeFeeds?: Maybe<
    Array<
      Pick<GqlTransientNativeFeed, 'url' | 'type' | 'description' | 'title'>
    >
  >;
  document?: Maybe<
    Pick<
      GqlFeedDiscoveryDocument,
      | 'mimeType'
      | 'htmlBody'
      | 'title'
      | 'description'
      | 'language'
      | 'imageUrl'
      | 'url'
      | 'favicon'
    >
  >;
};

export type GenericFeed = Pick<
  GqlGenericFeed,
  'id' | 'feedUrl' | 'hash' | 'createdAt'
> & {
  specification: {
    selectors: Selectors;
    parserOptions: Pick<GqlParserOptions, 'strictMode'>;
    fetchOptions: Pick<
      GqlFetchOptions,
      'prerender' | 'websiteUrl' | 'prerenderScript' | 'prerenderWaitUntil'
    >;
    refineOptions: Pick<GqlRefineOptions, 'filter' | 'recovery'>;
  };
};

export type NativeFeeds = {
  nativeFeeds: Array<BasicNativeFeed>;
  pagination: Pagination;
};
export type RemoteFeedItem = Pick<
  GqlFilteredRemoteNativeFeedItem,
  'omitted'
> & {
  item: Pick<
    GqlWebDocument,
    | 'url'
    | 'title'
    | 'contentText'
    | 'contentRaw'
    | 'contentRawMime'
    | 'publishedAt'
    | 'startingAt'
  >;
};

export type RemoteFeed = Pick<
  GqlRemoteNativeFeed,
  'title' | 'description' | 'websiteUrl' | 'feedUrl'
> & { items?: Array<RemoteFeedItem> };

export type BasicImporter = Pick<
  GqlImporter,
  | 'id'
  | 'email'
  | 'filter'
  | 'webhook'
  | 'autoRelease'
  | 'createdAt'
  | 'nativeFeedId'
  | 'bucketId'
  | 'title'
  | 'lastUpdatedAt'
  | 'segmented'
  | 'histogram'
>;

export type Importer = BasicImporter & {
  nativeFeed: BasicNativeFeed & {
    genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
  };
};

export type Pagination = Pick<
  GqlPagination,
  'page' | 'isLast' | 'isFirst' | 'isEmpty'
>;

export type Feature = Pick<GqlFeature, 'name' | 'state'> & {
  value?: Maybe<{
    boolVal?: Maybe<Pick<GqlFeatureBooleanValue, 'value'>>;
    numVal?: Maybe<Pick<GqlFeatureIntValue, 'value'>>;
  }>;
};
export type Plan = Pick<
  GqlPlan,
  'id' | 'name' | 'availability' | 'isPrimary' | 'costs'
> & { features: Array<Feature> };

export type Profile = Pick<
  GqlProfile,
  'minimalFeatureState' | 'preferFulltext' | 'preferReader' | 'isLoggedIn'
> & {
  user?: Maybe<
    Pick<
      GqlUser,
      'id' | 'acceptedTermsAndServices' | 'name' | 'notificationsStreamId'
    > & {
      secrets: Array<UserSecret>;
      subscription?: Maybe<
        Pick<GqlPlanSubscription, 'expiry' | 'startedAt'> & {
          plan: Pick<
            GqlPlan,
            'id' | 'name' | 'availability' | 'isPrimary' | 'costs'
          >;
        }
      >;
    }
  >;
};

export type UserSecret = Pick<
  GqlUserSecret,
  'id' | 'validUntil' | 'type' | 'lastUsed' | 'value' | 'valueMasked'
>;

export type FlatFeature = Pick<GqlFeature, 'name' | 'state'>;

export type ApiUrls = Pick<GqlApiUrls, 'webToFeed'>;
