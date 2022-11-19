import { Injectable } from '@angular/core';
import {
  CreateNativeFeed,
  DeleteGenericFeed,
  DeleteNativeFeed,
  DiscoverFeeds,
  GenericFeedById,
  GqlContent,
  GqlCreateNativeFeedMutation,
  GqlCreateNativeFeedMutationVariables,
  GqlDeleteGenericFeedMutation,
  GqlDeleteGenericFeedMutationVariables,
  GqlDeleteNativeFeedMutation,
  GqlDeleteNativeFeedMutationVariables,
  GqlDiscoverFeedsQuery,
  GqlDiscoverFeedsQueryVariables, GqlEnclosure,
  GqlFeedDiscoveryResponse,
  GqlGenericFeed,
  GqlGenericFeedByIdQuery,
  GqlGenericFeedByIdQueryVariables,
  GqlNativeFeed,
  GqlNativeFeedByIdQuery,
  GqlNativeFeedByIdQueryVariables,
  GqlNativeFeedCreateInput,
  GqlNativeFeedsWhereInput,
  GqlRemoteNativeFeedQuery,
  GqlRemoteNativeFeedQueryVariables,
  GqlSearchNativeFeedsQuery,
  GqlSearchNativeFeedsQueryVariables,
  GqlTransientGenericFeed,
  GqlTransientNativeFeed,
  Maybe,
  NativeFeedById,
  RemoteNativeFeed,
  SearchNativeFeeds
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
  | 'feedUrl'
  | 'status'
  | 'lastUpdatedAt'
>;
export type NativeFeed = BasicNativeFeed & {
  genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
};
export type GenericFeed = Pick<GqlGenericFeed, 'id' | 'feedRule'> & {
  nativeFeed: BasicNativeFeed;
};
export type TransientNativeFeed = Pick<
  GqlTransientNativeFeed,
  'url' | 'type' | 'description' | 'title'
>;
export type TransientGenericFeed = Pick<GqlTransientGenericFeed, 'feedUrl'
    | 'score' | 'linkXPath' | 'extendContext' | 'dateXPath' | 'count'
    | 'contextXPath'>
  & { samples: Array<(
    Pick<GqlContent, 'title' | 'description' | 'hasFulltext' | 'contentTitle'
      | 'contentText' | 'contentRaw' | 'contentRawMime' | 'url' | 'imageUrl'
      | 'publishedAt' | 'updatedAt' | 'tags' | 'createdAt'>
    & { enclosures?: Maybe<Array<Pick<GqlEnclosure, 'length' | 'type' | 'url'>>> }
    )>; }
  ;
export type FeedDiscoveryResult = Pick<
  GqlFeedDiscoveryResponse,
  'mimeType' | 'failed' | 'errorMessage' | 'title' | 'description' | 'url'
> & {
  genericFeeds?: Maybe<Array<TransientGenericFeed>>;
  nativeFeeds?: Maybe<Array<TransientNativeFeed>>;
};
export type PagedNativeFeeds = {
  nativeFeeds: Array<BasicNativeFeed>;
  pagination: Pagination;
};
export type RemoteFeedItem = Pick<
  GqlContent,
  'url' | 'title' | 'contentText' | 'publishedAt'
>;

@Injectable({
  providedIn: 'root',
})
export class FeedService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  getNativeFeedById(id: string): Promise<NativeFeed> {
    return this.apollo
      .query<GqlNativeFeedByIdQuery, GqlNativeFeedByIdQueryVariables>({
        query: NativeFeedById,
        variables: {
          data: {
            where: {
              id,
            },
          },
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
      .then((response) => response.data.genericFeed);
  }

  searchNativeFeeds(
    where: GqlNativeFeedsWhereInput,
    page = 0
  ): Promise<PagedNativeFeeds> {
    return this.apollo
      .query<GqlSearchNativeFeedsQuery, GqlSearchNativeFeedsQueryVariables>({
        query: SearchNativeFeeds,
        variables: {
          data: {
            page,
            where,
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
}
