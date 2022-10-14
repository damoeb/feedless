import { Injectable } from '@angular/core';
import {
  BucketById,
  CreateBucket,
  GqlBucketByIdQuery,
  GqlBucketByIdQueryVariables, GqlBucketCreateInput, GqlCreateBucketMutation,
  GqlCreateBucketMutationVariables
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

export type ActualImporter = { __typename?: 'Importer', id: string, active: boolean, feed: { __typename?: 'NativeFeed', id: string, websiteUrl: string, title: string, description: string, feedUrl: string, status: string, genericFeed?: { __typename?: 'GenericFeed', id: string } | null } };
export type ActualBucket = { __typename?: 'Bucket', id: string, name: string, description: string, streamId: string, websiteUrl?: string | null, lastUpdatedAt?: any | null, createdAt: any, importers?: Array<ActualImporter> | null };

@Injectable({
  providedIn: 'root'
})
export class BucketService {

  constructor(private readonly apollo: ApolloClient<any>) { }

  getBucketById(id: string): Promise<ActualBucket> {
    return this.apollo
      .query<GqlBucketByIdQuery, GqlBucketByIdQueryVariables>({
        query: BucketById,
        variables: {
          data: {
            id
          }
        }
      })
      .then(response => {
        return response.data.bucketById.bucket;
      });

  }

  createBucket(data: GqlBucketCreateInput): Promise<{ __typename?: "Bucket"; id: string } | ActualBucket> {
    return this.apollo
      .mutate<GqlCreateBucketMutation, GqlCreateBucketMutationVariables>({
        mutation: CreateBucket,
        variables: {
          data
        }
      })
      .then(response => {
        return response.data.createBucket.bucket;
      });

  }
}
