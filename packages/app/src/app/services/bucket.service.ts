import { Injectable } from '@angular/core';
import {
  BucketById,
  CreateBucket, DeleteBucket,
  DeleteImporter,
  GqlBucket,
  GqlBucketByIdQuery,
  GqlBucketByIdQueryVariables,
  GqlBucketCreateInput,
  GqlCreateBucketMutation,
  GqlCreateBucketMutationVariables,
  GqlDeleteBucketMutation,
  GqlDeleteBucketMutationVariables,
  GqlDeleteImporterMutation,
  GqlDeleteImporterMutationVariables,
  GqlGenericFeed,
  GqlImporter,
  GqlNativeFeed,
  Maybe
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

export type Bucket = (
  Pick<GqlBucket, 'id' | 'name' | 'description' | 'streamId' | 'websiteUrl' | 'lastUpdatedAt' | 'createdAt'>
  & { importers?: Maybe<Array<(
    Pick<GqlImporter, 'id' | 'active'>
    & { feed: (
      Pick<GqlNativeFeed, 'id' | 'websiteUrl' | 'title' | 'description' | 'feedUrl' | 'status' | 'lastUpdatedAt' | 'domain'>
      & { genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>> }
      ) }
    )>> }
  );

@Injectable({
  providedIn: 'root'
})
export class BucketService {

  constructor(private readonly apollo: ApolloClient<any>) { }

  getBucketById(id: string): Promise<Bucket> {
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

  async deleteBucket(id: string): Promise<void> {
    await this.apollo
      .mutate<GqlDeleteBucketMutation, GqlDeleteBucketMutationVariables>({
        mutation: DeleteBucket,
        variables: {
          id
        }
      })
  }


  createBucket(data: GqlBucketCreateInput): Promise<Pick<GqlBucket, 'id'>> {
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
