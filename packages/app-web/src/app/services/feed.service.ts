import { Injectable } from '@angular/core';
import {
  CreateNativeFeeds,
  DeleteNativeFeed,
  DiscoverFeeds,
  GqlCreateNativeFeedsInput,
  GqlCreateNativeFeedsMutation,
  GqlCreateNativeFeedsMutationVariables,
  GqlDeleteNativeFeedMutation,
  GqlDeleteNativeFeedMutationVariables,
  GqlDiscoverFeedsInput,
  GqlDiscoverFeedsQuery,
  GqlDiscoverFeedsQueryVariables,
  GqlNativeFeedByIdQuery,
  GqlNativeFeedByIdQueryVariables,
  GqlNativeFeedsInput,
  GqlNativeFeedUpdateInput,
  GqlNativeFeedWhereInput,
  GqlNativeGenericOrFragmentWatchFeedCreateInput,
  GqlRemoteNativeFeedInput,
  GqlRemoteNativeFeedQuery,
  GqlRemoteNativeFeedQueryVariables,
  GqlSearchNativeFeedsQuery,
  GqlSearchNativeFeedsQueryVariables,
  GqlUpdateNativeFeedMutation,
  GqlUpdateNativeFeedMutationVariables,
  NativeFeedById,
  RemoteNativeFeed,
  SearchNativeFeeds,
  UpdateNativeFeed,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import {
  FeedDiscoveryResult,
  NativeFeed,
  NativeFeeds,
  RemoteFeed,
  RemoteFeedItem,
} from '../graphql/types';

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

  searchNativeFeeds(data: GqlNativeFeedsInput): Promise<NativeFeeds> {
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

  async createNativeFeeds(data: GqlCreateNativeFeedsInput): Promise<void> {
    await this.apollo.mutate<
      GqlCreateNativeFeedsMutation,
      GqlCreateNativeFeedsMutationVariables
    >({
      mutation: CreateNativeFeeds,
      variables: {
        data,
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
