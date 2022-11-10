import { Injectable } from '@angular/core';
import {
  CreateNativeFeed,
  DeleteGenericFeed,
  DeleteNativeFeed,
  DiscoverFeeds,
  GenericFeedById,
  GqlCreateNativeFeedMutation,
  GqlCreateNativeFeedMutationVariables,
  GqlDeleteGenericFeedMutation,
  GqlDeleteGenericFeedMutationVariables,
  GqlDeleteNativeFeedMutation,
  GqlDeleteNativeFeedMutationVariables,
  GqlDiscoverFeedsQuery,
  GqlDiscoverFeedsQueryVariables,
  GqlFeedDiscoveryResponse,
  GqlGenericFeed,
  GqlGenericFeedByIdQuery,
  GqlGenericFeedByIdQueryVariables,
  GqlTransientGenericFeed,
  GqlNativeFeed,
  GqlNativeFeedByIdQuery,
  GqlNativeFeedByIdQueryVariables,
  GqlNativeFeedCreateInput,
  GqlTransientNativeFeed,
  GqlPagination,
  Maybe,
  NativeFeedById,
  GqlSearchNativeFeedsQuery,
  GqlSearchNativeFeedsQueryVariables,
  SearchNativeFeeds,
  GqlRemoteNativeFeedQuery,
  GqlRemoteNativeFeedQueryVariables, RemoteNativeFeed, GqlContent
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

export type NativeFeed = GqlNativeFeed;
export type GenericFeedRef = Pick<GqlGenericFeed, 'id' | 'feedRule'> & {
  nativeFeed: Pick<GqlNativeFeed, 'id' | 'title'>;
};
export type TransientNativeFeed = Pick<GqlTransientNativeFeed, 'url' | 'type' | 'description' | 'title'>;
export type TransientGenericFeed = Pick<GqlTransientGenericFeed, 'feedUrl' | 'score' | 'linkXPath' | 'extendContext' | 'dateXPath' | 'count' | 'contextXPath'>;
export type FeedDiscoveryResult = Pick<GqlFeedDiscoveryResponse, 'mimeType' | 'failed' | 'errorMessage' | 'title' | 'description' | 'url'>
  & { genericFeeds?: Maybe<Array<TransientGenericFeed>>, nativeFeeds?: Maybe<Array<TransientNativeFeed>> };
export type SearchFeedsResponse = { nativeFeeds: Array<Pick<GqlNativeFeed, 'id' | 'title' | 'description' | 'feedUrl' | 'websiteUrl' | 'createdAt' | 'lastUpdatedAt'>>, pagination: Pick<GqlPagination, 'isEmpty' | 'isFirst' | 'isLast' | 'page' | 'totalPages'> };

export type RemoteFeedItem = Pick<GqlContent, 'url' | 'title' | 'contentText' | 'publishedAt'>;

@Injectable({
  providedIn: 'root',
})
export class FeedService {
  constructor(
    private readonly apollo: ApolloClient<any>
  ) {}

  getNativeFeedById(id: string): Promise<NativeFeed> {
    return this.apollo
      .query<GqlNativeFeedByIdQuery, GqlNativeFeedByIdQueryVariables>({
        query: NativeFeedById,
        variables: {
          id,
        },
      })
      .then((response) => response.data.nativeFeed as NativeFeed);
  }

  discoverFeeds(url: string): Promise<FeedDiscoveryResult> {
    return this.apollo
      .query<GqlDiscoverFeedsQuery, GqlDiscoverFeedsQueryVariables>({
        query: DiscoverFeeds,
        variables: {
          url,
          prerender: false,
        },
      })
      .then((response) => response.data.discoverFeeds);
  }

  getGenericFeedById(id: string): Promise<GenericFeedRef> {
    return this.apollo
      .query<GqlGenericFeedByIdQuery, GqlGenericFeedByIdQueryVariables>({
        query: GenericFeedById,
        variables: {
          id,
        },
      })
      .then((response) => response.data.genericFeed);
  }

  searchNativeFeeds(query: string, page = 0): Promise<SearchFeedsResponse> {
    return this.apollo
      .query<GqlSearchNativeFeedsQuery, GqlSearchNativeFeedsQueryVariables>({
        query: SearchNativeFeeds,
        variables: {
          data: {
            page,
            where: {
              query
            }
          },
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
          nativeFeedId: id,
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
          genericFeedId: id,
        },
      },
    });
  }

  async remoteFeedContent(url: string): Promise<Array<RemoteFeedItem>> {
    return  this.apollo.query<
      GqlRemoteNativeFeedQuery,
      GqlRemoteNativeFeedQueryVariables
    >({
      query: RemoteNativeFeed,
      variables: {
        url
      },
    }).then(response => response.data.remoteNativeFeed.items);
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
}
