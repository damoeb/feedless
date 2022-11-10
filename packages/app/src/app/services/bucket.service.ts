import { Injectable } from '@angular/core';
import {
  BucketById,
  CreateBucket,
  DeleteBucket,
  GqlBucket,
  GqlBucketByIdQuery,
  GqlBucketByIdQueryVariables,
  GqlBucketCreateInput,
  GqlCreateBucketMutation,
  GqlCreateBucketMutationVariables,
  GqlDeleteBucketMutation,
  GqlDeleteBucketMutationVariables,
  GqlGenericFeed,
  GqlImporter,
  GqlNativeFeed,
  Maybe,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

export type Bucket = (
  Pick<GqlBucket, 'id' | 'title' | 'description' | 'streamId' | 'websiteUrl' | 'lastUpdatedAt' | 'createdAt'>
  & { importers?: Maybe<Array<(
    Pick<GqlImporter, 'id' | 'autoRelease' | 'createdAt'>
    & { nativeFeed: (
      Pick<GqlNativeFeed, 'id' | 'createdAt' | 'websiteUrl' | 'title' | 'description' | 'feedUrl' | 'status' | 'lastUpdatedAt' | 'domain'>
      & { genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>> }
      ) }
    )>> }
  );

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
          id,
        },
      })
      .then((response) => response.data.bucket);
  }

  async deleteBucket(id: string): Promise<void> {
    await this.apollo.mutate<
      GqlDeleteBucketMutation,
      GqlDeleteBucketMutationVariables
    >({
      mutation: DeleteBucket,
      variables: {
        data: {
          bucketId: id,
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
