import {
  GqlAuthentication,
  GqlBase64Data,
  GqlBoundingBox,
  GqlCookieValue,
  GqlDomActionSelect,
  GqlDomElementByName,
  GqlDomElementByXPath,
  GqlFeature,
  GqlFeatureBooleanValue,
  GqlFeatureIntValue,
  GqlPlan,
  GqlPlanSubscription,
  GqlPlugin,
  GqlPluginExecution,
  GqlProfile,
  GqlRemoteNativeFeed,
  GqlRequestHeader,
  GqlRetention,
  GqlScrapeDebugOptions,
  GqlScrapeDebugResponse,
  GqlScrapeDebugTimes,
  GqlScrapedField,
  GqlScrapedReadability,
  GqlScrapedSingleFieldValue,
  GqlScrapePage,
  GqlScrapeRequest,
  GqlScrapeResponse,
  GqlScrapeSelector,
  GqlScrapeSelectorExpose,
  GqlScrapeSelectorExposeField,
  GqlScrapeSelectorExposeFieldTextValue,
  GqlScrapeSelectorExposeFieldValue,
  GqlSegment,
  GqlSelectors,
  GqlSourceSubscription,
  GqlTextData,
  GqlUser,
  GqlUserSecret,
  GqlViewPort,
  GqlWebDocument,
  Maybe
} from '../../generated/graphql';

export type SourceSubscription = Pick<
  GqlSourceSubscription,
  'id' | 'ownerId' | 'title' | 'description' | 'visibility' | 'createdAt' | 'disabledFrom'
> & {
  segmented?: Maybe<
    Pick<
      GqlSegment,
      'digest' | 'scheduleExpression' | 'size' | 'sortAsc' | 'sortBy'
    >
  >;
  retention: Pick<GqlRetention, 'maxAgeDays' | 'maxItems'>;
  sources: Array<
    Pick<GqlScrapeRequest, 'id'> & {
    debug?: Maybe<
      Pick<
        GqlScrapeDebugOptions,
        'network' | 'html' | 'console' | 'cookies' | 'screenshot'
      >
    >;
    emit: Array<{
      selectorBased?: Maybe<
        Pick<GqlScrapeSelector, 'min' | 'max'> & {
        xpath: Pick<GqlDomElementByXPath, 'value'>;
        expose: Pick<GqlScrapeSelectorExpose, 'pixel'> & {
          fields?: Maybe<
            Array<
              Pick<GqlScrapeSelectorExposeField, 'min' | 'max' | 'name'> & {
              value?: Maybe<
                Pick<GqlScrapeSelectorExposeFieldValue, 'set'> & {
                html?: Maybe<{
                  xpath: Pick<GqlDomElementByXPath, 'value'>;
                }>;
                text?: Maybe<
                  Pick<GqlScrapeSelectorExposeFieldTextValue, 'regex'>
                >;
              }
              >;
            }
            >
          >;
          transformers?: Maybe<
            Array<
              Pick<GqlPluginExecution, 'pluginId'> & {
              params?: Maybe<{
                genericFeed?: Maybe<
                  Pick<
                    GqlSelectors,
                    | 'contextXPath'
                    | 'dateXPath'
                    | 'dateIsStartOfEvent'
                    | 'extendContext'
                    | 'linkXPath'
                  >
                >;
              }>;
            }
            >
          >;
        };
      }
      >;
      imageBased?: Maybe<{
        boundingBox: Pick<GqlBoundingBox, 'x' | 'y' | 'w' | 'h'>;
      }>;
    }>;
    page: Pick<GqlScrapePage, 'url' | 'timeout'> & {
      prerender?: Maybe<{
        viewport?: Maybe<
          Pick<GqlViewPort, 'height' | 'width' | 'isLandscape' | 'isMobile'>
        >;
      }>;
      actions?: Maybe<
        Array<{
          type?: Maybe<{ element: Pick<GqlDomElementByXPath, 'value'> }>;
          cookie?: Maybe<Pick<GqlCookieValue, 'value'>>;
          click?: Maybe<{
            element?: Maybe<{
              xpath?: Maybe<Pick<GqlDomElementByXPath, 'value'>>;
              name?: Maybe<Pick<GqlDomElementByName, 'value'>>;
            }>;
          }>;
          header?: Maybe<Pick<GqlRequestHeader, 'value' | 'name'>>;
          select?: Maybe<
            Pick<GqlDomActionSelect, 'selectValue'> & {
            element: Pick<GqlDomElementByXPath, 'value'>;
          }
          >;
          wait?: Maybe<{
            element?: Maybe<{
              name?: Maybe<Pick<GqlDomElementByName, 'value'>>;
              xpath?: Maybe<Pick<GqlDomElementByXPath, 'value'>>;
            }>;
          }>;
        }>
      >;
    };
  }
  >;
};

