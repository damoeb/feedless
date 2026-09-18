import gql from 'graphql-tag';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
export type MakeEmpty<T extends { [key: string]: unknown }, K extends keyof T> = { [_ in K]?: never };
export type Incremental<T> = T | { [P in keyof T]?: P extends ' $fragmentName' | '__typename' ? T[P] : never };
export type FieldWrapper<T> = T;
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: { input: string; output: string; }
  String: { input: string; output: string; }
  Boolean: { input: boolean; output: boolean; }
  Int: { input: number; output: number; }
  Float: { input: number; output: number; }
  /** An RFC-3339 compliant DateTime Scalar */
  DateTime: { input: any; output: any; }
  /** A JSON scalar */
  JSON: { input: any; output: any; }
  /** A 64-bit signed integer */
  Long: { input: any; output: any; }
  Upload: { input: any; output: any; }
};

export type GqlAgent = {
  addedAt: FieldWrapper<Scalars['Long']['output']>;
  name: FieldWrapper<Scalars['String']['output']>;
  openInstance: FieldWrapper<Scalars['Boolean']['output']>;
  ownerId: FieldWrapper<Scalars['ID']['output']>;
  secretKeyId: FieldWrapper<Scalars['ID']['output']>;
  version: FieldWrapper<Scalars['String']['output']>;
};

export type GqlAgentAuthentication = {
  token: FieldWrapper<Scalars['String']['output']>;
};

export type GqlAgentByGroup = {
  group: Scalars['String']['input'];
};

export type GqlAgentEvent = {
  authentication?: Maybe<FieldWrapper<GqlAgentAuthentication>>;
  callbackId?: Maybe<FieldWrapper<Scalars['ID']['output']>>;
  corrId?: Maybe<FieldWrapper<Scalars['ID']['output']>>;
  scrape?: Maybe<FieldWrapper<GqlSource>>;
};

export type GqlAgentInput = {
  secretKeyId: Scalars['ID']['input'];
};

export type GqlAnnotation = {
  downVote?: Maybe<FieldWrapper<GqlBoolAnnotation>>;
  flag?: Maybe<FieldWrapper<GqlBoolAnnotation>>;
  id: FieldWrapper<Scalars['ID']['output']>;
  text?: Maybe<FieldWrapper<GqlTextAnnotation>>;
  upVote?: Maybe<FieldWrapper<GqlBoolAnnotation>>;
};

export type GqlAnnotationWhereInput = {
  document?: InputMaybe<GqlRecordUniqueWhereInput>;
  repository?: InputMaybe<GqlRepositoryUniqueWhereInput>;
};

export type GqlAnnotationWhereUniqueInput = {
  id: Scalars['String']['input'];
};

export type GqlAnnotations = {
  downVotes: FieldWrapper<Scalars['Int']['output']>;
  upVotes: FieldWrapper<Scalars['Int']['output']>;
  votes?: Maybe<Array<FieldWrapper<GqlAnnotation>>>;
};

export type GqlAttachment = {
  duration?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  id: FieldWrapper<Scalars['ID']['output']>;
  size?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  type: FieldWrapper<Scalars['String']['output']>;
  url: FieldWrapper<Scalars['String']['output']>;
};

export type GqlAttachmentWhereUniqueInput = {
  id: Scalars['ID']['input'];
};

export enum GqlAuthType {
  MailToken = 'mailToken',
  Oauth = 'oauth',
  Password = 'password'
}

export type GqlAuthUserInput = {
  email: Scalars['String']['input'];
  secretKey: Scalars['String']['input'];
};

export type GqlAuthViaMailInput = {
  allowCreate: Scalars['Boolean']['input'];
  email: Scalars['String']['input'];
  osInfo: Scalars['String']['input'];
  product: GqlVertical;
};

export type GqlAuthentication = {
  corrId: FieldWrapper<Scalars['ID']['output']>;
  token: FieldWrapper<Scalars['String']['output']>;
};

export type GqlBillingsWhereInput = {
  id?: InputMaybe<GqlStringFilterInput>;
};

export type GqlBoolAnnotation = {
  value: FieldWrapper<Scalars['Boolean']['output']>;
};

export type GqlBoolUpdateOperationsInput = {
  set: Scalars['Boolean']['input'];
};

export type GqlBoundingBox = {
  h: FieldWrapper<Scalars['Int']['output']>;
  w: FieldWrapper<Scalars['Int']['output']>;
  x: FieldWrapper<Scalars['Int']['output']>;
  y: FieldWrapper<Scalars['Int']['output']>;
};

export type GqlBoundingBoxInput = {
  h: Scalars['Int']['input'];
  w: Scalars['Int']['input'];
  x: Scalars['Int']['input'];
  y: Scalars['Int']['input'];
};

export type GqlBuildInfo = {
  commit: FieldWrapper<Scalars['String']['output']>;
  date: FieldWrapper<Scalars['Long']['output']>;
};

export type GqlCompareBy = {
  field: FieldWrapper<GqlRecordField>;
  fragmentNameRef?: Maybe<FieldWrapper<Scalars['String']['output']>>;
};

export type GqlCompareByInput = {
  field: GqlRecordField;
  fragmentNameRef?: InputMaybe<Scalars['String']['input']>;
};

export type GqlCompositeFieldFilterParams = {
  content?: Maybe<FieldWrapper<GqlStringFilterParams>>;
  index?: Maybe<FieldWrapper<GqlNumericalFilterParams>>;
  link?: Maybe<FieldWrapper<GqlStringFilterParams>>;
  title?: Maybe<FieldWrapper<GqlStringFilterParams>>;
};

export type GqlCompositeFieldFilterParamsInput = {
  content?: InputMaybe<GqlStringFilterParamsInput>;
  index?: InputMaybe<GqlNumericalFilterParamsInput>;
  link?: InputMaybe<GqlStringFilterParamsInput>;
  title?: InputMaybe<GqlStringFilterParamsInput>;
};

export type GqlCompositeFilterParams = {
  exclude?: Maybe<FieldWrapper<GqlCompositeFieldFilterParams>>;
  include?: Maybe<FieldWrapper<GqlCompositeFieldFilterParams>>;
};

export type GqlCompositeFilterParamsInput = {
  exclude?: InputMaybe<GqlCompositeFieldFilterParamsInput>;
  include?: InputMaybe<GqlCompositeFieldFilterParamsInput>;
};

export type GqlConditionalTag = {
  filter: FieldWrapper<GqlCompositeFieldFilterParams>;
  tag: FieldWrapper<Scalars['String']['output']>;
};

export type GqlConditionalTagInput = {
  filter: GqlCompositeFieldFilterParamsInput;
  tag: Scalars['String']['input'];
};

export type GqlConfirmAuthCodeInput = {
  code: Scalars['String']['input'];
  otpId: Scalars['ID']['input'];
};

export type GqlConfirmCode = {
  length: FieldWrapper<Scalars['Int']['output']>;
  otpId: FieldWrapper<Scalars['ID']['output']>;
};

export type GqlConfirmCodeInput = {
  otpId: Scalars['ID']['input'];
  value: Scalars['String']['input'];
};

export type GqlConnectedApp = {
  app: FieldWrapper<Scalars['String']['output']>;
  authorized: FieldWrapper<Scalars['Boolean']['output']>;
  authorizedAt?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  id: FieldWrapper<Scalars['ID']['output']>;
};

export type GqlCountRepositoriesInput = {
  product: GqlVertical;
};

export type GqlCreateAnnotationInput = {
  annotation: GqlOneOfAnnotationInput;
  where: GqlAnnotationWhereInput;
};

export type GqlCreateAttachmentFieldsInput = {
  data: Scalars['Upload']['input'];
  name: Scalars['String']['input'];
};

export type GqlCreateAttachmentInput = {
  attachment: GqlCreateAttachmentFieldsInput;
  where: GqlRecordUniqueWhereInput;
};

export type GqlCreateRecordInput = {
  id?: InputMaybe<Scalars['String']['input']>;
  parent?: InputMaybe<GqlRecordUniqueWhereInput>;
  publishedAt: Scalars['Long']['input'];
  rawBase64?: InputMaybe<Scalars['String']['input']>;
  rawMimeType?: InputMaybe<Scalars['String']['input']>;
  repositoryId: GqlRepositoryUniqueWhereInput;
  tags?: InputMaybe<Array<Scalars['String']['input']>>;
  text?: InputMaybe<Scalars['String']['input']>;
  title: Scalars['String']['input'];
  url: Scalars['String']['input'];
};

export type GqlCreateUserSecretInput = {
  name: Scalars['String']['input'];
};

export type GqlCursor = {
  page: Scalars['Int']['input'];
  pageSize?: InputMaybe<Scalars['Int']['input']>;
};

export type GqlDomActionSelect = {
  element: FieldWrapper<GqlDomElementByXPath>;
  selectValue: FieldWrapper<Scalars['String']['output']>;
};

export type GqlDomActionSelectInput = {
  element: GqlDomElementByXPathInput;
  selectValue: Scalars['String']['input'];
};

export type GqlDomActionType = {
  element: FieldWrapper<GqlDomElementByXPath>;
  typeValue: FieldWrapper<Scalars['String']['output']>;
};

export type GqlDomActionTypeInput = {
  element: GqlDomElementByXPathInput;
  typeValue: Scalars['String']['input'];
};

export type GqlDomElement = {
  element?: Maybe<FieldWrapper<GqlDomElementByNameOrXPath>>;
  position?: Maybe<FieldWrapper<GqlXyPosition>>;
};

export type GqlDomElementByName = {
  value: FieldWrapper<Scalars['String']['output']>;
};

export type GqlDomElementByNameInput = {
  value: Scalars['String']['input'];
};

export type GqlDomElementByNameOrXPath = {
  name?: Maybe<FieldWrapper<GqlDomElementByName>>;
  xpath?: Maybe<FieldWrapper<GqlDomElementByXPath>>;
};

export type GqlDomElementByNameOrXPathInput = {
  name?: InputMaybe<GqlDomElementByNameInput>;
  xpath?: InputMaybe<GqlDomElementByXPathInput>;
};

export type GqlDomElementByXPath = {
  value: FieldWrapper<Scalars['String']['output']>;
};

export type GqlDomElementByXPathInput = {
  value: Scalars['String']['input'];
};

export type GqlDomElementInput = {
  element?: InputMaybe<GqlDomElementByNameOrXPathInput>;
  position?: InputMaybe<GqlXyPositionInput>;
};

export type GqlDomExtract = {
  emit: Array<FieldWrapper<GqlScrapeEmit>>;
  extract?: Maybe<Array<FieldWrapper<GqlDomExtract>>>;
  fragmentName: FieldWrapper<Scalars['String']['output']>;
  max?: Maybe<FieldWrapper<Scalars['Int']['output']>>;
  uniqueBy: FieldWrapper<GqlScrapeEmit>;
  xpath: FieldWrapper<GqlDomElementByXPath>;
};

export type GqlDomExtractInput = {
  emit: Array<GqlScrapeEmit>;
  extract?: InputMaybe<Array<GqlDomExtractInput>>;
  fragmentName: Scalars['String']['input'];
  max?: InputMaybe<Scalars['Int']['input']>;
  uniqueBy: GqlScrapeEmit;
  xpath: GqlDomElementByXPathInput;
};

export type GqlDateFilter = {
  gt?: InputMaybe<Scalars['Long']['input']>;
  lt?: InputMaybe<Scalars['Long']['input']>;
};

export type GqlDateWhereInput = {
  value: Scalars['DateTime']['input'];
};

export type GqlDatesWhereInput = {
  after?: InputMaybe<Scalars['Long']['input']>;
  before?: InputMaybe<Scalars['Long']['input']>;
  inFuture?: InputMaybe<Scalars['Boolean']['input']>;
};

export type GqlDeleteAnnotationInput = {
  where: GqlAnnotationWhereUniqueInput;
};

export type GqlDeleteAttachmentInput = {
  where: GqlAttachmentWhereUniqueInput;
};

export type GqlDeleteFeatureValueInput = {
  id: Scalars['ID']['input'];
};

export type GqlDeleteRecordsInput = {
  where: GqlRecordsWhereInput;
};

export type GqlDeleteUserSecretInput = {
  where: GqlUserSecretWhereInput;
};

export type GqlDiffRecordsParams = {
  compareBy: FieldWrapper<GqlCompareBy>;
  inlineDiffImage?: Maybe<FieldWrapper<Scalars['Boolean']['output']>>;
  inlineLatestImage?: Maybe<FieldWrapper<Scalars['Boolean']['output']>>;
  inlinePreviousImage?: Maybe<FieldWrapper<Scalars['Boolean']['output']>>;
  nextItemMinIncrement: FieldWrapper<Scalars['Float']['output']>;
};

export type GqlDiffRecordsParamsInput = {
  compareBy: GqlCompareByInput;
  inlineDiffImage?: InputMaybe<Scalars['Boolean']['input']>;
  inlineLatestImage?: InputMaybe<Scalars['Boolean']['input']>;
  inlinePreviousImage?: InputMaybe<Scalars['Boolean']['input']>;
  nextItemMinIncrement: Scalars['Float']['input'];
};

export enum GqlExtendContentOptions {
  Next = 'NEXT',
  None = 'NONE',
  Previous = 'PREVIOUS',
  PreviousAndNext = 'PREVIOUS_AND_NEXT'
}

export type GqlFeature = {
  id: FieldWrapper<Scalars['String']['output']>;
  name: FieldWrapper<GqlFeatureName>;
  value: FieldWrapper<GqlFeatureValue>;
};

export type GqlFeatureBooleanValue = {
  value: FieldWrapper<Scalars['Boolean']['output']>;
};

export type GqlFeatureBooleanValueInput = {
  value: Scalars['Boolean']['input'];
};

export type GqlFeatureGroup = {
  features: Array<FieldWrapper<GqlFeature>>;
  id: FieldWrapper<Scalars['ID']['output']>;
  name: FieldWrapper<Scalars['String']['output']>;
  parentId?: Maybe<FieldWrapper<Scalars['ID']['output']>>;
};

export type GqlFeatureGroupWhereInput = {
  id?: InputMaybe<GqlStringFilterInput>;
};

export type GqlFeatureIntValue = {
  value: FieldWrapper<Scalars['Long']['output']>;
};

export type GqlFeatureIntValueInput = {
  value: Scalars['Long']['input'];
};

export enum GqlFeatureName {
  CanActivatePlan = 'canActivatePlan',
  CanJoinPlanWaitList = 'canJoinPlanWaitList',
  Plugins = 'plugins',
  PublicRepository = 'publicRepository',
  RefreshRateInMinutesLowerLimit = 'refreshRateInMinutesLowerLimit',
  RepositoriesMaxCountTotalInt = 'repositoriesMaxCountTotalInt',
  RepositoryCapacityLowerLimitInt = 'repositoryCapacityLowerLimitInt',
  RepositoryCapacityUpperLimitInt = 'repositoryCapacityUpperLimitInt',
  RepositoryRetentionMaxDaysLowerLimitInt = 'repositoryRetentionMaxDaysLowerLimitInt',
  RequestPerMinuteUpperLimitInt = 'requestPerMinuteUpperLimitInt',
  ScrapeRequestActionMaxCount = 'scrapeRequestActionMaxCount',
  ScrapeRequestMaxCountPerSource = 'scrapeRequestMaxCountPerSource',
  ScrapeRequestTimeoutMsec = 'scrapeRequestTimeoutMsec',
  ScrapeSourceMaxCountActive = 'scrapeSourceMaxCountActive',
  ScrapeSourceMaxCountTotal = 'scrapeSourceMaxCountTotal',
  SourceMaxCountPerRepositoryInt = 'sourceMaxCountPerRepositoryInt'
}

export type GqlFeatureValue = {
  boolVal?: Maybe<FieldWrapper<GqlFeatureBooleanValue>>;
  id: FieldWrapper<Scalars['ID']['output']>;
  numVal?: Maybe<FieldWrapper<GqlFeatureIntValue>>;
};

export type GqlFeatureValueInput = {
  boolVal?: InputMaybe<GqlFeatureBooleanValueInput>;
  numVal?: InputMaybe<GqlFeatureIntValueInput>;
};

export type GqlFeedParams = {
  generic?: Maybe<FieldWrapper<GqlSelectors>>;
};

