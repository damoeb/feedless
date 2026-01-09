import {
  GqlAnnotation,
  GqlAuthentication,
  GqlCreateUserSecretMutation,
  GqlFeatureGroupsQuery,
  GqlFullRecordByIdsQuery,
  GqlListPluginsQuery,
  GqlListProductsQuery,
  GqlListPublicRepositoriesQuery,
  GqlListRepositoriesQuery,
  GqlRecordByIdQuery,
  GqlRepositoryByIdQuery,
  GqlScrapeQuery,
  GqlSelectors,
  GqlServerSettingsQuery,
  GqlSessionQuery,
  GqlSourcesWithFlowByRepositoryQuery,
} from '../generated/graphql';

export type GetElementType<T extends any[]> = T extends (infer U)[] ? U : never;
export type RepositorySource = GetElementType<
  GqlRepositoryByIdQuery['repository']['sources']
>;
export type RepositoryWithFrequency = GetElementType<
  GqlListRepositoriesQuery['repositories']
>;
export type PublicRepository = GetElementType<
  GqlListPublicRepositoriesQuery['repositories']
>;
export type RepositoryFull = GqlRepositoryByIdQuery['repository'];
export type SourceFull = GetElementType<
  GqlSourcesWithFlowByRepositoryQuery['repository']['sources']
>;
export type Product = GetElementType<GqlListProductsQuery['products']>;

export type Record = GqlRecordByIdQuery['record'];
export type RecordFull = GetElementType<GqlFullRecordByIdsQuery['records']>;

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

export type ScrapedReadability = Record;
export type User = SessionResponse['user'];
// export type ScrapedOutput = GetElementType<ScrapeResponse['outputs']>;
export type ScrapeResponse = GqlScrapeQuery['scrape'];
export type FeatureGroup = GetElementType<
  GqlFeatureGroupsQuery['featureGroups']
>;
export type Feature = GetElementType<FeatureGroup['features']>;
export type SessionResponse = GqlSessionQuery['session'];
export type UserSecret = GqlCreateUserSecretMutation['createUserSecret'];
export type FeedlessPlugin = GetElementType<GqlListPluginsQuery['plugins']>;
export type LocalizedLicense =
  GqlServerSettingsQuery['serverSettings']['license'];

export type Annotation = Pick<GqlAnnotation, 'id'>;
