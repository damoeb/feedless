import { Injectable } from '@angular/core';
import {
  BucketById,
  CreateBucket,
  DeleteBucket,
  GqlBucket,
  GqlBucketByIdQuery,
  GqlBucketByIdQueryVariables,
  GqlBucketCreateInput,
  GqlBucketsPagedInput,
  GqlCreateBucketMutation,
  GqlCreateBucketMutationVariables,
  GqlDeleteBucketMutation,
  GqlDeleteBucketMutationVariables,
  GqlGenericFeed,
  GqlSearchBucketsQuery,
  GqlSearchBucketsQueryVariables,
  Maybe,
  SearchBuckets,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { BasicImporter } from './importer.service';
import { BasicNativeFeed } from './feed.service';
import { Pagination } from './pagination.service';

export type BasicBucket = Pick<
  GqlBucket,
  | 'id'
  | 'title'
  | 'description'
  | 'imageUrl'
  | 'streamId'
  | 'websiteUrl'
  | 'lastUpdatedAt'
  | 'createdAt'
  | 'importersCount'
  | 'articlesCount'
>;

export type Bucket = BasicBucket & {
  importers?: Maybe<
    Array<
      BasicImporter & {
        nativeFeed: BasicNativeFeed & {
          genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
        };
      }
    >
  >;
};

@Injectable({
  providedIn: 'root',
})
export class BucketService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  getBucketById(id: string): Promise<Bucket> {
    return this.apollo
      .query<GqlBucketByIdQuery, GqlBucketByIdQueryVariables>({
        query: BucketById,
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

  search(
    data: GqlBucketsPagedInput
  ): Promise<{ buckets: Array<BasicBucket>; pagination: Pagination }> {
    return this.apollo
      .query<GqlSearchBucketsQuery, GqlSearchBucketsQueryVariables>({
        query: SearchBuckets,
        variables: {
          data,
        },
      })
      .then((response) => response.data.buckets);
  }

  async deleteBucket(id: string): Promise<void> {
    await this.apollo.mutate<
      GqlDeleteBucketMutation,
      GqlDeleteBucketMutationVariables
    >({
      mutation: DeleteBucket,
      variables: {
        data: {
          where: {
            id,
          },
        },
      },
    });
  }

  createBucket(data: GqlBucketCreateInput): Promise<Pick<GqlBucket, 'id'>> {
    return this.apollo
      .mutate<GqlCreateBucketMutation, GqlCreateBucketMutationVariables>({
        mutation: CreateBucket,
        variables: {
          data,
        },
      })
      .then((response) => response.data.createBucket);
  }
}