export type GqlFeedParamsInput = {
  generic?: InputMaybe<GqlSelectorsInput>;
};

export enum GqlFeedlessPlugins {
  OrgFeedlessConditionalTag = 'org_feedless_conditional_tag',
  OrgFeedlessDetectMedia = 'org_feedless_detect_media',
  OrgFeedlessDiffRecords = 'org_feedless_diff_records',
  OrgFeedlessEventReport = 'org_feedless_event_report',
  OrgFeedlessFeed = 'org_feedless_feed',
  OrgFeedlessFeeds = 'org_feedless_feeds',
  OrgFeedlessFilter = 'org_feedless_filter',
  OrgFeedlessFulltext = 'org_feedless_fulltext',
  OrgFeedlessPrivacy = 'org_feedless_privacy'
}

export type GqlFetchActionDebugResponse = {
  console: Array<FieldWrapper<Scalars['String']['output']>>;
  contentType?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  cookies?: Maybe<Array<FieldWrapper<Scalars['String']['output']>>>;
  corrId: FieldWrapper<Scalars['String']['output']>;
  html?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  network: Array<FieldWrapper<GqlNetworkRequest>>;
  prerendered: FieldWrapper<Scalars['Boolean']['output']>;
  screenshot?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  statusCode?: Maybe<FieldWrapper<Scalars['Int']['output']>>;
  url: FieldWrapper<Scalars['String']['output']>;
  viewport: FieldWrapper<GqlViewPort>;
};

export type GqlFetchActionDebugResponseInput = {
  console: Array<Scalars['String']['input']>;
  contentType?: InputMaybe<Scalars['String']['input']>;
  cookies?: InputMaybe<Array<Scalars['String']['input']>>;
  corrId: Scalars['String']['input'];
  html?: InputMaybe<Scalars['String']['input']>;
  network: Array<GqlNetworkRequestInput>;
  prerendered: Scalars['Boolean']['input'];
  screenshot?: InputMaybe<Scalars['String']['input']>;
  statusCode?: InputMaybe<Scalars['Int']['input']>;
  url: Scalars['String']['input'];
  viewport: GqlViewPortInput;
};

export type GqlFloatUpdateOperationsInput = {
  set: Scalars['Float']['input'];
};

export type GqlFulltextPluginParams = {
  inheritParams: FieldWrapper<Scalars['Boolean']['output']>;
  readability: FieldWrapper<Scalars['Boolean']['output']>;
  summary: FieldWrapper<Scalars['Boolean']['output']>;
};

export type GqlFulltextPluginParamsInput = {
  inheritParams: Scalars['Boolean']['input'];
  onErrorRemove?: InputMaybe<Scalars['Boolean']['input']>;
  readability: Scalars['Boolean']['input'];
  summary: Scalars['Boolean']['input'];
};

export type GqlFulltextQueryFilter = {
  query: Scalars['String']['input'];
};

export type GqlGeoPoint = {
  lat: FieldWrapper<Scalars['Float']['output']>;
  lng: FieldWrapper<Scalars['Float']['output']>;
};

export type GqlGeoPointInput = {
  lat: Scalars['Float']['input'];
  lng: Scalars['Float']['input'];
};

export type GqlGeoPointWhereInput = {
  near?: InputMaybe<GqlGeoPointWhereNearInput>;
  within?: InputMaybe<GqlGeoPointWhereWithinInput>;
};

export type GqlGeoPointWhereNearInput = {
  distanceKm: Scalars['Float']['input'];
  point: GqlGeoPointInput;
};

export type GqlGeoPointWhereWithinInput = {
  nw: GqlGeoPointInput;
  se: GqlGeoPointInput;
};

export type GqlGroupAssignment = {
  id: FieldWrapper<Scalars['ID']['output']>;
  name: FieldWrapper<Scalars['String']['output']>;
  role: FieldWrapper<GqlRole>;
};

export type GqlHarvest = {
  finishedAt?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  itemsAdded: FieldWrapper<Scalars['Int']['output']>;
  itemsIgnored: FieldWrapper<Scalars['Int']['output']>;
  logs: FieldWrapper<Scalars['String']['output']>;
  ok: FieldWrapper<Scalars['Boolean']['output']>;
  startedAt: FieldWrapper<Scalars['Long']['output']>;
};

export type GqlHttpFetch = {
  get: FieldWrapper<GqlHttpGetRequest>;
};

export type GqlHttpFetchInput = {
  get: GqlHttpGetRequestInput;
};

export type GqlHttpFetchResponse = {
  data: FieldWrapper<Scalars['String']['output']>;
  debug: FieldWrapper<GqlFetchActionDebugResponse>;
};

export type GqlHttpFetchResponseInput = {
  data: Scalars['String']['input'];
  debug: GqlFetchActionDebugResponseInput;
};

export type GqlHttpGetRequest = {
  additionalWaitSec?: Maybe<FieldWrapper<Scalars['Int']['output']>>;
  forcePrerender?: Maybe<FieldWrapper<Scalars['Boolean']['output']>>;
  language?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  timeout?: Maybe<FieldWrapper<Scalars['Int']['output']>>;
  url: FieldWrapper<GqlStringLiteralOrVariable>;
  viewport?: Maybe<FieldWrapper<GqlViewPort>>;
  waitUntil?: Maybe<FieldWrapper<GqlPuppeteerWaitUntil>>;
};

export type GqlHttpGetRequestInput = {
  additionalWaitSec?: InputMaybe<Scalars['Int']['input']>;
  agents?: InputMaybe<Array<GqlAgentInput>>;
  forcePrerender?: InputMaybe<Scalars['Boolean']['input']>;
  language?: InputMaybe<Scalars['String']['input']>;
  timeout?: InputMaybe<Scalars['Int']['input']>;
  url: GqlStringLiteralOrVariableInput;
  viewport?: InputMaybe<GqlViewPortInput>;
  waitUntil?: InputMaybe<GqlPuppeteerWaitUntil>;
};

export enum GqlIntervalUnit {
  Month = 'MONTH',
  Week = 'WEEK'
}

export type GqlItemFilterParams = {
  composite?: Maybe<FieldWrapper<GqlCompositeFilterParams>>;
  expression?: Maybe<FieldWrapper<Scalars['String']['output']>>;
};

export type GqlItemFilterParamsInput = {
  composite?: InputMaybe<GqlCompositeFilterParamsInput>;
  expression?: InputMaybe<Scalars['String']['input']>;
};

export type GqlJsonData = {
  jsonData: FieldWrapper<Scalars['String']['output']>;
  jsonSchema: FieldWrapper<Scalars['String']['output']>;
};

export type GqlJsonDataInput = {
  jsonData: Scalars['String']['input'];
  jsonSchema: Scalars['String']['input'];
};

export type GqlLicense = {
  createdAt: FieldWrapper<Scalars['Long']['output']>;
  email: FieldWrapper<Scalars['String']['output']>;
  name: FieldWrapper<Scalars['String']['output']>;
  scope: FieldWrapper<GqlVertical>;
  version: FieldWrapper<Scalars['Int']['output']>;
};

export type GqlLocalizedLicense = {
  data?: Maybe<FieldWrapper<GqlLicense>>;
  isLocated: FieldWrapper<Scalars['Boolean']['output']>;
  isTrial: FieldWrapper<Scalars['Boolean']['output']>;
  isValid: FieldWrapper<Scalars['Boolean']['output']>;
  trialUntil?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
};

export type GqlLogStatement = {
  message: FieldWrapper<Scalars['String']['output']>;
  time: FieldWrapper<Scalars['Long']['output']>;
};

export type GqlLogStatementInput = {
  message: Scalars['String']['input'];
  time: Scalars['Long']['input'];
};

export type GqlLoginResponse = {
  token: FieldWrapper<Scalars['String']['output']>;
  user: FieldWrapper<GqlUser>;
};

export type GqlLongUpdateOperationsInput = {
  set: Scalars['Long']['input'];
};

export type GqlMimeData = {
  data: FieldWrapper<Scalars['String']['output']>;
  mimeType: FieldWrapper<Scalars['String']['output']>;
};

export type GqlMimeDataInput = {
  data: Scalars['String']['input'];
  mimeType: Scalars['String']['input'];
};

export type GqlMutation = {
  authAnonymous: FieldWrapper<GqlAuthentication>;
  authConfirmCode: FieldWrapper<GqlAuthentication>;
  authUser: FieldWrapper<GqlAuthentication>;
  authenticateWithCodeViaMail: FieldWrapper<GqlConfirmCode>;
  createAnnotation: FieldWrapper<GqlAnnotation>;
  createAttachment: FieldWrapper<GqlAttachment>;
  createRecords: Array<FieldWrapper<GqlRecord>>;
  createReport: FieldWrapper<GqlReport>;
  createRepositories: Array<FieldWrapper<GqlRepository>>;
  createUserSecret: FieldWrapper<GqlUserSecret>;
  deleteAnnotation: FieldWrapper<Scalars['Boolean']['output']>;
  deleteAttachment: FieldWrapper<Scalars['Boolean']['output']>;
  deleteConnectedApp?: Maybe<FieldWrapper<Scalars['Boolean']['output']>>;
  deleteFeatureValue: FieldWrapper<Scalars['Boolean']['output']>;
  deleteRecords: FieldWrapper<Scalars['Boolean']['output']>;
  deleteReport: FieldWrapper<Scalars['Boolean']['output']>;
  deleteRepository: FieldWrapper<Scalars['Boolean']['output']>;
  deleteUserSecret: FieldWrapper<Scalars['Boolean']['output']>;
  issueAnonymousFeedToken: FieldWrapper<GqlAuthentication>;
  logout?: Maybe<FieldWrapper<Scalars['Boolean']['output']>>;
  submitAgentData: FieldWrapper<Scalars['Boolean']['output']>;
  updateConnectedApp?: Maybe<FieldWrapper<Scalars['Boolean']['output']>>;
  updateCurrentUser: FieldWrapper<Scalars['Boolean']['output']>;
  updateFeatureValue: FieldWrapper<Scalars['Boolean']['output']>;
  updateLicense: FieldWrapper<GqlLocalizedLicense>;
  updateRecord: FieldWrapper<Scalars['Boolean']['output']>;
  updateRepository: FieldWrapper<Scalars['Boolean']['output']>;
  upsertOrder: FieldWrapper<GqlOrder>;
};


export type GqlMutationAuthConfirmCodeArgs = {
  data: GqlConfirmAuthCodeInput;
};


export type GqlMutationAuthUserArgs = {
  data: GqlAuthUserInput;
};


export type GqlMutationAuthenticateWithCodeViaMailArgs = {
  data: GqlAuthViaMailInput;
};


export type GqlMutationCreateAnnotationArgs = {
  data: GqlCreateAnnotationInput;
};


export type GqlMutationCreateAttachmentArgs = {
  data: GqlCreateAttachmentInput;
};


export type GqlMutationCreateRecordsArgs = {
  records?: InputMaybe<Array<GqlCreateRecordInput>>;
  upload?: InputMaybe<Scalars['Upload']['input']>;
};


export type GqlMutationCreateReportArgs = {
  repositoryId: Scalars['ID']['input'];
  segmentation: GqlSegmentInput;
};


export type GqlMutationCreateRepositoriesArgs = {
  data: Array<GqlRepositoryCreateInput>;
};


export type GqlMutationCreateUserSecretArgs = {
  data: GqlCreateUserSecretInput;
};


export type GqlMutationDeleteAnnotationArgs = {
  data: GqlDeleteAnnotationInput;
};


export type GqlMutationDeleteAttachmentArgs = {
  data: GqlDeleteAttachmentInput;
};


export type GqlMutationDeleteConnectedAppArgs = {
  id: Scalars['String']['input'];
};


export type GqlMutationDeleteFeatureValueArgs = {
  data: GqlDeleteFeatureValueInput;
};


export type GqlMutationDeleteRecordsArgs = {
  data: GqlDeleteRecordsInput;
};


export type GqlMutationDeleteReportArgs = {
  reportId: Scalars['ID']['input'];
};


export type GqlMutationDeleteRepositoryArgs = {
  data: GqlRepositoryUniqueWhereInput;
};


export type GqlMutationDeleteUserSecretArgs = {
  data: GqlDeleteUserSecretInput;
};


export type GqlMutationIssueAnonymousFeedTokenArgs = {
  url: Scalars['String']['input'];
};


export type GqlMutationSubmitAgentDataArgs = {
  data: GqlSubmitAgentDataInput;
};


export type GqlMutationUpdateConnectedAppArgs = {
  authorize: Scalars['Boolean']['input'];
  id: Scalars['String']['input'];
};


export type GqlMutationUpdateCurrentUserArgs = {
  data: GqlUpdateCurrentUserInput;
};


export type GqlMutationUpdateFeatureValueArgs = {
  data: GqlUpdateFeatureValueInput;
};


export type GqlMutationUpdateLicenseArgs = {
  data: GqlUpdateLicenseInput;
};


export type GqlMutationUpdateRecordArgs = {
  data: GqlUpdateRecordInput;
};


export type GqlMutationUpdateRepositoryArgs = {
  data: GqlRepositoryUpdateInput;
};


export type GqlMutationUpsertOrderArgs = {
  data: GqlUpsertOrderInput;
};

export type GqlNativeFeed = {
  autoRelease: FieldWrapper<Scalars['Boolean']['output']>;
  createdAt: FieldWrapper<Scalars['Long']['output']>;
  description?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  domain?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  errorMessage?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  feedUrl: FieldWrapper<Scalars['String']['output']>;
  harvestRateFixed: FieldWrapper<Scalars['Boolean']['output']>;
  harvestRateMinutes?: Maybe<FieldWrapper<Scalars['Int']['output']>>;
  iconUrl?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  imageUrl?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  lastChangedAt?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  lastCheckedAt?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  lat?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  lng?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  ownerId: FieldWrapper<Scalars['ID']['output']>;
  status: FieldWrapper<GqlNativeFeedStatus>;
  streamId: FieldWrapper<Scalars['ID']['output']>;
  title: FieldWrapper<Scalars['String']['output']>;
  /**   nextRefreshAt: Long */
  visibility: FieldWrapper<GqlVisibility>;
  websiteUrl?: Maybe<FieldWrapper<Scalars['String']['output']>>;
};

export enum GqlNativeFeedStatus {
  Defective = 'defective',
  Disabled = 'disabled',
  NeverFetched = 'never_fetched',
  NotFound = 'not_found',
  Ok = 'ok',
  ServiceUnavailable = 'service_unavailable'
}

export type GqlNetworkRequest = {
  requestHeaders?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  requestPostData?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  responseBody?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  responseHeaders?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  responseSize?: Maybe<FieldWrapper<Scalars['Int']['output']>>;
  url?: Maybe<FieldWrapper<Scalars['String']['output']>>;
};

export type GqlNetworkRequestInput = {
  requestHeaders?: InputMaybe<Scalars['String']['input']>;
  requestPostData?: InputMaybe<Scalars['String']['input']>;
  responseBody?: InputMaybe<Scalars['String']['input']>;
  responseHeaders?: InputMaybe<Scalars['String']['input']>;
  responseSize?: InputMaybe<Scalars['Int']['input']>;
  url?: InputMaybe<Scalars['String']['input']>;
};

export type GqlNullableBoolUpdateOperationsInput = {
  set?: InputMaybe<Scalars['Boolean']['input']>;
};

export type GqlNullableIntUpdateOperationsInput = {
  set?: InputMaybe<Scalars['Int']['input']>;
};

export type GqlNullableLongUpdateOperationsInput = {
  set?: InputMaybe<Scalars['Long']['input']>;
};

export type GqlNullableStringUpdateOperationsInput = {
  set?: InputMaybe<Scalars['String']['input']>;
};

export type GqlNullableUpdateFlowInput = {
  set?: InputMaybe<GqlScrapeFlowInput>;
};

export type GqlNullableUpdateGeoPointInput = {
  set?: InputMaybe<GqlGeoPointInput>;
};

export type GqlNullableUpdateOperationsInput = {
  assignNull: Scalars['Boolean']['input'];
};

export enum GqlNumberFilterOperator {
  Eq = 'eq',
  Gt = 'gt',
  Lt = 'lt'
}

export type GqlNumericalFilterParams = {
  operator: FieldWrapper<GqlNumberFilterOperator>;
  value: FieldWrapper<Scalars['Int']['output']>;
};

