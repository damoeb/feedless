import {
  GqlAuthentication,
  GqlCreateRepositoriesMutation,
  GqlCreateUserSecretMutation,
  GqlListPluginsQuery,
  GqlPlansQuery,
  GqlRemoteNativeFeedQuery,
  GqlRepositoryByIdQuery,
  GqlScrapedReadability,
  GqlScrapeQuery,
  GqlSelectors,
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
export type Session = GqlSessionQuery['session'];
export type UserSecret = GqlCreateUserSecretMutation['createUserSecret'];
export type FeedlessPlugin = GetElementType<GqlListPluginsQuery['plugins']>;
