import { Injectable } from '@angular/core';
import {
  NativeFeedById,
  CreateBucket,
  GqlNativeFeedByIdQuery,
  GqlNativeFeedByIdQueryVariables, GqlBucketCreateInput, GqlCreateBucketMutation,
  GqlCreateBucketMutationVariables, GenericFeedById, GqlGenericFeedByIdQueryVariables, GqlGenericFeedByIdQuery
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

export type ActualNativeFeed = { __typename?: 'NativeFeed', id: string, title: string, description: string, domain: string, websiteUrl: string, feedUrl: string, status: string, lastUpdatedAt?: any | null, genericFeed?: { __typename?: 'GenericFeed', id: string } | null }
export type ActualGenericFeed = { __typename?: 'GenericFeed', id: string, feedRule: string, nativeFeedId: string }

@Injectable({
  providedIn: 'root'
})
export class FeedService {

  constructor(private readonly apollo: ApolloClient<any>) { }

  getNativeFeedById(id: string): Promise<ActualNativeFeed> {
    return this.apollo
      .query<GqlNativeFeedByIdQuery, GqlNativeFeedByIdQueryVariables>({
        query: NativeFeedById,
        variables: {
          id
        }
      })
      .then(response => {
        return response.data.nativeFeed;
      });

  }

  getGenericFeedById(id: string): Promise<ActualGenericFeed> {
    return this.apollo
      .query<GqlGenericFeedByIdQuery, GqlGenericFeedByIdQueryVariables>({
        query: GenericFeedById,
        variables: {
          id
        }
      })
      .then(response => {
        return response.data.genericFeed;
      });

  }
}
