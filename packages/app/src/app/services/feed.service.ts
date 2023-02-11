import { Injectable } from '@angular/core';
import {
  CreateGenericFeed,
  CreateNativeFeed,
  DeleteGenericFeed,
  DeleteNativeFeed,
  DiscoverFeeds,
  GenericFeedById,
  GqlBucket,
  GqlContent,
  GqlCreateGenericFeedMutation,
  GqlCreateGenericFeedMutationVariables,
  GqlCreateNativeFeedMutation,
  GqlCreateNativeFeedMutationVariables,
  GqlDeleteGenericFeedMutation,
  GqlDeleteGenericFeedMutationVariables,
  GqlDeleteNativeFeedMutation,
  GqlDeleteNativeFeedMutationVariables,
  GqlDiscoverFeedsInput,
  GqlDiscoverFeedsQuery,
  GqlDiscoverFeedsQueryVariables,
  GqlEnclosure,
  GqlFeedDiscoveryDocument,
  GqlFeedDiscoveryResponse,
  GqlFetchOptions,
  GqlGenericFeed,
  GqlGenericFeedByIdQuery,
  GqlGenericFeedByIdQueryVariables,
  GqlGenericFeedCreateInput,
  GqlGenericFeedUpdateInput,
  GqlImporter,
  GqlNativeFeed,
  GqlNativeFeedByIdQuery,
  GqlNativeFeedByIdQueryVariables,
  GqlNativeFeedCreateInput, GqlNativeFeedsInput, GqlNativeFeedWhereInput,
  GqlParserOptions,
  GqlRefineOptions,
  GqlRemoteNativeFeedQuery,
  GqlRemoteNativeFeedQueryVariables,
  GqlSearchNativeFeedsQuery,
  GqlSearchNativeFeedsQueryVariables,
  GqlSelectors,
  GqlTransientGenericFeed,
  GqlTransientNativeFeed,
  GqlUpdateGenericFeedMutation,
  GqlUpdateGenericFeedMutationVariables,
  Maybe,
  NativeFeedById,
  RemoteNativeFeed,
  SearchNativeFeeds,
  UpdateGenericFeed
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { Pagination } from './pagination.service';

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
  | 'articlesCount'
>;
export type NativeFeed = BasicNativeFeed & {
  genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
  importers: Array<
    Pick<
      GqlImporter,
      'id' | 'autoRelease' | 'createdAt' | 'nativeFeedId' | 'bucketId'
    > & {
      bucket: Pick<
        GqlBucket,
        | 'id'
        | 'title'
        | 'description'
        | 'imageUrl'
        | 'streamId'
        | 'websiteUrl'
        | 'lastUpdatedAt'
        | 'createdAt'
      >;
    }
  >;
};
export type TransientNativeFeed = Pick<
  GqlTransientNativeFeed,
  'url' | 'type' | 'description' | 'title'
>;
export type Selectors = Pick<GqlSelectors,
  'linkXPath' | 'extendContext' | 'dateXPath' | 'contextXPath' | 'dateIsStartOfEvent' | 'paginationXPath'>;
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
      | 'hasFulltext'
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

export type FeedDiscoveryResult =     Pick<GqlFeedDiscoveryResponse, 'failed' | 'errorMessage' | 'websiteUrl'>
  & { genericFeeds: { fetchOptions: Pick<GqlFetchOptions, 'prerender' | 'websiteUrl' | 'prerenderWithoutMedia' | 'prerenderScript'
      | 'prerenderWaitUntil'>;
    parserOptions: Pick<GqlParserOptions, 'strictMode'>;
    feeds: Array<(
      Pick<GqlTransientGenericFeed, 'feedUrl' | 'hash' | 'score' | 'count'>
      & {
        selectors: Selectors;
        samples: Array<(
        Pick<GqlContent, 'title' | 'description' | 'hasFulltext' | 'contentTitle' | 'contentText' | 'contentRaw' | 'contentRawMime'
          | 'url' | 'imageUrl' | 'publishedAt' | 'updatedAt' | 'tags' | 'createdAt'>
        & {
          enclosures?: Maybe<Array<Pick<GqlEnclosure, 'length' | 'type' | 'url'>>>; }
        )>; }
      )>; }; nativeFeeds?: Maybe<Array<Pick<GqlTransientNativeFeed, 'url' | 'type' | 'description' | 'title'>>>;
      document?: Maybe<Pick<GqlFeedDiscoveryDocument, 'mimeType' | 'htmlBody' | 'title' | 'description' | 'language' | 'imageUrl'
        | 'favicon'>>; }
  ;

export type GenericFeed = Pick<GqlGenericFeed, 'id' | 'feedUrl' | 'hash' | 'nativeFeedId' | 'createdAt'>
  & {
  specification: {
    selectors: Selectors;
    parserOptions: Pick<GqlParserOptions, 'strictMode'>;
    fetchOptions: Pick<GqlFetchOptions, 'prerender' | 'websiteUrl' | 'prerenderWithoutMedia' | 'prerenderScript' | 'prerenderWaitUntil'>;
    refineOptions: Pick<GqlRefineOptions, 'filter' | 'recovery'>;
  };
};

export type PagedNativeFeeds = {
  nativeFeeds: Array<BasicNativeFeed>;
  pagination: Pagination;
};
export type RemoteFeedItem = Pick<GqlContent, 'url' | 'title' | 'contentText' | 'publishedAt' | 'startingAt'>;

@Injectable({
  providedIn: 'root',
})
export class FeedService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  getNativeFeed(data: GqlNativeFeedWhereInput): Promise<NativeFeed> {
    return this.apollo
      .query<GqlNativeFeedByIdQuery, GqlNativeFeedByIdQueryVariables>({
        query: NativeFeedById,
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

  // getGenericFeedById(id: string): Promise<GenericFeed> {
  //   return this.apollo
  //     .query<GqlGenericFeedByIdQuery, GqlGenericFeedByIdQueryVariables>({
  //       query: GenericFeedById,
  //       variables: {
  //         data: {
  //           where: {
  //             id,
  //           },
  //         },
  //       },
  //     })
  //     .then((response) => response.data.genericFeed);
  // }

  searchNativeFeeds(
    data: GqlNativeFeedsInput,
  ): Promise<PagedNativeFeeds> {
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

  async deleteGenericFeed(id: string): Promise<void> {
    await this.apollo.mutate<
      GqlDeleteGenericFeedMutation,
      GqlDeleteGenericFeedMutationVariables
    >({
      mutation: DeleteGenericFeed,
      variables: {
        data: {
          genericFeed: { id },
        },
      },
    });
  }

  async remoteFeedContent(url: string): Promise<Array<RemoteFeedItem>> {
    return this.apollo
      .query<GqlRemoteNativeFeedQuery, GqlRemoteNativeFeedQueryVariables>({
        query: RemoteNativeFeed,
        variables: {
          url,
        },
      })
      .then((response) => response.data.remoteNativeFeed.items);
  }

  async createNativeFeed(
    data: GqlNativeFeedCreateInput
  ): Promise<Pick<GqlNativeFeed, 'id'>> {
    return this.apollo
      .mutate<
        GqlCreateNativeFeedMutation,
        GqlCreateNativeFeedMutationVariables
      >({
        mutation: CreateNativeFeed,
        variables: {
          data,
        },
      })
      .then((response) => response.data.createNativeFeed);
  }

  async createGenericFeed(data: GqlGenericFeedCreateInput): Promise<
    GenericFeed
  > {
    return this.apollo
      .mutate<
        GqlCreateGenericFeedMutation,
        GqlCreateGenericFeedMutationVariables
      >({
        mutation: CreateGenericFeed,
        variables: {
          data,
        },
      })
      .then((response) => response.data.createGenericFeed);
  }

  async updateGenericFeed(
    data: GqlGenericFeedUpdateInput
  ): Promise<
    GenericFeed
  > {
    return this.apollo
      .mutate<
        GqlUpdateGenericFeedMutation,
        GqlUpdateGenericFeedMutationVariables
      >({
        mutation: UpdateGenericFeed,
        variables: {
          data,
        },
      })
      .then((response) => response.data.updateGenericFeed);
  }

  getGenericFeedById(id: string): Promise<GenericFeed> {
    return this.apollo
      .query<GqlGenericFeedByIdQuery, GqlGenericFeedByIdQueryVariables>({
        query: GenericFeedById,
        variables: {
          data: {
            where: {
              id,
            },
          },
        },
      })
      .then((response) => response.data.genericFeed as GenericFeed);
  }
}