export type GqlNumericalFilterParamsInput = {
  operator: GqlNumberFilterOperator;
  value: Scalars['Int']['input'];
};

export type GqlOneOfAnnotationInput = {
  downVote?: InputMaybe<GqlBoolUpdateOperationsInput>;
  flag?: InputMaybe<GqlBoolUpdateOperationsInput>;
  text?: InputMaybe<GqlTextAnnotationInput>;
  upVote?: InputMaybe<GqlBoolUpdateOperationsInput>;
};

export type GqlOrder = {
  createdAt: FieldWrapper<Scalars['Long']['output']>;
  id: FieldWrapper<Scalars['ID']['output']>;
  invoiceRecipientEmail: FieldWrapper<Scalars['String']['output']>;
  invoiceRecipientName: FieldWrapper<Scalars['String']['output']>;
  isOffer: FieldWrapper<Scalars['Boolean']['output']>;
  isOfferRejected: FieldWrapper<Scalars['Boolean']['output']>;
  isPaid: FieldWrapper<Scalars['Boolean']['output']>;
  licenses?: Maybe<Array<FieldWrapper<GqlLicense>>>;
  offerValidTo?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  paidAt?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  paymentDueTo?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  paymentMethod?: Maybe<FieldWrapper<GqlPaymentMethod>>;
  price: FieldWrapper<Scalars['Float']['output']>;
  product: FieldWrapper<GqlProduct>;
  productId: FieldWrapper<Scalars['ID']['output']>;
  user?: Maybe<FieldWrapper<GqlUser>>;
  userId: FieldWrapper<Scalars['ID']['output']>;
};

export type GqlOrderCreateInput = {
  invoiceRecipientEmail: Scalars['String']['input'];
  invoiceRecipientName: Scalars['String']['input'];
  isOffer: Scalars['Boolean']['input'];
  overwritePrice: Scalars['Float']['input'];
  paymentMethod: GqlPaymentMethod;
  productId: Scalars['ID']['input'];
  targetGroup: GqlProductTargetGroup;
  user: GqlUserCreateOrConnectInput;
};

export type GqlOrderUpdateInput = {
  isOffer?: InputMaybe<GqlBoolUpdateOperationsInput>;
  isRejected?: InputMaybe<GqlBoolUpdateOperationsInput>;
  price?: InputMaybe<GqlFloatUpdateOperationsInput>;
};

export type GqlOrderWhereUniqueInput = {
  id: Scalars['ID']['input'];
};

export type GqlOrdersInput = {
  cursor: GqlCursor;
  where?: InputMaybe<GqlBillingsWhereInput>;
};

export type GqlOsInfo = {
  arch: Scalars['String']['input'];
  platform: Scalars['String']['input'];
};

export type GqlPaginated = {
  pagination: FieldWrapper<GqlPagination>;
};

export type GqlPagination = {
  isEmpty: FieldWrapper<Scalars['Boolean']['output']>;
  isFirst: FieldWrapper<Scalars['Boolean']['output']>;
  isLast: FieldWrapper<Scalars['Boolean']['output']>;
  page: FieldWrapper<Scalars['Int']['output']>;
  pageSize: FieldWrapper<Scalars['Int']['output']>;
};

export enum GqlPaymentMethod {
  CreditCard = 'CreditCard'
}

export type GqlPlan = {
  id: FieldWrapper<Scalars['ID']['output']>;
  product: FieldWrapper<GqlProduct>;
  productId: FieldWrapper<Scalars['ID']['output']>;
  recurringYearly: FieldWrapper<Scalars['Boolean']['output']>;
  startedAt?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  terminatedAt?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
};

export type GqlPlugin = {
  id: FieldWrapper<Scalars['ID']['output']>;
  listed: FieldWrapper<Scalars['Boolean']['output']>;
  name: FieldWrapper<Scalars['String']['output']>;
  type: FieldWrapper<GqlPluginType>;
};

export type GqlPluginExecution = {
  params: FieldWrapper<GqlPluginExecutionParams>;
  pluginId: FieldWrapper<Scalars['ID']['output']>;
};

export type GqlPluginExecutionInput = {
  params: GqlPluginExecutionParamsInput;
  pluginId: Scalars['ID']['input'];
};

export type GqlPluginExecutionParams = {
  jsonData?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  org_feedless_conditional_tag?: Maybe<Array<FieldWrapper<GqlConditionalTag>>>;
  org_feedless_diff_records?: Maybe<FieldWrapper<GqlDiffRecordsParams>>;
  org_feedless_feed?: Maybe<FieldWrapper<GqlFeedParams>>;
  org_feedless_filter?: Maybe<Array<FieldWrapper<GqlItemFilterParams>>>;
  org_feedless_fulltext?: Maybe<FieldWrapper<GqlFulltextPluginParams>>;
};

export type GqlPluginExecutionParamsInput = {
  jsonData?: InputMaybe<Scalars['String']['input']>;
  org_feedless_conditional_tag?: InputMaybe<Array<GqlConditionalTagInput>>;
  org_feedless_diff_records?: InputMaybe<GqlDiffRecordsParamsInput>;
  org_feedless_feed?: InputMaybe<GqlFeedParamsInput>;
  org_feedless_filter?: InputMaybe<Array<GqlItemFilterParamsInput>>;
  org_feedless_fulltext?: InputMaybe<GqlFulltextPluginParamsInput>;
};

export enum GqlPluginType {
  Entity = 'entity',
  Fragment = 'fragment'
}

export type GqlPricedProduct = {
  description?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  id: FieldWrapper<Scalars['ID']['output']>;
  inStock: FieldWrapper<Scalars['Int']['output']>;
  price: FieldWrapper<Scalars['Float']['output']>;
  recurringInterval: FieldWrapper<GqlRecurringPaymentInterval>;
};

export type GqlProduct = {
  description: FieldWrapper<Scalars['String']['output']>;
  enterprise: FieldWrapper<Scalars['Boolean']['output']>;
  featureGroup?: Maybe<FieldWrapper<GqlFeatureGroup>>;
  featureGroupId?: Maybe<FieldWrapper<Scalars['ID']['output']>>;
  id: FieldWrapper<Scalars['ID']['output']>;
  individual: FieldWrapper<Scalars['Boolean']['output']>;
  isCloud: FieldWrapper<Scalars['Boolean']['output']>;
  name: FieldWrapper<Scalars['String']['output']>;
  other: FieldWrapper<Scalars['Boolean']['output']>;
  partOf?: Maybe<FieldWrapper<GqlVertical>>;
  prices: Array<FieldWrapper<GqlPricedProduct>>;
};

export enum GqlProductTargetGroup {
  Eneterprise = 'eneterprise',
  Individual = 'individual',
  Other = 'other'
}

export type GqlProductsUniqueWhereInput = {
  id: Scalars['ID']['input'];
};

export type GqlProductsWhereInput = {
  id?: InputMaybe<GqlStringFilterInput>;
  vertical?: InputMaybe<GqlVertical>;
};

export enum GqlProfileName {
  Dev = 'dev',
  Saas = 'saas',
  SelfHosted = 'selfHosted'
}

export enum GqlPuppeteerWaitUntil {
  Domcontentloaded = 'domcontentloaded',
  Load = 'load',
  Networkidle0 = 'networkidle0',
  Networkidle2 = 'networkidle2'
}

export type GqlQuery = {
  agents: Array<FieldWrapper<GqlAgent>>;
  connectedApp: FieldWrapper<GqlConnectedApp>;
  countRepositories: FieldWrapper<Scalars['Int']['output']>;
  featureGroups: Array<FieldWrapper<GqlFeatureGroup>>;
  orders: Array<FieldWrapper<GqlOrder>>;
  plans: Array<FieldWrapper<GqlPlan>>;
  plugins: Array<FieldWrapper<GqlPlugin>>;
  products: Array<FieldWrapper<GqlProduct>>;
  record: FieldWrapper<GqlRecord>;
  records: Array<FieldWrapper<GqlRecord>>;
  recordsFrequency?: Maybe<Array<FieldWrapper<GqlRecordFrequency>>>;
  repositories: Array<FieldWrapper<GqlRepository>>;
  repository: FieldWrapper<GqlRepository>;
  scrape: FieldWrapper<GqlScrapeResponse>;
  serverSettings: FieldWrapper<GqlServerSettings>;
  session: FieldWrapper<GqlSession>;
};


export type GqlQueryConnectedAppArgs = {
  id: Scalars['String']['input'];
};


export type GqlQueryCountRepositoriesArgs = {
  data: GqlCountRepositoriesInput;
};


export type GqlQueryFeatureGroupsArgs = {
  inherit: Scalars['Boolean']['input'];
  where: GqlFeatureGroupWhereInput;
};


export type GqlQueryOrdersArgs = {
  data: GqlOrdersInput;
};


export type GqlQueryPlansArgs = {
  cursor: GqlCursor;
};


export type GqlQueryProductsArgs = {
  data: GqlProductsWhereInput;
};


export type GqlQueryRecordArgs = {
  data: GqlRecordWhereInput;
};


export type GqlQueryRecordsArgs = {
  data: GqlRecordsInput;
};


export type GqlQueryRecordsFrequencyArgs = {
  groupBy: GqlRecordDateField;
  where: GqlRecordsWhereInput;
};


export type GqlQueryRepositoriesArgs = {
  data: GqlRepositoriesInput;
};


export type GqlQueryRepositoryArgs = {
  data: GqlRepositoryWhereInput;
};


export type GqlQueryScrapeArgs = {
  data: GqlSourceInput;
};


export type GqlQueryServerSettingsArgs = {
  data: GqlServerSettingsContextInput;
};

export type GqlRecord = {
  annotations?: Maybe<FieldWrapper<GqlAnnotations>>;
  attachments?: Maybe<Array<FieldWrapper<GqlAttachment>>>;
  createdAt: FieldWrapper<Scalars['Long']['output']>;
  html?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  id: FieldWrapper<Scalars['ID']['output']>;
  imageUrl?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  latLng?: Maybe<FieldWrapper<GqlGeoPoint>>;
  publishedAt: FieldWrapper<Scalars['Long']['output']>;
  rawBase64?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  rawMimeType?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  startingAt?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  tags?: Maybe<Array<FieldWrapper<Scalars['String']['output']>>>;
  text?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  title?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  updatedAt: FieldWrapper<Scalars['Long']['output']>;
  url: FieldWrapper<Scalars['String']['output']>;
};

export enum GqlRecordDateField {
  CreatedAt = 'createdAt',
  PublishedAt = 'publishedAt',
  StartingAt = 'startingAt'
}

export type GqlRecordDateFieldUpdateOperationsInput = {
  set: GqlRecordDateField;
};

export enum GqlRecordField {
  Markup = 'markup',
  Pixel = 'pixel',
  Text = 'text'
}

export type GqlRecordFrequency = {
  count: FieldWrapper<Scalars['Int']['output']>;
  group: FieldWrapper<Scalars['Long']['output']>;
};

export type GqlRecordOrderByInput = {
  startedAt?: InputMaybe<GqlSortOrder>;
};

export type GqlRecordUniqueWhereInput = {
  id: Scalars['ID']['input'];
};

export type GqlRecordUpdateInput = {
  rawBase64?: InputMaybe<GqlNullableStringUpdateOperationsInput>;
  rawMimeType?: InputMaybe<GqlNullableStringUpdateOperationsInput>;
  tags?: InputMaybe<GqlStringArrayUpdateOperationsInput>;
  text?: InputMaybe<GqlStringUpdateOperationsInput>;
  title?: InputMaybe<GqlStringUpdateOperationsInput>;
  url?: InputMaybe<GqlStringUpdateOperationsInput>;
};

export type GqlRecordWhereInput = {
  where: GqlRecordUniqueWhereInput;
};

export type GqlRecordsInput = {
  cursor: GqlCursor;
  orderBy?: InputMaybe<GqlRecordOrderByInput>;
  where: GqlRecordsWhereInput;
};

export type GqlRecordsWhereInput = {
  createdAt?: InputMaybe<GqlDatesWhereInput>;
  id?: InputMaybe<GqlStringFilterInput>;
  latLng?: InputMaybe<GqlGeoPointWhereInput>;
  publishedAt?: InputMaybe<GqlDatesWhereInput>;
  repository: GqlRepositoryUniqueWhereInput;
  source?: InputMaybe<GqlSourceUniqueWhereInput>;
  startedAt?: InputMaybe<GqlDatesWhereInput>;
  tags?: InputMaybe<GqlStringFilterInput>;
  updatedAt?: InputMaybe<GqlDatesWhereInput>;
};

export enum GqlRecurringPaymentInterval {
  Monthly = 'monthly',
  Yearly = 'yearly'
}

export type GqlRegisterAgentInput = {
  connectionId: Scalars['String']['input'];
  name: Scalars['String']['input'];
  os: GqlOsInfo;
  secretKey: GqlSecretKey;
  version: Scalars['String']['input'];
};

export type GqlRemoteNativeFeed = {
  author?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  description?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  expired?: Maybe<FieldWrapper<Scalars['Boolean']['output']>>;
  feedUrl: FieldWrapper<Scalars['String']['output']>;
  items: Array<FieldWrapper<GqlRecord>>;
  language?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  nextPageUrls?: Maybe<Array<FieldWrapper<Scalars['String']['output']>>>;
  publishedAt: FieldWrapper<Scalars['Long']['output']>;
  tags?: Maybe<Array<FieldWrapper<Scalars['String']['output']>>>;
  title: FieldWrapper<Scalars['String']['output']>;
  websiteUrl?: Maybe<FieldWrapper<Scalars['String']['output']>>;
};

export type GqlReport = {
  createdAt: FieldWrapper<Scalars['Long']['output']>;
  id: FieldWrapper<Scalars['ID']['output']>;
};

export type GqlReportEmailRecipientInput = {
  email: Scalars['String']['input'];
  name: Scalars['String']['input'];
};

export type GqlReportRecipientInput = {
  email: GqlReportEmailRecipientInput;
};

export type GqlRepositoriesInput = {
  capability?: InputMaybe<Scalars['String']['input']>;
  cursor: GqlCursor;
  orderBy?: InputMaybe<GqlRecordOrderByInput>;
  where?: InputMaybe<GqlRepositoriesWhereInput>;
};

export type GqlRepositoriesWhereInput = {
  product?: InputMaybe<GqlVerticalFilter>;
  tags?: InputMaybe<GqlStringArrayFilter>;
  text?: InputMaybe<GqlFulltextQueryFilter>;
  visibility?: InputMaybe<GqlVisibilityFilter>;
};

export type GqlRepository = {
  annotations?: Maybe<FieldWrapper<GqlAnnotations>>;
  archived: FieldWrapper<Scalars['Boolean']['output']>;
  createdAt: FieldWrapper<Scalars['Long']['output']>;
  currentUserIsOwner: FieldWrapper<Scalars['Boolean']['output']>;
  description: FieldWrapper<Scalars['String']['output']>;
  disabledFrom?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  documentCount: FieldWrapper<Scalars['Long']['output']>;
  frequency?: Maybe<Array<FieldWrapper<GqlRecordFrequency>>>;
  id: FieldWrapper<Scalars['ID']['output']>;
  lastUpdatedAt: FieldWrapper<Scalars['Long']['output']>;
  nextUpdateAt?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  ownerId: FieldWrapper<Scalars['ID']['output']>;
  plugins?: Maybe<Array<FieldWrapper<GqlPluginExecution>>>;
  product: FieldWrapper<GqlVertical>;
  pullsPerMonth: FieldWrapper<Scalars['Int']['output']>;
  pushNotificationsEnabled: FieldWrapper<Scalars['Boolean']['output']>;
  refreshCron: FieldWrapper<Scalars['String']['output']>;
  retention: FieldWrapper<GqlRetention>;
  shareKey: FieldWrapper<Scalars['String']['output']>;
  sources?: Maybe<Array<FieldWrapper<GqlSource>>>;
  sourcesCount: FieldWrapper<Scalars['Int']['output']>;
  sourcesCountWithProblems: FieldWrapper<Scalars['Int']['output']>;
  sunset?: Maybe<FieldWrapper<GqlSunSetPolicy>>;
  tags: Array<FieldWrapper<Scalars['String']['output']>>;
  title: FieldWrapper<Scalars['String']['output']>;
  visibility: FieldWrapper<GqlVisibility>;
};


