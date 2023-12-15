import {
  FieldWrapper,
  GqlApiUrls,
  GqlArticle,
  GqlAuthentication,
  GqlBase64Data,
  GqlBoundingBox,
  GqlBucket,
  GqlDomElementByXPath,
  GqlEnclosure,
  GqlFeature,
  GqlFeatureBooleanValue,
  GqlFeatureIntValue,
  GqlFeedDiscoveryDocument,
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
  GqlRemoteOrExistingNativeFeed,
  GqlScrapeDebugResponse,
  GqlScrapeDebugTimes,
  GqlScrapedField,
  GqlScrapedFieldByTransformer,
  GqlScrapedReadability,
  GqlScrapedSingleFieldValue,
  GqlScrapeResponse,
  GqlSelectors,
  GqlSourceSubscription,
  GqlTextData,
  GqlTransientGenericFeed,
  GqlUser,
  GqlUserSecret,
  GqlViewPort,
  GqlWebDocument,
  Maybe,
  Scalars
} from '../../generated/graphql';

export type SourceSubscription = Pick<GqlSourceSubscription, 'id'>

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
>;
export type BasicEnclosure = Pick<
  GqlEnclosure,
  'size' | 'duration' | 'type' | 'url'
>;
export type TransientGenericFeed = GqlTransientGenericFeed;

export type TransientOrExistingNativeFeed = GqlRemoteOrExistingNativeFeed;

export type GqlFeedDiscoveryResponse = {
  document?: Maybe<FieldWrapper<GqlFeedDiscoveryDocument>>;
  errorMessage?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  failed: FieldWrapper<Scalars['Boolean']['output']>;
  fetchOptions: FieldWrapper<FetchOptions>;
  genericFeeds: Array<FieldWrapper<GqlTransientGenericFeed>>;
  nativeFeeds?: Maybe<Array<FieldWrapper<GqlRemoteOrExistingNativeFeed>>>;
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

export type ScrapedElement = { image?: Maybe<{ boundingBox: Pick<GqlBoundingBox, 'y' | 'x' | 'w' | 'h'>, data: Pick<GqlBase64Data, 'base64Data'> }>, selector?: Maybe<{ xpath: Pick<GqlDomElementByXPath, 'value'>, html?: Maybe<Pick<GqlTextData, 'data'>>, pixel?: Maybe<Pick<GqlBase64Data, 'base64Data'>>, text?: Maybe<Pick<GqlTextData, 'data'>>, fields?: Maybe<Array<(
      Pick<GqlScrapedField, 'name'>
      & { xpath?: Maybe<Pick<GqlDomElementByXPath, 'value'>>, transformer?: Maybe<Pick<GqlScrapedFieldByTransformer, 'internal' | 'external'>>, value?: Maybe<{ one?: Maybe<Pick<GqlScrapedSingleFieldValue, 'mimeType' | 'data'>>, many?: Maybe<Array<Pick<GqlScrapedSingleFieldValue, 'mimeType' | 'data'>>>, nested?: Maybe<Array<(
          Pick<GqlScrapedField, 'name'>
          & { xpath?: Maybe<Pick<GqlDomElementByXPath, 'value'>> }
          )>> }> }
      )>> }> };

export type ScrapeResponse = (
  Pick<GqlScrapeResponse, 'url' | 'failed' | 'errorMessage'>
  & { debug: (
    Pick<GqlScrapeDebugResponse, 'console' | 'cookies' | 'contentType' | 'statusCode' | 'screenshot' | 'html'>
    & { metrics: Pick<GqlScrapeDebugTimes, 'queue' | 'render'>, viewport?: Maybe<Pick<GqlViewPort, 'width' | 'height'>> }
    ), elements: Array<ScrapedElement> }
  );

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
  GqlWebDocument,
  | 'url'
  | 'title'
  | 'contentText'
  | 'contentRaw'
  | 'contentRawMime'
  // | 'publishedAt'
  | 'startingAt'
>;

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
