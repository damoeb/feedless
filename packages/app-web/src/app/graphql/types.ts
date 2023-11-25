import {
  FieldWrapper,
  GqlApiUrls,
  GqlArticle,
  GqlAuthentication, GqlBoundingBox,
  GqlBucket, GqlDomElementByXPath,
  GqlEmittedScrapeData,
  GqlEnclosure,
  GqlFeature,
  GqlFeatureBooleanValue,
  GqlFeatureIntValue,
  GqlFeedDiscoveryDocument,
  GqlFilteredRemoteNativeFeedItem,
  GqlGenericFeed,
  GqlImporter,
  GqlNativeFeed,
  GqlPagination,
  GqlPlan,
  GqlPlanSubscription,
  GqlPlugin,
  GqlProfile,
  GqlPuppeteerWaitUntil,
  GqlRefineOptions,
  GqlRemoteNativeFeed,
  GqlScrapeDebugResponse, GqlScrapeDebugTimes,
  GqlScrapeDebugTimesInput,
  GqlScrapedElement,
  GqlScrapedReadability,
  GqlScrapeResponse,
  GqlSelectors,
  GqlTransientGenericFeed,
  GqlTransientNativeFeed,
  GqlTransientOrExistingNativeFeed,
  GqlUser,
  GqlUserSecret,
  GqlViewPort,
  GqlWebDocument,
  Maybe,
  Scalars
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
  | 'pendingPlugins'
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
  | 'errorMessage'
  | 'iconUrl'
  | 'feedUrl'
  | 'status'
  | 'streamId'
  | 'lastCheckedAt'
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
  samples?: Array<BasicWebDocument>;
};

export type TransientOrExistingNativeFeed = {
  transient?: Maybe<
    Pick<GqlTransientNativeFeed, 'url' | 'type' | 'description' | 'title'>
  >;
  existing?: Maybe<BasicNativeFeed>;
};

export type GqlFeedDiscoveryResponse = {
  document?: Maybe<FieldWrapper<GqlFeedDiscoveryDocument>>;
  errorMessage?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  failed: FieldWrapper<Scalars['Boolean']['output']>;
  fetchOptions: FieldWrapper<FetchOptions>;
  genericFeeds: Array<FieldWrapper<GqlTransientGenericFeed>>;
  nativeFeeds?: Maybe<Array<FieldWrapper<GqlTransientOrExistingNativeFeed>>>;
  /**   relatedFeeds: [NativeFeedGql] */
  websiteUrl: FieldWrapper<Scalars['String']['output']>;
};

export type ScrapedReadability = Pick<
  GqlScrapedReadability,
  | 'url'
  | 'content'
  | 'contentMime'
  | 'contentText'
  | 'date'
  | 'faviconUrl'
  | 'imageUrl'
  | 'title'
>;

export type ScrapedFeeds = {
  genericFeeds: Array<TransientGenericFeed>;
  nativeFeeds?: Maybe<Array<TransientOrExistingNativeFeed>>;
};

export type EmittedScrapeData = Pick<
  GqlEmittedScrapeData,
  'type' | 'raw' | 'text' | 'pixel'
> & { readability?: Maybe<ScrapedReadability>; feeds?: Maybe<ScrapedFeeds> };

export type ScrapedElement = { fragment: { boundingBox?: Maybe<Pick<GqlBoundingBox, 'h' | 'w' | 'x' | 'y'>>, xpath?: Maybe<Pick<GqlDomElementByXPath, 'value'>> }, data: Array<(
    Pick<GqlEmittedScrapeData, 'type' | 'raw' | 'text' | 'pixel'>
    & { readability?: Maybe<Pick<GqlScrapedReadability, 'url' | 'content' | 'contentMime' | 'contentText' | 'date' | 'faviconUrl' | 'imageUrl' | 'title'>>, feeds?: Maybe<{ genericFeeds: Array<(
        Pick<GqlTransientGenericFeed, 'feedUrl' | 'hash' | 'score' | 'count'>
        & { selectors: Pick<GqlSelectors, 'contextXPath' | 'linkXPath' | 'extendContext' | 'dateXPath' | 'paginationXPath' | 'dateIsStartOfEvent'>, samples: Array<Pick<GqlWebDocument, 'id' | 'title' | 'description' | 'url' | 'imageUrl' | 'createdAt'>> }
        )>, nativeFeeds?: Maybe<Array<{ transient?: Maybe<Pick<GqlTransientNativeFeed, 'url' | 'type' | 'description' | 'title'>>, existing?: Maybe<Pick<GqlNativeFeed, 'id' | 'title' | 'description' | 'domain' | 'imageUrl' | 'iconUrl' | 'websiteUrl' | 'feedUrl' | 'status' | 'lastCheckedAt' | 'errorMessage' | 'lastChangedAt' | 'streamId' | 'lat' | 'lon' | 'ownerId' | 'createdAt'>> }>> }> }
    )> };

export type ScrapeResponse = Pick<GqlScrapeResponse, 'url' | 'failed' | 'errorMessage'>
  & { debug: (
    Pick<GqlScrapeDebugResponse, 'console' | 'cookies' | 'contentType' | 'statusCode' | 'screenshot' | 'html'>
    & { metrics: Pick<GqlScrapeDebugTimes, 'queue' | 'render'>, viewport?: Maybe<Pick<GqlViewPort, 'width' | 'height'>> }
    ), elements: Array<ScrapedElement> }
  ;

export type FeedDiscoveryResult = Pick<
  GqlFeedDiscoveryResponse,
  'failed' | 'errorMessage' | 'websiteUrl'
> & {
  genericFeeds: Array<TransientGenericFeed>;
  fetchOptions: Pick<
    FetchOptions,
    'prerender' | 'websiteUrl' | 'prerenderWaitUntil'
  >;
  nativeFeeds?: Maybe<Array<TransientOrExistingNativeFeed>>;
  document?: Maybe<
    Pick<
      GqlFeedDiscoveryDocument,
      | 'mimeType'
      | 'htmlBody'
      | 'title'
      | 'url'
      | 'description'
      | 'language'
      | 'imageUrl'
      | 'favicon'
    >
  >;
};

export type FetchOptions = {
  prerender: FieldWrapper<Scalars['Boolean']['input']>;
  prerenderWaitUntil: FieldWrapper<GqlPuppeteerWaitUntil>;
  websiteUrl: FieldWrapper<Scalars['String']['input']>;
};

export type GenericFeed = Pick<
  GqlGenericFeed,
  'id' | 'feedUrl' | 'hash' | 'createdAt'
> & {
  specification: {
    selectors: Selectors;
    fetchOptions: Pick<
      FetchOptions,
      'prerender' | 'websiteUrl' | 'prerenderWaitUntil'
    >;
    refineOptions: Pick<GqlRefineOptions, 'filter'>;
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
  | 'filter'
  | 'plugins'
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
      | 'id'
      | 'acceptedTermsAndServices'
      | 'name'
      | 'notificationsStreamId'
      | 'purgeScheduledFor'
    > & {
      plugins: Array<Plugin>;
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

export type ApiUrls = Pick<GqlApiUrls, 'webToFeed' | 'webToPageChange'>;

export type Plugin = Pick<
  GqlPlugin,
  'id' | 'description' | 'state' | 'perProfile' | 'value'
>;