export type GqlRepositoryFrequencyArgs = {
  groupBy: GqlRecordDateField;
};


export type GqlRepositorySourcesArgs = {
  cursor: GqlCursor;
  order?: InputMaybe<Array<GqlSourceOrderByInput>>;
  where?: InputMaybe<GqlSourcesWhereInput>;
};

export type GqlRepositoryCreateInput = {
  additionalSinks?: InputMaybe<Array<GqlWebhookOrEmailInput>>;
  agents?: InputMaybe<Array<GqlAgentByGroup>>;
  description: Scalars['String']['input'];
  plugins?: InputMaybe<Array<GqlPluginExecutionInput>>;
  product: GqlVertical;
  pushNotificationsMuted?: InputMaybe<Scalars['Boolean']['input']>;
  refreshCron?: InputMaybe<Scalars['String']['input']>;
  retention?: InputMaybe<GqlRetentionInput>;
  sources: Array<GqlSourceInput>;
  sunset?: InputMaybe<GqlSunSetPolicyInput>;
  title: Scalars['String']['input'];
  visibility?: InputMaybe<GqlVisibility>;
  withShareKey?: InputMaybe<Scalars['Boolean']['input']>;
};

export type GqlRepositoryUniqueWhereInput = {
  id: Scalars['ID']['input'];
};

export type GqlRepositoryUpdateDataInput = {
  description?: InputMaybe<GqlNullableStringUpdateOperationsInput>;
  nextUpdateAt?: InputMaybe<GqlNullableLongUpdateOperationsInput>;
  plugins?: InputMaybe<Array<GqlPluginExecutionInput>>;
  pushNotificationsMuted?: InputMaybe<GqlBoolUpdateOperationsInput>;
  refreshCron?: InputMaybe<GqlNullableStringUpdateOperationsInput>;
  retention?: InputMaybe<GqlRetentionUpdateInput>;
  sources?: InputMaybe<GqlSourcesUpdateInput>;
  title?: InputMaybe<GqlStringUpdateOperationsInput>;
  visibility?: InputMaybe<GqlVisibilityUpdateOperationsInput>;
};

export type GqlRepositoryUpdateInput = {
  data: GqlRepositoryUpdateDataInput;
  where: GqlRepositoryUniqueWhereInput;
};

export type GqlRepositoryWhereInput = {
  where: GqlRepositoryUniqueWhereInput;
};

export type GqlRequestHeader = {
  name?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  value?: Maybe<FieldWrapper<Scalars['String']['output']>>;
};

export type GqlRequestHeaderInput = {
  name?: InputMaybe<Scalars['String']['input']>;
  value?: InputMaybe<Scalars['String']['input']>;
};

export type GqlRetention = {
  ageReferenceField?: Maybe<FieldWrapper<GqlRecordDateField>>;
  maxAgeDays?: Maybe<FieldWrapper<Scalars['Int']['output']>>;
  maxCapacity?: Maybe<FieldWrapper<Scalars['Int']['output']>>;
};

export type GqlRetentionInput = {
  ageReferenceField?: InputMaybe<GqlRecordDateField>;
  maxAgeDays?: InputMaybe<Scalars['Int']['input']>;
  maxCapacity?: InputMaybe<Scalars['Int']['input']>;
};

export type GqlRetentionUpdateInput = {
  ageReferenceField?: InputMaybe<GqlRecordDateFieldUpdateOperationsInput>;
  maxAgeDays?: InputMaybe<GqlNullableIntUpdateOperationsInput>;
  maxCapacity?: InputMaybe<GqlNullableIntUpdateOperationsInput>;
};

export enum GqlRole {
  Editor = 'editor',
  Owner = 'owner',
  Viewer = 'viewer'
}

export type GqlScheduledRefreshRate = {
  expression: Scalars['String']['input'];
};

export type GqlScheduledSegmentInput = {
  interval: GqlIntervalUnit;
  startingAt: Scalars['Long']['input'];
};

export type GqlScrapeAction = {
  click?: Maybe<FieldWrapper<GqlDomElement>>;
  execute?: Maybe<FieldWrapper<GqlPluginExecution>>;
  extract?: Maybe<FieldWrapper<GqlScrapeExtract>>;
  fetch?: Maybe<FieldWrapper<GqlHttpFetch>>;
  header?: Maybe<FieldWrapper<GqlRequestHeader>>;
  purge?: Maybe<FieldWrapper<GqlDomElementByXPath>>;
  select?: Maybe<FieldWrapper<GqlDomActionSelect>>;
  type?: Maybe<FieldWrapper<GqlDomActionType>>;
  waitFor?: Maybe<FieldWrapper<GqlWaitForAction>>;
};

export type GqlScrapeActionInput = {
  click?: InputMaybe<GqlDomElementInput>;
  execute?: InputMaybe<GqlPluginExecutionInput>;
  extract?: InputMaybe<GqlScrapeExtractInput>;
  fetch?: InputMaybe<GqlHttpFetchInput>;
  header?: InputMaybe<GqlRequestHeaderInput>;
  purge?: InputMaybe<GqlDomElementByXPathInput>;
  select?: InputMaybe<GqlDomActionSelectInput>;
  type?: InputMaybe<GqlDomActionTypeInput>;
  waitFor?: InputMaybe<GqlWaitForActionInput>;
};

export type GqlScrapeActionResponse = {
  extract?: Maybe<FieldWrapper<GqlScrapeExtractResponse>>;
  fetch?: Maybe<FieldWrapper<GqlHttpFetchResponse>>;
};

export type GqlScrapeActionResponseInput = {
  extract?: InputMaybe<GqlScrapeExtractResponseInput>;
  fetch?: InputMaybe<GqlHttpFetchResponseInput>;
};

export type GqlScrapeBoundingBox = {
  boundingBox: FieldWrapper<GqlBoundingBox>;
};

export type GqlScrapeBoundingBoxInput = {
  boundingBox: GqlBoundingBoxInput;
};

export enum GqlScrapeEmit {
  Date = 'date',
  Html = 'html',
  Pixel = 'pixel',
  Text = 'text'
}

export type GqlScrapeExtract = {
  fragmentName: FieldWrapper<Scalars['String']['output']>;
  imageBased?: Maybe<FieldWrapper<GqlScrapeBoundingBox>>;
  selectorBased?: Maybe<FieldWrapper<GqlDomExtract>>;
};

export type GqlScrapeExtractFragment = {
  data?: Maybe<FieldWrapper<GqlMimeData>>;
  extracts?: Maybe<Array<FieldWrapper<GqlScrapeExtractResponse>>>;
  html?: Maybe<FieldWrapper<GqlTextData>>;
  text?: Maybe<FieldWrapper<GqlTextData>>;
  uniqueBy: FieldWrapper<GqlScrapeExtractFragmentPart>;
};

export type GqlScrapeExtractFragmentInput = {
  data?: InputMaybe<GqlMimeDataInput>;
  extracts?: InputMaybe<Array<GqlScrapeExtractResponseInput>>;
  html?: InputMaybe<GqlTextDataInput>;
  text?: InputMaybe<GqlTextDataInput>;
  uniqueBy: GqlScrapeExtractFragmentPart;
};

export enum GqlScrapeExtractFragmentPart {
  Data = 'data',
  Html = 'html',
  Text = 'text'
}

export type GqlScrapeExtractInput = {
  fragmentName: Scalars['String']['input'];
  imageBased?: InputMaybe<GqlScrapeBoundingBoxInput>;
  selectorBased?: InputMaybe<GqlDomExtractInput>;
};

export type GqlScrapeExtractResponse = {
  feeds?: Maybe<FieldWrapper<GqlScrapedFeeds>>;
  fragmentName: FieldWrapper<Scalars['String']['output']>;
  fragments?: Maybe<Array<FieldWrapper<GqlScrapeExtractFragment>>>;
  items?: Maybe<Array<FieldWrapper<GqlRecord>>>;
};

export type GqlScrapeExtractResponseInput = {
  fragmentName: Scalars['String']['input'];
  fragments: Array<GqlScrapeExtractFragmentInput>;
};

export type GqlScrapeFlow = {
  sequence: Array<FieldWrapper<GqlScrapeAction>>;
};

export type GqlScrapeFlowInput = {
  sequence: Array<GqlScrapeActionInput>;
};

export type GqlScrapeOutputResponse = {
  index: FieldWrapper<Scalars['Int']['output']>;
  response: FieldWrapper<GqlScrapeActionResponse>;
};

export type GqlScrapeOutputResponseInput = {
  index: Scalars['Int']['input'];
  response: GqlScrapeActionResponseInput;
};

export type GqlScrapeResponse = {
  errorMessage?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  logs: Array<FieldWrapper<GqlLogStatement>>;
  ok: FieldWrapper<Scalars['Boolean']['output']>;
  outputs: Array<FieldWrapper<GqlScrapeOutputResponse>>;
};

export type GqlScrapeResponseInput = {
  errorMessage?: InputMaybe<Scalars['String']['input']>;
  logs: Array<GqlLogStatementInput>;
  ok: Scalars['Boolean']['input'];
  outputs: Array<GqlScrapeOutputResponseInput>;
};

export type GqlScrapedElementMeta = {
  description?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  favicon?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  htmlBody?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  imageUrl?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  language?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  mimeType?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  title?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  url?: Maybe<FieldWrapper<Scalars['String']['output']>>;
};

export type GqlScrapedFeeds = {
  genericFeeds: Array<FieldWrapper<GqlTransientGenericFeed>>;
  nativeFeeds?: Maybe<Array<FieldWrapper<GqlRemoteNativeFeed>>>;
};

export type GqlSecretKey = {
  email: Scalars['String']['input'];
  secretKey: Scalars['String']['input'];
};

export type GqlSegmentInput = {
  recipient: GqlReportRecipientInput;
  report: GqlSegmentReportInput;
  what: GqlSegmentRecordsWhereInput;
  when: GqlTimeSegmentInput;
};

export type GqlSegmentRecordsWhereInput = {
  latLng?: InputMaybe<GqlGeoPointWhereInput>;
  maxSize?: InputMaybe<Scalars['Int']['input']>;
  tags?: InputMaybe<GqlStringFilterInput>;
};

export type GqlSegmentReportInput = {
  plugin: GqlPluginExecutionInput;
};

export type GqlSelectors = {
  contextXPath: FieldWrapper<Scalars['String']['output']>;
  dateIsStartOfEvent: FieldWrapper<Scalars['Boolean']['output']>;
  dateXPath: FieldWrapper<Scalars['String']['output']>;
  extendContext: FieldWrapper<GqlExtendContentOptions>;
  linkXPath: FieldWrapper<Scalars['String']['output']>;
  paginationXPath: FieldWrapper<Scalars['String']['output']>;
};

export type GqlSelectorsInput = {
  contextXPath: Scalars['String']['input'];
  dateIsStartOfEvent: Scalars['Boolean']['input'];
  dateXPath: Scalars['String']['input'];
  extendContext: GqlExtendContentOptions;
  linkXPath: Scalars['String']['input'];
  paginationXPath: Scalars['String']['input'];
};

export type GqlServerSettings = {
  auth: Array<FieldWrapper<GqlAuthType>>;
  build: FieldWrapper<GqlBuildInfo>;
  license?: Maybe<FieldWrapper<GqlLocalizedLicense>>;
  profiles: Array<FieldWrapper<GqlProfileName>>;
  version: FieldWrapper<Scalars['String']['output']>;
};

export type GqlServerSettingsContextInput = {
  host: Scalars['String']['input'];
  product: GqlVertical;
};

export type GqlSession = {
  isAnonymous: FieldWrapper<Scalars['Boolean']['output']>;
  isLoggedIn: FieldWrapper<Scalars['Boolean']['output']>;
  user?: Maybe<FieldWrapper<GqlUser>>;
  userId?: Maybe<FieldWrapper<Scalars['ID']['output']>>;
};

export enum GqlSortField {
  PublishedAt = 'publishedAt'
}

export enum GqlSortOrder {
  Asc = 'asc',
  Desc = 'desc'
}

export type GqlSource = {
  disabled?: Maybe<FieldWrapper<Scalars['Boolean']['output']>>;
  flow: FieldWrapper<GqlScrapeFlow>;
  harvests?: Maybe<Array<FieldWrapper<GqlHarvest>>>;
  id: FieldWrapper<Scalars['ID']['output']>;
  lastErrorMessage?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  lastRecordsRetrieved?: Maybe<FieldWrapper<Scalars['Int']['output']>>;
  lastRefreshedAt?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  latLng?: Maybe<FieldWrapper<GqlGeoPoint>>;
  recordCount?: Maybe<FieldWrapper<Scalars['Int']['output']>>;
  tags?: Maybe<Array<FieldWrapper<Scalars['String']['output']>>>;
  title: FieldWrapper<Scalars['String']['output']>;
};

export type GqlSourceInput = {
  draft?: InputMaybe<Scalars['Boolean']['input']>;
  flow: GqlScrapeFlowInput;
  id?: InputMaybe<Scalars['ID']['input']>;
  lastErrorMessage?: InputMaybe<Scalars['String']['input']>;
  latLng?: InputMaybe<GqlGeoPointInput>;
  tags?: InputMaybe<Array<Scalars['String']['input']>>;
  title: Scalars['String']['input'];
};

export type GqlSourceOrderByInput = {
  lastRecordsRetrieved?: InputMaybe<GqlSortOrder>;
  lastRefreshedAt?: InputMaybe<GqlSortOrder>;
  title?: InputMaybe<GqlSortOrder>;
};

export type GqlSourceUniqueWhereInput = {
  id: Scalars['ID']['input'];
};

export type GqlSourceUpdateDataInput = {
  disabled?: InputMaybe<GqlBoolUpdateOperationsInput>;
  flow?: InputMaybe<GqlNullableUpdateFlowInput>;
  latLng?: InputMaybe<GqlNullableUpdateGeoPointInput>;
  tags?: InputMaybe<GqlStringArrayUpdateOperationsInput>;
  title?: InputMaybe<GqlStringUpdateOperationsInput>;
};

export type GqlSourceUpdateInput = {
  data: GqlSourceUpdateDataInput;
  where: GqlSourceUniqueWhereInput;
};

export type GqlSourcesUpdateInput = {
  add?: InputMaybe<Array<GqlSourceInput>>;
  remove?: InputMaybe<Array<Scalars['ID']['input']>>;
  update?: InputMaybe<Array<GqlSourceUpdateInput>>;
};

export type GqlSourcesWhereInput = {
  disabled?: InputMaybe<Scalars['Boolean']['input']>;
  id?: InputMaybe<GqlStringFilterInput>;
  latLng?: InputMaybe<GqlGeoPointWhereInput>;
  like?: InputMaybe<Scalars['String']['input']>;
};

export type GqlStoriesWhereInput = {
  best: Scalars['Boolean']['input'];
  newest: Scalars['Boolean']['input'];
};

export type GqlStringArrayFilter = {
  every?: InputMaybe<Array<Scalars['String']['input']>>;
  some?: InputMaybe<Array<Scalars['String']['input']>>;
};

export type GqlStringArrayUpdateOperationsInput = {
  set: Array<Scalars['String']['input']>;
};

export type GqlStringFilterInput = {
  eq?: InputMaybe<Scalars['String']['input']>;
  in?: InputMaybe<Array<Scalars['String']['input']>>;
};

export enum GqlStringFilterOperator {
  Contains = 'contains',
  EndsWith = 'endsWith',
  Matches = 'matches',
  StartsWidth = 'startsWidth'
}

export type GqlStringFilterParams = {
  operator: FieldWrapper<GqlStringFilterOperator>;
  value: FieldWrapper<Scalars['String']['output']>;
};

export type GqlStringFilterParamsInput = {
  operator: GqlStringFilterOperator;
  value: Scalars['String']['input'];
};

export type GqlStringLiteralOrVariable = {
  literal?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  variable?: Maybe<FieldWrapper<Scalars['String']['output']>>;
};

export type GqlStringLiteralOrVariableInput = {
  literal?: InputMaybe<Scalars['String']['input']>;
  variable?: InputMaybe<Scalars['String']['input']>;
};

export type GqlStringUpdateOperationsInput = {
  set: Scalars['String']['input'];
};

