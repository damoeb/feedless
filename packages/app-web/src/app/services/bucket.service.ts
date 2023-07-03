import { Injectable } from '@angular/core';
import {
  BucketById,
  CreateBuckets,
  DeleteBucket,
  GqlBucket,
  GqlBucketByIdQuery,
  GqlBucketByIdQueryVariables,
  GqlBucketsCreateInput,
  GqlBucketsInput,
  GqlBucketUpdateInput,
  GqlCreateBucketsMutation,
  GqlCreateBucketsMutationVariables,
  GqlDeleteBucketMutation,
  GqlDeleteBucketMutationVariables,
  GqlSearchBucketsOrFeedsQuery,
  GqlSearchBucketsOrFeedsQueryVariables,
  GqlSearchBucketsQuery,
  GqlSearchBucketsQueryVariables,
  GqlUpdateBucketMutation,
  GqlUpdateBucketMutationVariables,
  Maybe,
  SearchBuckets,
  SearchBucketsOrFeeds,
  UpdateBucket,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import {
  BasicBucket,
  BasicNativeFeed,
  Bucket,
  Pagination,
} from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class BucketService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  getBucketById(
    id: string,
    fetchPolicy: FetchPolicy = 'cache-first'
  ): Promise<Bucket> {
    return this.apollo
      .query<GqlBucketByIdQuery, GqlBucketByIdQueryVariables>({
        query: BucketById,
        fetchPolicy,
        variables: {
          data: {
            where: {
              id,
            },
          },
        },
      })
      .then((response) => response.data.bucket);
  }

  async search(
    data: GqlBucketsInput,
    fetchPolicy: FetchPolicy = 'cache-first'
  ): Promise<{ buckets: Array<BasicBucket>; pagination: Pagination }> {
    return this.apollo
      .query<GqlSearchBucketsQuery, GqlSearchBucketsQueryVariables>({
        query: SearchBuckets,
        variables: {
          data,
        },
        fetchPolicy,
      })
      .then((response) => response.data.buckets);
  }

  async searchBucketsOrFeeds(
    data: GqlBucketsInput,
    fetchPolicy: FetchPolicy = 'cache-first'
  ): Promise<
    Array<{ bucket?: Maybe<BasicBucket>; feed?: Maybe<BasicNativeFeed> }>
  > {
    return this.apollo
      .query<
        GqlSearchBucketsOrFeedsQuery,
        GqlSearchBucketsOrFeedsQueryVariables
      >({
        query: SearchBucketsOrFeeds,
        variables: {
          data,
        },
        fetchPolicy,
      })
      .then((response) => response.data.bucketsOrNativeFeeds);
  }

  async deleteBucket(id: string, keepFeeds: boolean): Promise<void> {
    await this.apollo.mutate<
      GqlDeleteBucketMutation,
      GqlDeleteBucketMutationVariables
    >({
      mutation: DeleteBucket,
      variables: {
        data: {
          keepFeeds,
          where: {
            id,
          },
        },
      },
    });
  }

  createBuckets(data: GqlBucketsCreateInput): Promise<Pick<GqlBucket, 'id'>[]> {
    return this.apollo
      .mutate<GqlCreateBucketsMutation, GqlCreateBucketsMutationVariables>({
        mutation: CreateBuckets,
        variables: {
          data,
        },
      })
      .then((response) => response.data.createBuckets);
  }

  updateBucket(data: GqlBucketUpdateInput): Promise<Pick<GqlBucket, 'id'>> {
    return this.apollo
      .mutate<GqlUpdateBucketMutation, GqlUpdateBucketMutationVariables>({
        mutation: UpdateBucket,
        variables: {
          data,
        },
      })
      .then((response) => response.data.updateBucket);
  }
}
