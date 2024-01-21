import { Injectable } from '@angular/core';
import {
  GqlRemoteNativeFeedInput,
  GqlRemoteNativeFeedQuery,
  GqlRemoteNativeFeedQueryVariables,
  RemoteNativeFeed
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { RemoteFeed, RemoteFeedItem } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class FeedService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  async remoteFeedContent(
    data: GqlRemoteNativeFeedInput,
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
}