export type GqlSubmitAgentDataInput = {
  callbackId: Scalars['ID']['input'];
  corrId: Scalars['ID']['input'];
  scrapeResponse: GqlScrapeResponseInput;
};

export type GqlSubscription = {
  agents: Array<FieldWrapper<GqlAgent>>;
  registerAgent: FieldWrapper<GqlAgentEvent>;
};


export type GqlSubscriptionRegisterAgentArgs = {
  data: GqlRegisterAgentInput;
};

export type GqlSunSetPolicy = {
  afterSnapshots?: Maybe<FieldWrapper<Scalars['Int']['output']>>;
  afterTimestamp?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
};

export type GqlSunSetPolicyInput = {
  afterSnapshots?: InputMaybe<Scalars['Int']['input']>;
  afterTimestamp?: InputMaybe<Scalars['Long']['input']>;
};

export type GqlTextAnnotation = {
  fromChar: FieldWrapper<Scalars['Int']['output']>;
  toChar: FieldWrapper<Scalars['Int']['output']>;
};

export type GqlTextAnnotationInput = {
  document: GqlCreateRecordInput;
  fromChar: Scalars['Int']['input'];
  toChar: Scalars['Int']['input'];
};

export type GqlTextData = {
  data: FieldWrapper<Scalars['String']['output']>;
};

export type GqlTextDataInput = {
  data: Scalars['String']['input'];
};

export type GqlTimeSegmentInput = {
  scheduled: GqlScheduledSegmentInput;
};

export type GqlTransientGenericFeed = {
  count: FieldWrapper<Scalars['Int']['output']>;
  hash: FieldWrapper<Scalars['String']['output']>;
  score: FieldWrapper<Scalars['Float']['output']>;
  selectors: FieldWrapper<GqlSelectors>;
};

export type GqlUpdateCurrentUserInput = {
  acceptedTermsAndServices?: InputMaybe<GqlBoolUpdateOperationsInput>;
  country?: InputMaybe<GqlStringUpdateOperationsInput>;
  dateFormat?: InputMaybe<GqlStringUpdateOperationsInput>;
  email?: InputMaybe<GqlStringUpdateOperationsInput>;
  firstName?: InputMaybe<GqlStringUpdateOperationsInput>;
  lastName?: InputMaybe<GqlStringUpdateOperationsInput>;
  notificationsLastViewedAt?: InputMaybe<GqlLongUpdateOperationsInput>;
  plan?: InputMaybe<GqlStringUpdateOperationsInput>;
  purgeScheduledFor?: InputMaybe<GqlNullableUpdateOperationsInput>;
  timeFormat?: InputMaybe<GqlStringUpdateOperationsInput>;
};

export type GqlUpdateFeatureValueInput = {
  id: Scalars['ID']['input'];
  value: GqlFeatureValueInput;
};

export type GqlUpdateLicenseInput = {
  licenseRaw: Scalars['String']['input'];
};

export type GqlUpdateRecordInput = {
  data: GqlRecordUpdateInput;
  where: GqlRecordUniqueWhereInput;
};

export type GqlUpsertOrderInput = {
  create?: InputMaybe<GqlOrderCreateInput>;
  update?: InputMaybe<GqlOrderUpdateInput>;
  where?: InputMaybe<GqlOrderWhereUniqueInput>;
};

export type GqlUser = {
  connectedApps: Array<Maybe<FieldWrapper<GqlConnectedApp>>>;
  country?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  createdAt: FieldWrapper<Scalars['Long']['output']>;
  email: FieldWrapper<Scalars['String']['output']>;
  emailValidated: FieldWrapper<Scalars['Boolean']['output']>;
  features: Array<FieldWrapper<GqlFeature>>;
  firstName?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  groups: Array<FieldWrapper<GqlGroupAssignment>>;
  hasAcceptedTerms: FieldWrapper<Scalars['Boolean']['output']>;
  hasCompletedSignup: FieldWrapper<Scalars['Boolean']['output']>;
  id: FieldWrapper<Scalars['ID']['output']>;
  lastName?: Maybe<FieldWrapper<Scalars['String']['output']>>;
  notificationRepositoryId?: Maybe<FieldWrapper<Scalars['ID']['output']>>;
  notificationsLastViewedAt?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  purgeScheduledFor?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  secrets: Array<FieldWrapper<GqlUserSecret>>;
};

export type GqlUserCreateInput = {
  country: Scalars['String']['input'];
  email: Scalars['String']['input'];
  firstName: Scalars['String']['input'];
  hasAcceptedTerms: Scalars['Boolean']['input'];
  lastName: Scalars['String']['input'];
};

export type GqlUserCreateOrConnectInput = {
  connect?: InputMaybe<GqlUserWhereUniqueInput>;
  create?: InputMaybe<GqlUserCreateInput>;
};

export type GqlUserSecret = {
  id: FieldWrapper<Scalars['ID']['output']>;
  lastUsed?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  name: FieldWrapper<Scalars['String']['output']>;
  type: FieldWrapper<GqlUserSecretType>;
  validUntil?: Maybe<FieldWrapper<Scalars['Long']['output']>>;
  value: FieldWrapper<Scalars['String']['output']>;
  valueMasked: FieldWrapper<Scalars['Boolean']['output']>;
};

export enum GqlUserSecretType {
  Jwt = 'Jwt',
  SecretKey = 'SecretKey'
}

export type GqlUserSecretWhereInput = {
  eq: Scalars['ID']['input'];
};

export type GqlUserWhereUniqueInput = {
  id: Scalars['String']['input'];
};

export enum GqlVertical {
  All = 'all',
  FeedDump = 'feedDump',
  Feedless = 'feedless',
  PageChangeTracker = 'pageChangeTracker',
  Reader = 'reader',
  RssProxy = 'rssProxy',
  UntoldNotes = 'untoldNotes',
  Upcoming = 'upcoming',
  VisualDiff = 'visualDiff'
}

export type GqlVerticalFilter = {
  eq?: InputMaybe<GqlVertical>;
  in?: InputMaybe<Array<GqlVertical>>;
};

export type GqlViewPort = {
  height: FieldWrapper<Scalars['Int']['output']>;
  isLandscape: FieldWrapper<Scalars['Boolean']['output']>;
  isMobile: FieldWrapper<Scalars['Boolean']['output']>;
  width: FieldWrapper<Scalars['Int']['output']>;
};

export type GqlViewPortInput = {
  height: Scalars['Int']['input'];
  isLandscape: Scalars['Boolean']['input'];
  isMobile: Scalars['Boolean']['input'];
  width: Scalars['Int']['input'];
};

export enum GqlVisibility {
  IsPrivate = 'isPrivate',
  IsPublic = 'isPublic'
}

export type GqlVisibilityFilter = {
  in?: InputMaybe<Array<GqlVisibility>>;
};

export type GqlVisibilityOneOfInput = {
  oneOf: Array<GqlVisibility>;
};

export type GqlVisibilityUpdateOperationsInput = {
  set: GqlVisibility;
};

export type GqlWaitForAction = {
  element: FieldWrapper<GqlDomElementByNameOrXPath>;
};

export type GqlWaitForActionInput = {
  element: GqlDomElementByNameOrXPathInput;
};

export type GqlWebhookCreateInput = {
  url: Scalars['String']['input'];
};

export type GqlWebhookCreateOrConnectInput = {
  connect?: InputMaybe<GqlWebhookUniqueWhereInput>;
  create?: InputMaybe<GqlWebhookCreateInput>;
};

export type GqlWebhookOrEmailInput = {
  email?: InputMaybe<Scalars['String']['input']>;
  webhook?: InputMaybe<GqlWebhookCreateOrConnectInput>;
};

export type GqlWebhookUniqueWhereInput = {
  id: Scalars['ID']['input'];
};

export type GqlXyPosition = {
  x: FieldWrapper<Scalars['Int']['output']>;
  y: FieldWrapper<Scalars['Int']['output']>;
};

export type GqlXyPositionInput = {
  x: Scalars['Int']['input'];
  y: Scalars['Int']['input'];
};

export type GqlAgentsQueryVariables = Exact<{ [key: string]: never; }>;


export type GqlAgentsQuery = { agents: Array<Pick<
      GqlAgent,
      | 'addedAt'
      | 'name'
      | 'openInstance'
      | 'ownerId'
      | 'secretKeyId'
      | 'version'
    >> };

export type GqlCreateAnnotationMutationVariables = Exact<{
  data: GqlCreateAnnotationInput;
}>;


export type GqlCreateAnnotationMutation = { createAnnotation: (
    Pick<GqlAnnotation, 'id'>
    & {
      upVote?: Maybe<Pick<GqlBoolAnnotation, 'value'>>,
      downVote?: Maybe<Pick<GqlBoolAnnotation, 'value'>>,
      flag?: Maybe<Pick<GqlBoolAnnotation, 'value'>>,
    }
  ) };

export type GqlDeleteAnnotationMutationVariables = Exact<{
  data: GqlDeleteAnnotationInput;
}>;


export type GqlDeleteAnnotationMutation = Pick<GqlMutation, 'deleteAnnotation'>;

export type GqlAuthAnonymousMutationVariables = Exact<{ [key: string]: never; }>;


export type GqlAuthAnonymousMutation = { authAnonymous: Pick<GqlAuthentication, 'token' | 'corrId'> };

export type GqlAuthUserMutationVariables = Exact<{
  data: GqlAuthUserInput;
}>;


export type GqlAuthUserMutation = { authUser: Pick<GqlAuthentication, 'token' | 'corrId'> };

export type GqlAuthUsingMailMutationVariables = Exact<{
  data: GqlAuthViaMailInput;
}>;


export type GqlAuthUsingMailMutation = { authenticateWithCodeViaMail: Pick<GqlConfirmCode, 'length' | 'otpId'> };

export type GqlConfirmCodeMutationVariables = Exact<{
  data: GqlConfirmAuthCodeInput;
}>;


export type GqlConfirmCodeMutation = { authConfirmCode: Pick<GqlAuthentication, 'token' | 'corrId'> };

export type GqlConnectedAppByIdQueryVariables = Exact<{
  id: Scalars['String']['input'];
}>;


export type GqlConnectedAppByIdQuery = { connectedApp: Pick<GqlConnectedApp, 'authorized' | 'authorizedAt'> };

export type GqlUpdateConnectedAppMutationVariables = Exact<{
  id: Scalars['String']['input'];
  authorize: Scalars['Boolean']['input'];
}>;


export type GqlUpdateConnectedAppMutation = Pick<GqlMutation, 'updateConnectedApp'>;

export type GqlDeleteConnectedAppMutationVariables = Exact<{
  id: Scalars['String']['input'];
}>;


export type GqlDeleteConnectedAppMutation = Pick<GqlMutation, 'deleteConnectedApp'>;

export type GqlFindEventsQueryVariables = Exact<{
  where: GqlRecordsWhereInput;
}>;


export type GqlFindEventsQuery = { recordsFrequency?: Maybe<Array<Pick<GqlRecordFrequency, 'count' | 'group'>>> };

export type GqlEventsByIdsQueryVariables = Exact<{
  data: GqlRecordsInput;
}>;


export type GqlEventsByIdsQuery = { records: Array<(
    Pick<
      GqlRecord,
      | 'id'
      | 'tags'
      | 'title'
      | 'html'
      | 'text'
      | 'url'
      | 'startingAt'
    >
    & { latLng?: Maybe<Pick<GqlGeoPoint, 'lat' | 'lng'>> }
  )> };

export type GqlUpdateFeatureValueMutationVariables = Exact<{
  data: GqlUpdateFeatureValueInput;
}>;


export type GqlUpdateFeatureValueMutation = Pick<GqlMutation, 'updateFeatureValue'>;

export type GqlFeatureGroupsQueryVariables = Exact<{
  inherit: Scalars['Boolean']['input'];
  where: GqlFeatureGroupWhereInput;
}>;


export type GqlFeatureGroupsQuery = { featureGroups: Array<(
    Pick<GqlFeatureGroup, 'id' | 'parentId' | 'name'>
    & { features: Array<(
      Pick<GqlFeature, 'id' | 'name'>
      & { value: (
        Pick<GqlFeatureValue, 'id'>
        & {
          numVal?: Maybe<Pick<GqlFeatureIntValue, 'value'>>,
          boolVal?: Maybe<Pick<GqlFeatureBooleanValue, 'value'>>,
        }
      ) }
    )> }
  )> };

export type GqlUpdateLicenseMutationVariables = Exact<{
  data: GqlUpdateLicenseInput;
}>;


export type GqlUpdateLicenseMutation = { updateLicense: (
    Pick<
      GqlLocalizedLicense,
      | 'isValid'
      | 'isLocated'
      | 'trialUntil'
      | 'isTrial'
    >
    & { data?: Maybe<Pick<
        GqlLicense,
        | 'name'
        | 'email'
        | 'version'
        | 'createdAt'
        | 'scope'
      >> }
  ) };

export type GqlOrdersQueryVariables = Exact<{
  data: GqlOrdersInput;
}>;


export type GqlOrdersQuery = { orders: Array<(
    Pick<
      GqlOrder,
      | 'id'
      | 'createdAt'
      | 'isOffer'
      | 'isPaid'
      | 'paymentMethod'
      | 'invoiceRecipientEmail'
      | 'invoiceRecipientName'
    >
    & { product: (
      Pick<
        GqlProduct,
        | 'id'
        | 'name'
        | 'description'
        | 'featureGroupId'
        | 'isCloud'
        | 'enterprise'
        | 'individual'
        | 'other'
        | 'partOf'
      >
      & {
        featureGroup?: Maybe<(
          Pick<GqlFeatureGroup, 'id' | 'name'>
          & { features: Array<(
            Pick<GqlFeature, 'id' | 'name'>
            & { value: (
              Pick<GqlFeatureValue, 'id'>
              & {
                numVal?: Maybe<Pick<GqlFeatureIntValue, 'value'>>,
                boolVal?: Maybe<Pick<GqlFeatureBooleanValue, 'value'>>,
              }
            ) }
          )> }
        )>,
        prices: Array<Pick<
            GqlPricedProduct,
            | 'id'
            | 'recurringInterval'
            | 'description'
            | 'inStock'
            | 'price'
          >>,
      }
    ) }
  )> };

export type GqlUpsertOrderMutationVariables = Exact<{
  data: GqlUpsertOrderInput;
}>;


export type GqlUpsertOrderMutation = { upsertOrder: (
    Pick<
      GqlOrder,
      | 'id'
      | 'createdAt'
      | 'isOffer'
      | 'isPaid'
      | 'paymentMethod'
      | 'invoiceRecipientEmail'
      | 'invoiceRecipientName'
    >
    & { product: (
      Pick<
        GqlProduct,
        | 'id'
        | 'name'
        | 'description'
        | 'featureGroupId'
        | 'isCloud'
        | 'enterprise'
        | 'individual'
        | 'other'
        | 'partOf'
      >
      & {
        featureGroup?: Maybe<(
          Pick<GqlFeatureGroup, 'id' | 'name'>
          & { features: Array<(
            Pick<GqlFeature, 'id' | 'name'>
            & { value: (
              Pick<GqlFeatureValue, 'id'>
              & {
                numVal?: Maybe<Pick<GqlFeatureIntValue, 'value'>>,
                boolVal?: Maybe<Pick<GqlFeatureBooleanValue, 'value'>>,
              }
            ) }
          )> }
        )>,
        prices: Array<Pick<
            GqlPricedProduct,
            | 'id'
            | 'recurringInterval'
            | 'description'
            | 'inStock'
            | 'price'
          >>,
      }
    ) }
  ) };

export type GqlPlansQueryVariables = Exact<{
  cursor: GqlCursor;
}>;


export type GqlPlansQuery = { plans: Array<(
    Pick<
      GqlPlan,
      | 'id'
      | 'productId'
      | 'startedAt'
      | 'terminatedAt'
    >
    & { product: (
      Pick<
        GqlProduct,
        | 'id'
        | 'name'
        | 'description'
        | 'featureGroupId'
        | 'isCloud'
        | 'enterprise'
        | 'individual'
        | 'other'
        | 'partOf'
      >
      & {
        featureGroup?: Maybe<(
          Pick<GqlFeatureGroup, 'id' | 'name'>
          & { features: Array<(
            Pick<GqlFeature, 'id' | 'name'>
            & { value: (
              Pick<GqlFeatureValue, 'id'>
              & {
                numVal?: Maybe<Pick<GqlFeatureIntValue, 'value'>>,
                boolVal?: Maybe<Pick<GqlFeatureBooleanValue, 'value'>>,
              }
            ) }
          )> }
        )>,
        prices: Array<Pick<
            GqlPricedProduct,
            | 'id'
            | 'recurringInterval'
            | 'description'
            | 'inStock'
            | 'price'
          >>,
      }
    ) }
  )> };

