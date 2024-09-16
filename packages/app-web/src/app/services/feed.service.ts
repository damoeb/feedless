import { Injectable } from '@angular/core';
import {
  GqlPreviewFeedInput,
  GqlPreviewFeedQuery,
  GqlPreviewFeedQueryVariables,
  GqlRemoteNativeFeedInput,
  GqlRemoteNativeFeedQuery,
  GqlRemoteNativeFeedQueryVariables,
  PreviewFeed,
  RemoteNativeFeed,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { FeedPreview, RemoteFeed, RemoteFeedItem } from '../graphql/types';

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

  private async remoteFeed(
    data: GqlRemoteNativeFeedInput,
  ): Promise<RemoteFeed> {
    return this.apollo
      .query<GqlRemoteNativeFeedQuery, GqlRemoteNativeFeedQueryVariables>({
        query: RemoteNativeFeed,
        variables: {
          data,
        },
      })
      .then((response) => response.data.remoteNativeFeed);
  }

  async previewFeed(data: GqlPreviewFeedInput): Promise<FeedPreview> {
    return this.apollo
      .query<GqlPreviewFeedQuery, GqlPreviewFeedQueryVariables>({
        query: PreviewFeed,
        variables: {
          data,
        },
      })
      .then((response) => response.data.previewFeed);
  }
}
