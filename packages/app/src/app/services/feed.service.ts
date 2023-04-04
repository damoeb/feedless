import { Injectable } from '@angular/core';
import {
  DeleteNativeFeed,
  DiscoverFeeds,
  GqlContent,
  GqlDeleteNativeFeedMutation,
  GqlDeleteNativeFeedMutationVariables,
  GqlDiscoverFeedsInput,
  GqlDiscoverFeedsQuery,
  GqlDiscoverFeedsQueryVariables,
  GqlEnclosure,
  GqlFeedDiscoveryDocument,
  GqlFeedDiscoveryResponse,
  GqlFetchOptions,
  GqlFilteredContent,
  GqlGenericFeed,
  GqlNativeFeed,
  GqlNativeFeedByIdQuery,
  GqlNativeFeedByIdQueryVariables,
  GqlNativeFeedsPagedInput,
  GqlNativeFeedUpdateInput,
  GqlNativeFeedWhereInput,
  GqlParserOptions,
  GqlRefineOptions,
  GqlRemoteNativeFeed,
  GqlRemoteNativeFeedInput,
  GqlRemoteNativeFeedQuery,
  GqlRemoteNativeFeedQueryVariables,
  GqlSearchNativeFeedsQuery,
  GqlSearchNativeFeedsQueryVariables,
  GqlSelectors,
  GqlTransientGenericFeed,
  GqlTransientNativeFeed,
  GqlUpdateNativeFeedMutation,
  GqlUpdateNativeFeedMutationVariables,
  Maybe,
  NativeFeedById,
  RemoteNativeFeed,
  SearchNativeFeeds,
  UpdateNativeFeed,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { Pagination } from './pagination.service';
import { BasicImporter } from './importer.service';
import { BasicBucket } from './bucket.service';

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
export type TransientGenericFeed = Pick<
  GqlTransientGenericFeed,
  'feedUrl' | 'hash' | 'score' | 'count'
> & {
  selectors: Selectors;
  samples: Array<
    Pick<
      GqlContent,
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
      | 'createdAt'
    > & {
      enclosures?: Maybe<Array<Pick<GqlEnclosure, 'length' | 'type' | 'url'>>>;
    }
  >;
};

export type FeedDiscoveryResult = Pick<
  GqlFeedDiscoveryResponse,
  'failed' | 'errorMessage' | 'websiteUrl'
> & {
  fetchOptions: Pick<
    GqlFetchOptions,
    | 'prerender'
    | 'websiteUrl'
    | 'prerenderWithoutMedia'
    | 'prerenderScript'
    | 'prerenderWaitUntil'
  >;
  genericFeeds: {
    parserOptions: Pick<GqlParserOptions, 'strictMode'>;
    feeds: Array<
      Pick<GqlTransientGenericFeed, 'feedUrl' | 'hash' | 'score' | 'count'> & {
        selectors: Selectors;
        samples: Array<
          Pick<
            GqlContent,
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
            | 'createdAt'
          > & {
            enclosures?: Maybe<
              Array<Pick<GqlEnclosure, 'length' | 'type' | 'url'>>
            >;
          }
        >;
      }
    >;
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
  'id' | 'feedUrl' | 'hash' | 'nativeFeedId' | 'createdAt'
> & {
  specification: {
    selectors: Selectors;
    parserOptions: Pick<GqlParserOptions, 'strictMode'>;
    fetchOptions: Pick<
      GqlFetchOptions,
      | 'prerender'
      | 'websiteUrl'
      | 'prerenderWithoutMedia'
      | 'prerenderScript'
      | 'prerenderWaitUntil'
    >;
    refineOptions: Pick<GqlRefineOptions, 'filter' | 'recovery'>;
  };
};

export type PagedNativeFeeds = {
  nativeFeeds: Array<BasicNativeFeed>;
  pagination: Pagination;
};
export type RemoteFeedItem = Pick<GqlFilteredContent, 'omitted'> & {
  content: Pick<
    GqlContent,
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

@Injectable({
  providedIn: 'root',
})
export class FeedService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  getNativeFeed(
    data: GqlNativeFeedWhereInput,
    fetchPolicy: FetchPolicy = 'cache-first'
  ): Promise<NativeFeed> {
    return this.apollo
      .query<GqlNativeFeedByIdQuery, GqlNativeFeedByIdQueryVariables>({
        query: NativeFeedById,
        fetchPolicy,
        variables: {
          data,
        },
      })
      .then((response) => response.data.nativeFeed as NativeFeed);
  }

  discoverFeeds(data: GqlDiscoverFeedsInput): Promise<FeedDiscoveryResult> {
    return this.apollo
      .query<GqlDiscoverFeedsQuery, GqlDiscoverFeedsQueryVariables>({
        query: DiscoverFeeds,
        variables: {
          data,
        },
      })
      .then((response) => response.data.discoverFeeds);
  }

  searchNativeFeeds(data: GqlNativeFeedsPagedInput): Promise<PagedNativeFeeds> {
    return this.apollo
      .query<GqlSearchNativeFeedsQuery, GqlSearchNativeFeedsQueryVariables>({
        query: SearchNativeFeeds,
        variables: {
          data,
        },
      })
      .then((response) => response.data.nativeFeeds);
  }

  async deleteNativeFeed(id: string): Promise<void> {
    await this.apollo.mutate<
      GqlDeleteNativeFeedMutation,
      GqlDeleteNativeFeedMutationVariables
    >({
      mutation: DeleteNativeFeed,
      variables: {
        data: {
          nativeFeed: { id },
        },
      },
    });
  }

  async remoteFeedContent(
    data: GqlRemoteNativeFeedInput
  ): Promise<Array<RemoteFeedItem>> {
    return this.remoteFeed(data).then((response) => response.items);
  }

  async remoteFeed(data: GqlRemoteNativeFeedInput): Promise<RemoteFeed> {
    return this.apollo
      .query<GqlRemoteNativeFeedQuery, GqlRemoteNativeFeedQueryVariables>({
        query: RemoteNativeFeed,
        variables: {
          data,
        },
      })
      .then((response) => response.data.remoteNativeFeed);
  }

  async updateNativeFeed(data: GqlNativeFeedUpdateInput): Promise<any> {
    return this.apollo
      .mutate<
        GqlUpdateNativeFeedMutation,
        GqlUpdateNativeFeedMutationVariables
      >({
        mutation: UpdateNativeFeed,
        variables: {
          data,
        },
      })
      .then((response) => response.data.updateNativeFeed);
  }
}