export type GqlListPluginsQueryVariables = Exact<{ [key: string]: never; }>;


export type GqlListPluginsQuery = { plugins: Array<Pick<
      GqlPlugin,
      | 'id'
      | 'name'
      | 'type'
      | 'listed'
    >> };

export type GqlListProductsQueryVariables = Exact<{
  data: GqlProductsWhereInput;
}>;


export type GqlListProductsQuery = { products: Array<(
    Pick<
      GqlProduct,
      | 'id'
      | 'name'
      | 'description'
      | 'featureGroupId'
      | 'isCloud'
      | 'enterprise'
      | 'individual'
      | 'other'
      | 'partOf'
    >
    & {
      featureGroup?: Maybe<(
        Pick<GqlFeatureGroup, 'id' | 'name'>
        & { features: Array<(
          Pick<GqlFeature, 'id' | 'name'>
          & { value: (
            Pick<GqlFeatureValue, 'id'>
            & {
              numVal?: Maybe<Pick<GqlFeatureIntValue, 'value'>>,
              boolVal?: Maybe<Pick<GqlFeatureBooleanValue, 'value'>>,
            }
          ) }
        )> }
      )>,
      prices: Array<Pick<
          GqlPricedProduct,
          | 'id'
          | 'recurringInterval'
          | 'description'
          | 'inStock'
          | 'price'
        >>,
    }
  )> };

export type GqlRecordByIdQueryVariables = Exact<{
  data: GqlRecordWhereInput;
}>;


export type GqlRecordByIdQuery = { record: (
    Pick<
      GqlRecord,
      | 'id'
      | 'tags'
      | 'title'
      | 'rawMimeType'
      | 'rawBase64'
      | 'html'
      | 'text'
      | 'url'
      | 'imageUrl'
      | 'createdAt'
      | 'publishedAt'
      | 'updatedAt'
      | 'startingAt'
    >
    & {
      latLng?: Maybe<Pick<GqlGeoPoint, 'lat' | 'lng'>>,
      attachments?: Maybe<Array<Pick<
          GqlAttachment,
          | 'size'
          | 'duration'
          | 'type'
          | 'url'
        >>>,
    }
  ) };

export type GqlRecordByIdsQueryVariables = Exact<{
  data: GqlRecordsInput;
}>;


export type GqlRecordByIdsQuery = { records: Array<(
    Pick<
      GqlRecord,
      | 'id'
      | 'tags'
      | 'title'
      | 'rawMimeType'
      | 'rawBase64'
      | 'html'
      | 'text'
      | 'url'
      | 'imageUrl'
      | 'createdAt'
      | 'publishedAt'
      | 'updatedAt'
      | 'startingAt'
    >
    & {
      latLng?: Maybe<Pick<GqlGeoPoint, 'lat' | 'lng'>>,
      attachments?: Maybe<Array<Pick<
          GqlAttachment,
          | 'size'
          | 'duration'
          | 'type'
          | 'url'
        >>>,
    }
  )> };

export type GqlFullRecordByIdsQueryVariables = Exact<{
  data: GqlRecordsInput;
}>;


export type GqlFullRecordByIdsQuery = { records: Array<(
    Pick<
      GqlRecord,
      | 'id'
      | 'tags'
      | 'title'
      | 'rawMimeType'
      | 'rawBase64'
      | 'html'
      | 'text'
      | 'url'
      | 'imageUrl'
      | 'createdAt'
      | 'publishedAt'
      | 'updatedAt'
      | 'startingAt'
    >
    & {
      latLng?: Maybe<Pick<GqlGeoPoint, 'lat' | 'lng'>>,
      attachments?: Maybe<Array<Pick<
          GqlAttachment,
          | 'size'
          | 'duration'
          | 'type'
          | 'url'
        >>>,
      annotations?: Maybe<{ votes?: Maybe<Array<(
          Pick<GqlAnnotation, 'id'>
          & {
            upVote?: Maybe<Pick<GqlBoolAnnotation, 'value'>>,
            text?: Maybe<Pick<GqlTextAnnotation, 'fromChar' | 'toChar'>>,
          }
        )>> }>,
    }
  )> };

export type GqlDeleteRecordsByIdMutationVariables = Exact<{
  data: GqlDeleteRecordsInput;
}>;


export type GqlDeleteRecordsByIdMutation = Pick<GqlMutation, 'deleteRecords'>;

export type GqlCreateRecordsMutationVariables = Exact<{
  records?: InputMaybe<Array<GqlCreateRecordInput> | GqlCreateRecordInput>;
  upload?: InputMaybe<Scalars['Upload']['input']>;
}>;


export type GqlCreateRecordsMutation = { createRecords: Array<(
    Pick<
      GqlRecord,
      | 'id'
      | 'tags'
      | 'title'
      | 'rawMimeType'
      | 'rawBase64'
      | 'html'
      | 'text'
      | 'url'
      | 'imageUrl'
      | 'createdAt'
      | 'publishedAt'
      | 'updatedAt'
      | 'startingAt'
    >
    & {
      latLng?: Maybe<Pick<GqlGeoPoint, 'lat' | 'lng'>>,
      attachments?: Maybe<Array<Pick<
          GqlAttachment,
          | 'size'
          | 'duration'
          | 'type'
          | 'url'
        >>>,
    }
  )> };

export type GqlUpdateRecordMutationVariables = Exact<{
  data: GqlUpdateRecordInput;
}>;


export type GqlUpdateRecordMutation = Pick<GqlMutation, 'updateRecord'>;

export type GqlCreateReportMutationVariables = Exact<{
  repositoryId: Scalars['ID']['input'];
  segmentation: GqlSegmentInput;
}>;


export type GqlCreateReportMutation = { createReport: Pick<GqlReport, 'id'> };

export type GqlCreateRepositoriesMutationVariables = Exact<{
  data: Array<GqlRepositoryCreateInput> | GqlRepositoryCreateInput;
}>;


export type GqlCreateRepositoriesMutation = { createRepositories: Array<(
    Pick<
      GqlRepository,
      | 'id'
      | 'ownerId'
      | 'title'
      | 'description'
      | 'product'
      | 'visibility'
      | 'tags'
      | 'createdAt'
      | 'lastUpdatedAt'
      | 'nextUpdateAt'
      | 'refreshCron'
      | 'sourcesCount'
      | 'sourcesCountWithProblems'
      | 'pushNotificationsEnabled'
      | 'shareKey'
      | 'disabledFrom'
      | 'archived'
      | 'documentCount'
    >
    & {
      plugins?: Maybe<Array<(
        Pick<GqlPluginExecution, 'pluginId'>
        & { params: (
          Pick<GqlPluginExecutionParams, 'jsonData'>
          & {
            org_feedless_feed?: Maybe<{ generic?: Maybe<Pick<
                  GqlSelectors,
                  | 'contextXPath'
                  | 'linkXPath'
                  | 'extendContext'
                  | 'dateXPath'
                  | 'paginationXPath'
                  | 'dateIsStartOfEvent'
                >> }>,
            org_feedless_diff_records?: Maybe<(
              Pick<
                GqlDiffRecordsParams,
                | 'inlineDiffImage'
                | 'inlineLatestImage'
                | 'inlinePreviousImage'
                | 'nextItemMinIncrement'
              >
              & { compareBy: Pick<GqlCompareBy, 'field' | 'fragmentNameRef'> }
            )>,
            org_feedless_filter?: Maybe<Array<(
              Pick<GqlItemFilterParams, 'expression'>
              & { composite?: Maybe<{
                  exclude?: Maybe<{
                      index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                      title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                    }>,
                  include?: Maybe<{
                      index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                      title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                    }>,
                }> }
            )>>,
            org_feedless_fulltext?: Maybe<Pick<GqlFulltextPluginParams, 'readability' | 'summary' | 'inheritParams'>>,
            org_feedless_conditional_tag?: Maybe<Array<(
              Pick<GqlConditionalTag, 'tag'>
              & { filter: {
                  index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                  title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                  content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                  link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                } }
            )>>,
          }
        ) }
      )>>,
      annotations?: Maybe<Pick<GqlAnnotations, 'downVotes' | 'upVotes'>>,
      retention: Pick<GqlRetention, 'maxAgeDays' | 'maxCapacity'>,
    }
  )> };

export type GqlDeleteRepositoryMutationVariables = Exact<{
  data: GqlRepositoryUniqueWhereInput;
}>;


export type GqlDeleteRepositoryMutation = Pick<GqlMutation, 'deleteRepository'>;

export type GqlListRepositoriesQueryVariables = Exact<{
  data: GqlRepositoriesInput;
}>;


export type GqlListRepositoriesQuery = { repositories: Array<(
    Pick<
      GqlRepository,
      | 'id'
      | 'ownerId'
      | 'title'
      | 'description'
      | 'product'
      | 'visibility'
      | 'tags'
      | 'createdAt'
      | 'lastUpdatedAt'
      | 'nextUpdateAt'
      | 'refreshCron'
      | 'sourcesCount'
      | 'sourcesCountWithProblems'
      | 'pushNotificationsEnabled'
      | 'shareKey'
      | 'disabledFrom'
      | 'archived'
      | 'documentCount'
    >
    & {
      plugins?: Maybe<Array<(
        Pick<GqlPluginExecution, 'pluginId'>
        & { params: (
          Pick<GqlPluginExecutionParams, 'jsonData'>
          & {
            org_feedless_feed?: Maybe<{ generic?: Maybe<Pick<
                  GqlSelectors,
                  | 'contextXPath'
                  | 'linkXPath'
                  | 'extendContext'
                  | 'dateXPath'
                  | 'paginationXPath'
                  | 'dateIsStartOfEvent'
                >> }>,
            org_feedless_diff_records?: Maybe<(
              Pick<
                GqlDiffRecordsParams,
                | 'inlineDiffImage'
                | 'inlineLatestImage'
                | 'inlinePreviousImage'
                | 'nextItemMinIncrement'
              >
              & { compareBy: Pick<GqlCompareBy, 'field' | 'fragmentNameRef'> }
            )>,
            org_feedless_filter?: Maybe<Array<(
              Pick<GqlItemFilterParams, 'expression'>
              & { composite?: Maybe<{
                  exclude?: Maybe<{
                      index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                      title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                    }>,
                  include?: Maybe<{
                      index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                      title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                    }>,
                }> }
            )>>,
            org_feedless_fulltext?: Maybe<Pick<GqlFulltextPluginParams, 'readability' | 'summary' | 'inheritParams'>>,
            org_feedless_conditional_tag?: Maybe<Array<(
              Pick<GqlConditionalTag, 'tag'>
              & { filter: {
                  index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                  title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                  content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                  link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                } }
            )>>,
          }
        ) }
      )>>,
      annotations?: Maybe<Pick<GqlAnnotations, 'downVotes' | 'upVotes'>>,
      retention: Pick<GqlRetention, 'maxAgeDays' | 'maxCapacity'>,
      frequency?: Maybe<Array<Pick<GqlRecordFrequency, 'count' | 'group'>>>,
    }
  )> };

export type GqlListPublicRepositoriesQueryVariables = Exact<{
  data: GqlRepositoriesInput;
}>;


export type GqlListPublicRepositoriesQuery = { repositories: Array<Pick<
      GqlRepository,
      | 'id'
      | 'ownerId'
      | 'title'
      | 'description'
      | 'product'
      | 'visibility'
      | 'tags'
      | 'createdAt'
      | 'lastUpdatedAt'
      | 'nextUpdateAt'
      | 'refreshCron'
      | 'pullsPerMonth'
      | 'disabledFrom'
      | 'archived'
      | 'documentCount'
    >> };

export type GqlCountRepositoriesQueryVariables = Exact<{
  data: GqlCountRepositoriesInput;
}>;


export type GqlCountRepositoriesQuery = Pick<GqlQuery, 'countRepositories'>;

export type GqlRepositoryByIdQueryVariables = Exact<{
  repository: GqlRepositoryWhereInput;
  cursor: GqlCursor;
  where?: InputMaybe<GqlSourcesWhereInput>;
}>;


export type GqlRepositoryByIdQuery = { repository: (
    Pick<
      GqlRepository,
      | 'id'
      | 'ownerId'
      | 'title'
      | 'description'
      | 'product'
      | 'visibility'
      | 'tags'
      | 'createdAt'
      | 'lastUpdatedAt'
      | 'nextUpdateAt'
      | 'refreshCron'
      | 'sourcesCount'
      | 'sourcesCountWithProblems'
      | 'pushNotificationsEnabled'
      | 'shareKey'
      | 'disabledFrom'
      | 'archived'
      | 'documentCount'
    >
    & {
      plugins?: Maybe<Array<(
        Pick<GqlPluginExecution, 'pluginId'>
        & { params: (
          Pick<GqlPluginExecutionParams, 'jsonData'>
          & {
            org_feedless_feed?: Maybe<{ generic?: Maybe<Pick<
                  GqlSelectors,
                  | 'contextXPath'
                  | 'linkXPath'
                  | 'extendContext'
                  | 'dateXPath'
                  | 'paginationXPath'
                  | 'dateIsStartOfEvent'
                >> }>,
            org_feedless_diff_records?: Maybe<(
              Pick<
                GqlDiffRecordsParams,
                | 'inlineDiffImage'
                | 'inlineLatestImage'
                | 'inlinePreviousImage'
                | 'nextItemMinIncrement'
              >
              & { compareBy: Pick<GqlCompareBy, 'field' | 'fragmentNameRef'> }
            )>,
            org_feedless_filter?: Maybe<Array<(
              Pick<GqlItemFilterParams, 'expression'>
              & { composite?: Maybe<{
                  exclude?: Maybe<{
                      index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                      title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                    }>,
                  include?: Maybe<{
                      index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                      title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                      link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                    }>,
                }> }
            )>>,
            org_feedless_fulltext?: Maybe<Pick<GqlFulltextPluginParams, 'readability' | 'summary' | 'inheritParams'>>,
            org_feedless_conditional_tag?: Maybe<Array<(
              Pick<GqlConditionalTag, 'tag'>
              & { filter: {
                  index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                  title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                  content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                  link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                } }
            )>>,
          }
        ) }
      )>>,
      annotations?: Maybe<(
        Pick<GqlAnnotations, 'downVotes' | 'upVotes'>
        & { votes?: Maybe<Array<(
          Pick<GqlAnnotation, 'id'>
          & {
            upVote?: Maybe<Pick<GqlBoolAnnotation, 'value'>>,
            downVote?: Maybe<Pick<GqlBoolAnnotation, 'value'>>,
            flag?: Maybe<Pick<GqlBoolAnnotation, 'value'>>,
          }
        )>> }
      )>,
      retention: Pick<GqlRetention, 'maxAgeDays' | 'maxCapacity'>,
      frequency?: Maybe<Array<Pick<GqlRecordFrequency, 'count' | 'group'>>>,
      sources?: Maybe<Array<(
        Pick<
          GqlSource,
          | 'id'
          | 'disabled'
          | 'tags'
          | 'lastErrorMessage'
          | 'recordCount'
          | 'lastRefreshedAt'
          | 'lastRecordsRetrieved'
          | 'title'
        >
        & { latLng?: Maybe<Pick<GqlGeoPoint, 'lat' | 'lng'>> }
      )>>,
    }
  ) };

export type GqlSourcesByRepositoryQueryVariables = Exact<{
  repository: GqlRepositoryWhereInput;
  cursor: GqlCursor;
  where?: InputMaybe<GqlSourcesWhereInput>;
  order?: InputMaybe<Array<GqlSourceOrderByInput> | GqlSourceOrderByInput>;
}>;


