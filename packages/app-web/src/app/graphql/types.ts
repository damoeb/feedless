import {
  GqlAuthentication,
  GqlCreateRepositoriesMutation,
  GqlCreateUserSecretMutation,
  GqlFeatureGroupsQuery,
  GqlListPluginsQuery,
  GqlListProductsQuery,
  GqlListRepositoriesQuery,
  GqlPreviewFeedQuery,
  GqlRemoteNativeFeedQuery,
  GqlRepositoryByIdQuery,
  GqlScrapeQuery,
  GqlSelectors,
  GqlServerSettingsQuery,
  GqlSessionQuery,
  GqlWebDocumentByIdQuery,
} from '../../generated/graphql';

export type GetElementType<T extends any[]> = T extends (infer U)[] ? U : never;
export type RepositorySource = GetElementType<
  GqlRepositoryByIdQuery['repository']['sources']
>;
export type Repository = GetElementType<
  GqlListRepositoriesQuery['repositories']
>;
export type RepositoryFull = GqlRepositoryByIdQuery['repository'];
export type Product = GetElementType<GqlListProductsQuery['products']>;

export type WebDocument = GqlWebDocumentByIdQuery['webDocument'];

export type ActualAuthentication = Pick<GqlAuthentication, 'token' | 'corrId'>;

export type Selectors = Pick<
  GqlSelectors,
  | 'linkXPath'
  | 'extendContext'
  | 'dateXPath'
  | 'paginationXPath'
  | 'contextXPath'
  | 'dateIsStartOfEvent'
>;

export type ScrapedReadability = WebDocument;
export type User = Session['user'];
// export type ScrapedOutput = GetElementType<ScrapeResponse['outputs']>;
export type ScrapeResponse = GqlScrapeQuery['scrape'];
export type RemoteFeedItem = GetElementType<RemoteFeed['items']>;
export type FeedPreview = GqlPreviewFeedQuery['previewFeed'];
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
