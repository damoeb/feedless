import {
  GqlAuthentication,
  GqlCreateRepositoriesMutation,
  GqlCreateUserSecretMutation,
  GqlFeatureGroupsQuery,
  GqlListPluginsQuery,
  GqlListProductsQuery,
  GqlRemoteNativeFeedQuery,
  GqlRepositoryByIdQuery,
  GqlScrapedReadability,
  GqlScrapeQuery,
  GqlSelectors,
  GqlServerSettingsQuery,
  GqlSessionQuery,
  GqlWebDocumentByIdQuery,
} from '../../generated/graphql';

export type GetElementType<T extends any[]> = T extends (infer U)[] ? U : never;
export type FeedlessPluginExecution = GetElementType<
  GqlRepositoryByIdQuery['repository']['plugins']
>['params'];
export type SubscriptionSource = GetElementType<
  GqlRepositoryByIdQuery['repository']['sources']
>;
export type Repository = GetElementType<
  GqlCreateRepositoriesMutation['createRepositories']
>;
export type Product = GetElementType<GqlListProductsQuery['products']>;

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
export type User = Session['user'];
export type ScrapedElement = GetElementType<ScrapeResponse['elements']>;
export type ScrapeResponse = GqlScrapeQuery['scrape'];
export type RemoteFeedItem = GetElementType<RemoteFeed['items']>;
export type RemoteFeed = GqlRemoteNativeFeedQuery['remoteNativeFeed'];
export type FeatureGroup = GetElementType<
  GqlFeatureGroupsQuery['featureGroups']
>;
export type Feature = GetElementType<FeatureGroup['features']>;
export type Session = GqlSessionQuery['session'];
export type UserSecret = GqlCreateUserSecretMutation['createUserSecret'];
export type FeedlessPlugin = GetElementType<GqlListPluginsQuery['plugins']>;
export type LocalizedLicense =
  GqlServerSettingsQuery['serverSettings']['license'];