export type GqlSourcesByRepositoryQuery = { repository: { sources?: Maybe<Array<(
      Pick<
        GqlSource,
        | 'id'
        | 'disabled'
        | 'tags'
        | 'lastErrorMessage'
        | 'recordCount'
        | 'lastRefreshedAt'
        | 'lastRecordsRetrieved'
        | 'title'
      >
      & {
        latLng?: Maybe<Pick<GqlGeoPoint, 'lat' | 'lng'>>,
        harvests?: Maybe<Array<Pick<GqlHarvest, 'itemsAdded' | 'finishedAt'>>>,
      }
    )>> } };

export type GqlLastHarvestsFromSourcesByRepositoryQueryVariables = Exact<{
  repositoryId: Scalars['ID']['input'];
  sourceId: Scalars['String']['input'];
}>;


export type GqlLastHarvestsFromSourcesByRepositoryQuery = { repository: { sources?: Maybe<Array<{ harvests?: Maybe<Array<Pick<
          GqlHarvest,
          | 'startedAt'
          | 'finishedAt'
          | 'itemsAdded'
          | 'itemsIgnored'
          | 'logs'
          | 'ok'
        >>> }>> } };

export type GqlSourcesWithFlowByRepositoryQueryVariables = Exact<{
  repository: GqlRepositoryWhereInput;
  cursor: GqlCursor;
  where?: InputMaybe<GqlSourcesWhereInput>;
}>;


export type GqlSourcesWithFlowByRepositoryQuery = { repository: { sources?: Maybe<Array<(
      Pick<
        GqlSource,
        | 'id'
        | 'disabled'
        | 'tags'
        | 'lastErrorMessage'
        | 'recordCount'
        | 'lastRefreshedAt'
        | 'lastRecordsRetrieved'
        | 'title'
      >
      & {
        latLng?: Maybe<Pick<GqlGeoPoint, 'lat' | 'lng'>>,
        flow: { sequence: Array<{
              execute?: Maybe<(
                Pick<GqlPluginExecution, 'pluginId'>
                & { params: (
                  Pick<GqlPluginExecutionParams, 'jsonData'>
                  & {
                    org_feedless_feed?: Maybe<{ generic?: Maybe<Pick<
                          GqlSelectors,
                          | 'contextXPath'
                          | 'linkXPath'
                          | 'extendContext'
                          | 'dateXPath'
                          | 'paginationXPath'
                          | 'dateIsStartOfEvent'
                        >> }>,
                    org_feedless_diff_records?: Maybe<(
                      Pick<
                        GqlDiffRecordsParams,
                        | 'inlineDiffImage'
                        | 'inlineLatestImage'
                        | 'inlinePreviousImage'
                        | 'nextItemMinIncrement'
                      >
                      & { compareBy: Pick<GqlCompareBy, 'field' | 'fragmentNameRef'> }
                    )>,
                    org_feedless_filter?: Maybe<Array<(
                      Pick<GqlItemFilterParams, 'expression'>
                      & { composite?: Maybe<{
                          exclude?: Maybe<{
                              index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                              title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                              content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                              link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                            }>,
                          include?: Maybe<{
                              index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                              title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                              content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                              link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                            }>,
                        }> }
                    )>>,
                    org_feedless_fulltext?: Maybe<Pick<GqlFulltextPluginParams, 'readability' | 'summary' | 'inheritParams'>>,
                    org_feedless_conditional_tag?: Maybe<Array<(
                      Pick<GqlConditionalTag, 'tag'>
                      & { filter: {
                          index?: Maybe<Pick<GqlNumericalFilterParams, 'operator' | 'value'>>,
                          title?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                          content?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                          link?: Maybe<Pick<GqlStringFilterParams, 'operator' | 'value'>>,
                        } }
                    )>>,
                  }
                ) }
              )>,
              extract?: Maybe<(
                Pick<GqlScrapeExtract, 'fragmentName'>
                & {
                  selectorBased?: Maybe<(
                    Pick<GqlDomExtract, 'fragmentName' | 'emit' | 'max'>
                    & { xpath: Pick<GqlDomElementByXPath, 'value'> }
                  )>,
                  imageBased?: Maybe<{ boundingBox: Pick<
                        GqlBoundingBox,
                        | 'x'
                        | 'y'
                        | 'h'
                        | 'w'
                      > }>,
                }
              )>,
              type?: Maybe<{ element: Pick<GqlDomElementByXPath, 'value'> }>,
              fetch?: Maybe<{ get: (
                  Pick<
                    GqlHttpGetRequest,
                    | 'timeout'
                    | 'forcePrerender'
                    | 'additionalWaitSec'
                    | 'language'
                    | 'waitUntil'
                  >
                  & {
                    url: Pick<GqlStringLiteralOrVariable, 'literal' | 'variable'>,
                    viewport?: Maybe<Pick<
                        GqlViewPort,
                        | 'height'
                        | 'width'
                        | 'isLandscape'
                        | 'isMobile'
                      >>,
                  }
                ) }>,
              click?: Maybe<{
                  position?: Maybe<Pick<GqlXyPosition, 'x' | 'y'>>,
                  element?: Maybe<{
                      xpath?: Maybe<Pick<GqlDomElementByXPath, 'value'>>,
                      name?: Maybe<Pick<GqlDomElementByName, 'value'>>,
                    }>,
                }>,
              header?: Maybe<Pick<GqlRequestHeader, 'value' | 'name'>>,
              select?: Maybe<(
                Pick<GqlDomActionSelect, 'selectValue'>
                & { element: Pick<GqlDomElementByXPath, 'value'> }
              )>,
            }> },
      }
    )>> } };

export type GqlUpdateRepositoryMutationVariables = Exact<{
  data: GqlRepositoryUpdateInput;
}>;


export type GqlUpdateRepositoryMutation = Pick<GqlMutation, 'updateRepository'>;

export type GqlScrapeQueryVariables = Exact<{
  data: GqlSourceInput;
}>;


export type GqlScrapeQuery = { scrape: (
    Pick<GqlScrapeResponse, 'ok' | 'errorMessage'>
    & {
      outputs: Array<(
        Pick<GqlScrapeOutputResponse, 'index'>
        & { response: {
            extract?: Maybe<(
              Pick<GqlScrapeExtractResponse, 'fragmentName'>
              & {
                items?: Maybe<Array<(
                  Pick<
                    GqlRecord,
                    | 'id'
                    | 'tags'
                    | 'title'
                    | 'rawMimeType'
                    | 'rawBase64'
                    | 'html'
                    | 'text'
                    | 'url'
                    | 'imageUrl'
                    | 'createdAt'
                    | 'publishedAt'
                    | 'updatedAt'
                    | 'startingAt'
                  >
                  & {
                    latLng?: Maybe<Pick<GqlGeoPoint, 'lat' | 'lng'>>,
                    attachments?: Maybe<Array<Pick<
                        GqlAttachment,
                        | 'size'
                        | 'duration'
                        | 'type'
                        | 'url'
                      >>>,
                  }
                )>>,
                feeds?: Maybe<{
                    nativeFeeds?: Maybe<Array<(
                      Pick<
                        GqlRemoteNativeFeed,
                        | 'title'
                        | 'description'
                        | 'websiteUrl'
                        | 'tags'
                        | 'feedUrl'
                        | 'nextPageUrls'
                      >
                      & { items: Array<(
                        Pick<
                          GqlRecord,
                          | 'id'
                          | 'tags'
                          | 'title'
                          | 'rawMimeType'
                          | 'rawBase64'
                          | 'html'
                          | 'text'
                          | 'url'
                          | 'imageUrl'
                          | 'createdAt'
                          | 'publishedAt'
                          | 'updatedAt'
                          | 'startingAt'
                        >
                        & {
                          latLng?: Maybe<Pick<GqlGeoPoint, 'lat' | 'lng'>>,
                          attachments?: Maybe<Array<Pick<
                              GqlAttachment,
                              | 'size'
                              | 'duration'
                              | 'type'
                              | 'url'
                            >>>,
                        }
                      )> }
                    )>>,
                    genericFeeds: Array<(
                      Pick<GqlTransientGenericFeed, 'count' | 'hash' | 'score'>
                      & { selectors: Pick<
                          GqlSelectors,
                          | 'contextXPath'
                          | 'linkXPath'
                          | 'extendContext'
                          | 'dateXPath'
                          | 'paginationXPath'
                          | 'dateIsStartOfEvent'
                        > }
                    )>,
                  }>,
                fragments?: Maybe<Array<{
                    data?: Maybe<Pick<GqlMimeData, 'mimeType' | 'data'>>,
                    text?: Maybe<Pick<GqlTextData, 'data'>>,
                    html?: Maybe<Pick<GqlTextData, 'data'>>,
                    extracts?: Maybe<Array<(
                      Pick<GqlScrapeExtractResponse, 'fragmentName'>
                      & { fragments?: Maybe<Array<{
                          data?: Maybe<Pick<GqlMimeData, 'mimeType' | 'data'>>,
                          text?: Maybe<Pick<GqlTextData, 'data'>>,
                          html?: Maybe<Pick<GqlTextData, 'data'>>,
                        }>> }
                    )>>,
                  }>>,
              }
            )>,
            fetch?: Maybe<(
              Pick<GqlHttpFetchResponse, 'data'>
              & { debug: (
                Pick<
                  GqlFetchActionDebugResponse,
                  | 'console'
                  | 'contentType'
                  | 'cookies'
                  | 'corrId'
                  | 'screenshot'
                  | 'statusCode'
                >
                & { viewport: Pick<
                    GqlViewPort,
                    | 'height'
                    | 'width'
                    | 'isLandscape'
                    | 'isMobile'
                  > }
              ) }
            )>,
          } }
      )>,
      logs: Array<Pick<GqlLogStatement, 'time' | 'message'>>,
    }
  ) };

export type GqlServerSettingsQueryVariables = Exact<{
  data: GqlServerSettingsContextInput;
}>;


export type GqlServerSettingsQuery = { serverSettings: (
    Pick<GqlServerSettings, 'auth' | 'profiles' | 'version'>
    & {
      build: Pick<GqlBuildInfo, 'commit' | 'date'>,
      license?: Maybe<(
        Pick<
          GqlLocalizedLicense,
          | 'isValid'
          | 'isLocated'
          | 'trialUntil'
          | 'isTrial'
        >
        & { data?: Maybe<Pick<
            GqlLicense,
            | 'name'
            | 'email'
            | 'version'
            | 'createdAt'
            | 'scope'
          >> }
      )>,
    }
  ) };

export type GqlSessionQueryVariables = Exact<{ [key: string]: never; }>;


export type GqlSessionQuery = { session: (
    Pick<GqlSession, 'isLoggedIn'>
    & { user?: Maybe<(
      Pick<
        GqlUser,
        | 'id'
        | 'hasAcceptedTerms'
        | 'hasCompletedSignup'
        | 'email'
        | 'emailValidated'
        | 'firstName'
        | 'lastName'
        | 'country'
        | 'createdAt'
        | 'notificationRepositoryId'
        | 'purgeScheduledFor'
      >
      & {
        features: Array<(
          Pick<GqlFeature, 'id' | 'name'>
          & { value: (
            Pick<GqlFeatureValue, 'id'>
            & {
              numVal?: Maybe<Pick<GqlFeatureIntValue, 'value'>>,
              boolVal?: Maybe<Pick<GqlFeatureBooleanValue, 'value'>>,
            }
          ) }
        )>,
        connectedApps: Array<Maybe<Pick<
            GqlConnectedApp,
            | 'id'
            | 'authorized'
            | 'authorizedAt'
            | 'app'
          >>>,
        secrets: Array<Pick<
            GqlUserSecret,
            | 'id'
            | 'name'
            | 'validUntil'
            | 'type'
            | 'lastUsed'
            | 'value'
            | 'valueMasked'
          >>,
      }
    )> }
  ) };

export type GqlUpdateCurrentUserMutationVariables = Exact<{
  data: GqlUpdateCurrentUserInput;
}>;


export type GqlUpdateCurrentUserMutation = Pick<GqlMutation, 'updateCurrentUser'>;

export type GqlLogoutMutationVariables = Exact<{ [key: string]: never; }>;


export type GqlLogoutMutation = Pick<GqlMutation, 'logout'>;

export type GqlCreateUserSecretMutationVariables = Exact<{
  data: GqlCreateUserSecretInput;
}>;


export type GqlCreateUserSecretMutation = { createUserSecret: Pick<
      GqlUserSecret,
      | 'id'
      | 'name'
      | 'validUntil'
      | 'type'
      | 'lastUsed'
      | 'value'
      | 'valueMasked'
    > };

export type GqlDeleteUserSecretMutationVariables = Exact<{
  data: GqlDeleteUserSecretInput;
}>;


export type GqlDeleteUserSecretMutation = Pick<GqlMutation, 'deleteUserSecret'>;

export type GqlIssueAnonymousFeedTokenMutationVariables = Exact<{
  url: Scalars['String']['input'];
}>;


export type GqlIssueAnonymousFeedTokenMutation = { issueAnonymousFeedToken: Pick<GqlAuthentication, 'token'> };

export const AuthenticationFragment = gql`
    fragment AuthenticationFragment on Authentication {
  token
  corrId
}
    `;
export const EventFragment = gql`
    fragment EventFragment on Record {
  id
  tags
  title
  html
  text
  url
  startingAt
  latLng {
    lat
    lng
  }
}
    `;
export const FeatureFragment = gql`
    fragment FeatureFragment on Feature {
  id
  name
  value {
    id
    numVal {
      value
    }
    boolVal {
      value
    }
  }
}
    `;
export const FeatureGroupFragment = gql`
    fragment FeatureGroupFragment on FeatureGroup {
  id
  parentId
  name
  features {
    ...FeatureFragment
  }
}
    ${FeatureFragment}`;
export const SourceFragment = gql`
    fragment SourceFragment on Source {
  id
  disabled
  tags
  latLng {
    lat
    lng
  }
  lastErrorMessage
  recordCount
  lastRefreshedAt
  lastRecordsRetrieved
  title
}
    `;
export const SelectorsFragment = gql`
    fragment SelectorsFragment on Selectors {
  contextXPath
  linkXPath
  extendContext
  dateXPath
  paginationXPath
  dateIsStartOfEvent
}
    `;
export const DiffRecordsParamsFragment = gql`
    fragment DiffRecordsParamsFragment on DiffRecordsParams {
  inlineDiffImage
  inlineLatestImage
  inlinePreviousImage
  compareBy {
    field
    fragmentNameRef
  }
  nextItemMinIncrement
}
    `;
export const StringFilterParamsFragment = gql`
    fragment StringFilterParamsFragment on StringFilterParams {
  operator
  value
}
    `;
export const CompositeFieldFilterParamsFragment = gql`
    fragment CompositeFieldFilterParamsFragment on CompositeFieldFilterParams {
  index {
    operator
    value
  }
  title {
    ...StringFilterParamsFragment
  }
  content {
    ...StringFilterParamsFragment
  }
  link {
    ...StringFilterParamsFragment
  }
}
    ${StringFilterParamsFragment}`;
export const CompositeFilterParamsFragment = gql`
    fragment CompositeFilterParamsFragment on CompositeFilterParams {
  exclude {
    ...CompositeFieldFilterParamsFragment
  }
  include {
    ...CompositeFieldFilterParamsFragment
  }
}
    ${CompositeFieldFilterParamsFragment}`;
export const ConditionalTagFragment = gql`
    fragment ConditionalTagFragment on ConditionalTag {
  tag
  filter {
    ...CompositeFieldFilterParamsFragment
  }
}
    ${CompositeFieldFilterParamsFragment}`;
export const PluginExecutionParamsFragment = gql`
    fragment PluginExecutionParamsFragment on PluginExecutionParams {
  org_feedless_feed {
    generic {
      ...SelectorsFragment
    }
  }
  org_feedless_diff_records {
    ...DiffRecordsParamsFragment
  }
  org_feedless_filter {
    composite {
      ...CompositeFilterParamsFragment
    }
    expression
  }
  org_feedless_fulltext {
    readability
    summary
    inheritParams
  }
  org_feedless_conditional_tag {
    ...ConditionalTagFragment
  }
  jsonData
}
    ${SelectorsFragment}
${DiffRecordsParamsFragment}
${CompositeFilterParamsFragment}
${ConditionalTagFragment}`;
export const RepositoryFragment = gql`
    fragment RepositoryFragment on Repository {
  id
  ownerId
  title
  description
  product
  visibility
  tags
  createdAt
  lastUpdatedAt
  nextUpdateAt
  plugins {
    pluginId
    params {
      ...PluginExecutionParamsFragment
    }
  }
  annotations {
    downVotes
    upVotes
  }
  refreshCron
  sourcesCount
  sourcesCountWithProblems
  pushNotificationsEnabled
  shareKey
  disabledFrom
  archived
  documentCount
  retention {
    maxAgeDays
    maxCapacity
  }
}
    ${PluginExecutionParamsFragment}`;