export type WebDocument = Pick<
  GqlWebDocument,
  | 'id'
  | 'url'
  | 'imageUrl'
  | 'createdAt'
  | 'contentText'
  | 'contentRaw'
  | 'contentRawMime'
  | 'contentTitle'
  | 'publishedAt'
  | 'startingAt'
>;

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

export type ScrapedElement = {
  image?: Maybe<{
    boundingBox: Pick<GqlBoundingBox, 'y' | 'x' | 'w' | 'h'>;
    data: Pick<GqlBase64Data, 'base64Data'>;
  }>;
  selector?: Maybe<{
    xpath: Pick<GqlDomElementByXPath, 'value'>;
    html?: Maybe<Pick<GqlTextData, 'data'>>;
    pixel?: Maybe<Pick<GqlBase64Data, 'base64Data'>>;
    text?: Maybe<Pick<GqlTextData, 'data'>>;
    fields?: Maybe<
      Array<
        Pick<GqlScrapedField, 'name'> & {
        xpath?: Maybe<Pick<GqlDomElementByXPath, 'value'>>;
        value?: Maybe<{
          one?: Maybe<Pick<GqlScrapedSingleFieldValue, 'mimeType' | 'data'>>;
          many?: Maybe<
            Array<Pick<GqlScrapedSingleFieldValue, 'mimeType' | 'data'>>
          >;
          nested?: Maybe<
            Array<
              Pick<GqlScrapedField, 'name'> & {
              xpath?: Maybe<Pick<GqlDomElementByXPath, 'value'>>;
            }
            >
          >;
        }>;
      }
      >
    >;
  }>;
};

export type ScrapeResponse = Pick<
  GqlScrapeResponse,
  'url' | 'failed' | 'errorMessage'
> & {
  debug: Pick<
    GqlScrapeDebugResponse,
    'console' | 'cookies' | 'contentType' | 'statusCode' | 'screenshot' | 'html'
  > & {
    metrics: Pick<GqlScrapeDebugTimes, 'queue' | 'render'>;
    viewport?: Maybe<Pick<GqlViewPort, 'width' | 'height'>>;
  };
  elements: Array<ScrapedElement>;
};

export type RemoteFeedItem = Pick<
  GqlWebDocument,
  | 'url'
  | 'contentTitle'
  | 'contentText'
  | 'contentRaw'
  | 'contentRawMime'
  | 'publishedAt'
  | 'startingAt'
>;

export type RemoteFeed = Pick<
  GqlRemoteNativeFeed,
  'title' | 'description' | 'websiteUrl' | 'feedUrl'
> & { items?: Array<RemoteFeedItem> };

export type Feature = Pick<GqlFeature, 'name' | 'state'> & {
  value?: Maybe<{
    boolVal?: Maybe<Pick<GqlFeatureBooleanValue, 'value'>>;
    numVal?: Maybe<Pick<GqlFeatureIntValue, 'value'>>;
  }>;
};
export type Plan = Pick<
  GqlPlan,
  'id' | 'name' | 'availability' | 'isPrimary' | 'currentCosts' | 'beforeCosts'
> & { features: Array<Feature> };

export type Profile = (
  Pick<GqlProfile, 'minimalFeatureState' | 'isLoggedIn'>
  & {
  user?: Maybe<(
    Pick<GqlUser, 'id' | 'hasAcceptedTerms' | 'purgeScheduledFor'>
    & {
    secrets: Array<Pick<GqlUserSecret, 'id' | 'validUntil' | 'type' | 'lastUsed' | 'value' | 'valueMasked'>>, subscription?: Maybe<(
      Pick<GqlPlanSubscription, 'expiry' | 'startedAt'>
      & { plan: Pick<GqlPlan, 'id' | 'name' | 'availability' | 'isPrimary' | 'currentCosts' | 'beforeCosts'> }
      )>
  }
    )>
}
  );

export type UserSecret = Pick<
  GqlUserSecret,
  | 'id'
  | 'validUntil'
  | 'type'
  | 'lastUsed'
  | 'value'
  | 'valueMasked'
>;

export type FlatFeature = Pick<GqlFeature, 'name' | 'state'>;

export type Plugin = Pick<GqlPlugin, 'id' | 'description' | 'name' | 'type'>;
