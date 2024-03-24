import {
  GqlAuthentication,
  GqlCreateSourceSubscriptionsMutation,
  GqlCreateUserSecretMutation,
  GqlListPluginsQuery,
  GqlPlansQuery,
  GqlProfileQuery,
  GqlRemoteNativeFeedQuery,
  GqlScrapedReadability,
  GqlScrapeQuery,
  GqlSelectors,
  GqlSourceSubscriptionByIdQuery,
  GqlWebDocumentByIdQuery,
} from '../../generated/graphql';

export type GetElementType<T extends any[]> = T extends (infer U)[] ? U : never;
export type FeedlessPluginExecution = GetElementType<
  GqlSourceSubscriptionByIdQuery['sourceSubscription']['plugins']
>['params'];
export type SubscriptionSource = GetElementType<
  GqlSourceSubscriptionByIdQuery['sourceSubscription']['sources']
>;
export type SourceSubscription = GetElementType<
  GqlCreateSourceSubscriptionsMutation['createSourceSubscriptions']
>;

export type WebDocument = GqlWebDocumentByIdQuery['webDocument'];

export type ActualAuthentication = Pick<GqlAuthentication, 'token' | 'corrId'>;

export type Selectors = Pick<
  GqlSelectors,
  | 'linkXPath'
  | 'extendContext'
  | 'dateXPath'
  | 'contextXPath'
  | 'dateIsStartOfEvent'
>;

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

export type ScrapedElement = GetElementType<ScrapeResponse['elements']>;
export type ScrapeResponse = GqlScrapeQuery['scrape'];
export type RemoteFeedItem = GetElementType<RemoteFeed['items']>;
export type RemoteFeed = GqlRemoteNativeFeedQuery['remoteNativeFeed'];
export type Feature = GetElementType<Plan['features']>;
export type Plan = GetElementType<GqlPlansQuery['plans']>;
export type Profile = GqlProfileQuery['profile'];
export type UserSecret = GqlCreateUserSecretMutation['createUserSecret'];
export type FeedlessPlugin = GetElementType<GqlListPluginsQuery['plugins']>;