export const ViewportFragment = gql`
    fragment ViewportFragment on ViewPort {
  height
  width
  isLandscape
  isMobile
}
    `;
export const HttpGetRequestFragment = gql`
    fragment HttpGetRequestFragment on HttpGetRequest {
  url {
    literal
    variable
  }
  timeout
  viewport {
    ...ViewportFragment
  }
  forcePrerender
  additionalWaitSec
  language
  waitUntil
}
    ${ViewportFragment}`;
export const ActionFragment = gql`
    fragment ActionFragment on ScrapeAction {
  execute {
    pluginId
    params {
      ...PluginExecutionParamsFragment
    }
  }
  extract {
    fragmentName
    selectorBased {
      fragmentName
      emit
      xpath {
        value
      }
      max
    }
    imageBased {
      boundingBox {
        x
        y
        h
        w
      }
    }
  }
  type {
    element {
      value
    }
  }
  fetch {
    get {
      ...HttpGetRequestFragment
    }
  }
  click {
    position {
      x
      y
    }
    element {
      xpath {
        value
      }
      name {
        value
      }
    }
  }
  header {
    value
    name
  }
  select {
    element {
      value
    }
    selectValue
  }
}
    ${PluginExecutionParamsFragment}
${HttpGetRequestFragment}`;
export const ScrapeFlowFragment = gql`
    fragment ScrapeFlowFragment on Source {
  flow {
    sequence {
      ...ActionFragment
    }
  }
}
    ${ActionFragment}`;
export const PaginationFragment = gql`
    fragment PaginationFragment on Pagination {
  page
  isLast
  isFirst
  isEmpty
}
    `;
export const NativeFeedFragment = gql`
    fragment NativeFeedFragment on NativeFeed {
  title
  description
  domain
  imageUrl
  iconUrl
  websiteUrl
  feedUrl
  status
  lastCheckedAt
  errorMessage
  lastChangedAt
  streamId
  lat
  lng
  ownerId
  createdAt
}
    `;
export const LicenseFragment = gql`
    fragment LicenseFragment on License {
  name
  email
  version
  createdAt
  scope
}
    `;
export const LocalizedLicenseFragment = gql`
    fragment LocalizedLicenseFragment on LocalizedLicense {
  isValid
  isLocated
  trialUntil
  isTrial
  data {
    ...LicenseFragment
  }
}
    ${LicenseFragment}`;
export const ProductFragment = gql`
    fragment ProductFragment on Product {
  id
  name
  description
  featureGroupId
  featureGroup {
    id
    name
    features {
      ...FeatureFragment
    }
  }
  isCloud
  enterprise
  individual
  other
  partOf
  prices {
    id
    recurringInterval
    description
    inStock
    price
  }
}
    ${FeatureFragment}`;
export const OrderFragment = gql`
    fragment OrderFragment on Order {
  id
  product {
    ...ProductFragment
  }
  createdAt
  isOffer
  isPaid
  paymentMethod
  invoiceRecipientEmail
  invoiceRecipientName
}
    ${ProductFragment}`;
export const PluginFragment = gql`
    fragment PluginFragment on Plugin {
  id
  name
  listed
}
    `;
export const AttachmentFragment = gql`
    fragment AttachmentFragment on Attachment {
  size
  duration
  type
  url
}
    `;
export const RecordFragment = gql`
    fragment RecordFragment on Record {
  id
  tags
  title
  rawMimeType
  rawBase64
  html
  text
  url
  imageUrl
  createdAt
  publishedAt
  updatedAt
  startingAt
  tags
  latLng {
    lat
    lng
  }
  attachments {
    ...AttachmentFragment
  }
}
    ${AttachmentFragment}`;
export const RemoteNativeFeedFragment = gql`
    fragment RemoteNativeFeedFragment on RemoteNativeFeed {
  title
  description
  websiteUrl
  tags
  feedUrl
  nextPageUrls
  items {
    ...RecordFragment
  }
}
    ${RecordFragment}`;
export const ScrapeExtractFragmentFragment = gql`
    fragment ScrapeExtractFragmentFragment on ScrapeExtractFragment {
  data {
    mimeType
    data
  }
  text {
    data
  }
  html {
    data
  }
}
    `;
export const ScrapeExtractResponseFragment = gql`
    fragment ScrapeExtractResponseFragment on ScrapeExtractResponse {
  fragmentName
  items {
    ...RecordFragment
  }
  feeds {
    nativeFeeds {
      ...RemoteNativeFeedFragment
    }
    genericFeeds {
      count
      hash
      score
      selectors {
        ...SelectorsFragment
      }
    }
  }
  fragments {
    ...ScrapeExtractFragmentFragment
    extracts {
      fragmentName
      fragments {
        ...ScrapeExtractFragmentFragment
      }
    }
  }
}
    ${RecordFragment}
${RemoteNativeFeedFragment}
${SelectorsFragment}
${ScrapeExtractFragmentFragment}`;
export const PlanFragment = gql`
    fragment PlanFragment on Plan {
  id
  productId
  startedAt
  terminatedAt
  product {
    ...ProductFragment
  }
}
    ${ProductFragment}`;
export const SecretFragment = gql`
    fragment SecretFragment on UserSecret {
  id
  name
  validUntil
  type
  lastUsed
  value
  valueMasked
}
    `;
export const UserFragment = gql`
    fragment UserFragment on User {
  id
  hasAcceptedTerms
  hasCompletedSignup
  email
  emailValidated
  firstName
  lastName
  country
  createdAt
  notificationRepositoryId
  purgeScheduledFor
  features {
    ...FeatureFragment
  }
  connectedApps {
    id
    authorized
    authorizedAt
    app
  }
  secrets {
    ...SecretFragment
  }
}
    ${FeatureFragment}
${SecretFragment}`;
export const Agents = gql`
    query agents {
  agents {
    addedAt
    name
    openInstance
    ownerId
    secretKeyId
    version
  }
}
    `;
export const CreateAnnotation = gql`
    mutation createAnnotation($data: CreateAnnotationInput!) {
  createAnnotation(data: $data) {
    id
    upVote {
      value
    }
    downVote {
      value
    }
    flag {
      value
    }
  }
}
    `;
export const DeleteAnnotation = gql`
    mutation deleteAnnotation($data: DeleteAnnotationInput!) {
  deleteAnnotation(data: $data)
}
    `;
export const AuthAnonymous = gql`
    mutation authAnonymous {
  authAnonymous {
    ...AuthenticationFragment
  }
}
    ${AuthenticationFragment}`;
export const AuthUser = gql`
    mutation authUser($data: AuthUserInput!) {
  authUser(data: $data) {
    ...AuthenticationFragment
  }
}
    ${AuthenticationFragment}`;
export const AuthUsingMail = gql`
    mutation authUsingMail($data: AuthViaMailInput!) {
  authenticateWithCodeViaMail(data: $data) {
    length
    otpId
  }
}
    `;
export const ConfirmCode = gql`
    mutation confirmCode($data: ConfirmAuthCodeInput!) {
  authConfirmCode(data: $data) {
    corrId
    token
  }
}
    `;
export const ConnectedAppById = gql`
    query connectedAppById($id: String!) {
  connectedApp(id: $id) {
    authorized
    authorizedAt
  }
}
    `;
export const UpdateConnectedApp = gql`
    mutation updateConnectedApp($id: String!, $authorize: Boolean!) {
  updateConnectedApp(id: $id, authorize: $authorize)
}
    `;
export const DeleteConnectedApp = gql`
    mutation deleteConnectedApp($id: String!) {
  deleteConnectedApp(id: $id)
}
    `;
export const FindEvents = gql`
    query findEvents($where: RecordsWhereInput!) {
  recordsFrequency(where: $where, groupBy: startingAt) {
    count
    group
  }
}
    `;
export const EventsByIds = gql`
    query eventsByIds($data: RecordsInput!) {
  records(data: $data) {
    ...EventFragment
  }
}
    ${EventFragment}`;
export const UpdateFeatureValue = gql`
    mutation updateFeatureValue($data: UpdateFeatureValueInput!) {
  updateFeatureValue(data: $data)
}
    `;
export const FeatureGroups = gql`
    query featureGroups($inherit: Boolean!, $where: FeatureGroupWhereInput!) {
  featureGroups(inherit: $inherit, where: $where) {
    ...FeatureGroupFragment
  }
}
    ${FeatureGroupFragment}`;
export const UpdateLicense = gql`
    mutation updateLicense($data: UpdateLicenseInput!) {
  updateLicense(data: $data) {
    ...LocalizedLicenseFragment
  }
}
    ${LocalizedLicenseFragment}`;
export const Orders = gql`
    query orders($data: OrdersInput!) {
  orders(data: $data) {
    ...OrderFragment
  }
}
    ${OrderFragment}`;
export const UpsertOrder = gql`
    mutation upsertOrder($data: UpsertOrderInput!) {
  upsertOrder(data: $data) {
    ...OrderFragment
  }
}
    ${OrderFragment}`;
export const Plans = gql`
    query plans($cursor: Cursor!) {
  plans(cursor: $cursor) {
    ...PlanFragment
  }
}
    ${PlanFragment}`;
export const ListPlugins = gql`
    query listPlugins {
  plugins {
    id
    name
    type
    listed
  }
}
    `;
export const ListProducts = gql`
    query listProducts($data: ProductsWhereInput!) {
  products(data: $data) {
    ...ProductFragment
  }
}
    ${ProductFragment}`;
export const RecordById = gql`
    query recordById($data: RecordWhereInput!) {
  record(data: $data) {
    ...RecordFragment
  }
}
    ${RecordFragment}`;
export const RecordByIds = gql`
    query recordByIds($data: RecordsInput!) {
  records(data: $data) {
    ...RecordFragment
  }
}
    ${RecordFragment}`;
export const FullRecordByIds = gql`
    query fullRecordByIds($data: RecordsInput!) {
  records(data: $data) {
    ...RecordFragment
    annotations {
      votes {
        id
        upVote {
          value
        }
        text {
          fromChar
          toChar
        }
      }
    }
  }
}
    ${RecordFragment}`;
export const DeleteRecordsById = gql`
    mutation deleteRecordsById($data: DeleteRecordsInput!) {
  deleteRecords(data: $data)
}
    `;
export const CreateRecords = gql`
    mutation createRecords($records: [CreateRecordInput!], $upload: Upload) {
  createRecords(records: $records, upload: $upload) {
    ...RecordFragment
  }
}
    ${RecordFragment}`;
export const UpdateRecord = gql`
    mutation updateRecord($data: UpdateRecordInput!) {
  updateRecord(data: $data)
}
    `;
export const CreateReport = gql`
    mutation createReport($repositoryId: ID!, $segmentation: SegmentInput!) {
  createReport(repositoryId: $repositoryId, segmentation: $segmentation) {
    id
  }
}
    `;
export const CreateRepositories = gql`
    mutation createRepositories($data: [RepositoryCreateInput!]!) {
  createRepositories(data: $data) {
    ...RepositoryFragment
  }
}
    ${RepositoryFragment}`;
export const DeleteRepository = gql`
    mutation deleteRepository($data: RepositoryUniqueWhereInput!) {
  deleteRepository(data: $data)
}
    `;
export const ListRepositories = gql`
    query listRepositories($data: RepositoriesInput!) {
  repositories(data: $data) {
    ...RepositoryFragment
    frequency(groupBy: createdAt) {
      count
      group
    }
  }
}
    ${RepositoryFragment}`;
export const ListPublicRepositories = gql`
    query listPublicRepositories($data: RepositoriesInput!) {
  repositories(data: $data) {
    id
    ownerId
    title
    description
    product
    visibility
    tags
    createdAt
    lastUpdatedAt
    nextUpdateAt
    refreshCron
    pullsPerMonth
    disabledFrom
    archived
    documentCount
  }
}
    `;
export const CountRepositories = gql`
    query countRepositories($data: CountRepositoriesInput!) {
  countRepositories(data: $data)
}
    `;
export const RepositoryById = gql`
    query repositoryById($repository: RepositoryWhereInput!, $cursor: Cursor!, $where: SourcesWhereInput) {
  repository(data: $repository) {
    ...RepositoryFragment
    frequency(groupBy: createdAt) {
      count
      group
    }
    sourcesCount
    sources(cursor: $cursor, where: $where) {
      ...SourceFragment
    }
    annotations {
      votes {
        id
        flag {
          value
        }
        upVote {
          value
        }
        downVote {
          value
        }
      }
    }
  }
}
    ${RepositoryFragment}
${SourceFragment}`;
export const SourcesByRepository = gql`
    query sourcesByRepository($repository: RepositoryWhereInput!, $cursor: Cursor!, $where: SourcesWhereInput, $order: [SourceOrderByInput!]) {
  repository(data: $repository) {
    sources(cursor: $cursor, where: $where, order: $order) {
      ...SourceFragment
      harvests {
        itemsAdded
        finishedAt
      }
    }
  }
}
    ${SourceFragment}`;
export const LastHarvestsFromSourcesByRepository = gql`
    query lastHarvestsFromSourcesByRepository($repositoryId: ID!, $sourceId: String!) {
  repository(data: {where: {id: $repositoryId}}) {
    sources(where: {id: {eq: $sourceId}}, cursor: {page: 0}) {
      harvests {
        startedAt
        finishedAt
        itemsAdded
        itemsIgnored
        logs
        ok
      }
    }
  }
}
    `;
export const SourcesWithFlowByRepository = gql`
    query sourcesWithFlowByRepository($repository: RepositoryWhereInput!, $cursor: Cursor!, $where: SourcesWhereInput) {
  repository(data: $repository) {
    sources(cursor: $cursor, where: $where) {
      ...SourceFragment
      ...ScrapeFlowFragment
    }
  }
}
    ${SourceFragment}
${ScrapeFlowFragment}`;
export const UpdateRepository = gql`
    mutation updateRepository($data: RepositoryUpdateInput!) {
  updateRepository(data: $data)
}
    `;
export const Scrape = gql`
    query scrape($data: SourceInput!) {
  scrape(data: $data) {
    outputs {
      index
      response {
        extract {
          ...ScrapeExtractResponseFragment
        }
        fetch {
          data
          debug {
            console
            contentType
            cookies
            corrId
            screenshot
            statusCode
            viewport {
              ...ViewportFragment
            }
          }
        }
        extract {
          ...ScrapeExtractResponseFragment
        }
      }
    }
    ok
    logs {
      time
      message
    }
    errorMessage
  }
}
    ${ScrapeExtractResponseFragment}
${ViewportFragment}`;
export const ServerSettings = gql`
    query serverSettings($data: ServerSettingsContextInput!) {
  serverSettings(data: $data) {
    auth
    profiles
    build {
      commit
      date
    }
    version
    license {
      ...LocalizedLicenseFragment
    }
  }
}
    ${LocalizedLicenseFragment}`;
export const Session = gql`
    query session {
  session {
    isLoggedIn
    user {
      ...UserFragment
    }
  }
}
    ${UserFragment}`;
export const UpdateCurrentUser = gql`
    mutation updateCurrentUser($data: UpdateCurrentUserInput!) {
  updateCurrentUser(data: $data)
}
    `;
export const Logout = gql`
    mutation logout {
  logout
}
    `;
export const CreateUserSecret = gql`
    mutation createUserSecret($data: CreateUserSecretInput!) {
  createUserSecret(data: $data) {
    ...SecretFragment
  }
}
    ${SecretFragment}`;
export const DeleteUserSecret = gql`
    mutation deleteUserSecret($data: DeleteUserSecretInput!) {
  deleteUserSecret(data: $data)
}
    `;
export const IssueAnonymousFeedToken = gql`
    mutation issueAnonymousFeedToken($url: String!) {
  issueAnonymousFeedToken(url: $url) {
    token
  }
}
    `;