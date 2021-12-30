import { Injectable } from '@angular/core';
import { Apollo, gql, QueryRef } from 'apollo-angular';
import {
  FieldWrapper,
  GqlBucket,
  GqlBucketByIdGQL,
  GqlBucketByIdQuery,
  GqlBucketsForUserGQL,
  GqlBucketsForUserQuery,
  GqlBucketsForUserQueryVariables,
  GqlDeleteBucketByIdGQL,
  GqlDeleteBucketByIdMutation,
  GqlUpdateBucketGQL,
  GqlUpdateBucketMutation,
  Scalars
} from '../../generated/graphql';
import { ProfileService } from './profile.service';
import { Observable } from 'rxjs';
import { ApolloQueryResult, FetchResult } from '@apollo/client/core';

@Injectable({
  providedIn: 'root',
})
export class BucketService {
  constructor(
    private readonly apollo: Apollo,
    private readonly profileService: ProfileService,
    private readonly bucketsForUserGQL: GqlBucketsForUserGQL,
    private readonly bucketsByIdGQL: GqlBucketByIdGQL,
    private readonly deleteBucketByIddGQL: GqlDeleteBucketByIdGQL,
    private readonly updteBucketdGQL: GqlUpdateBucketGQL
  ) {}

  getBucketsForUser(): QueryRef<GqlBucketsForUserQuery, GqlBucketsForUserQueryVariables> {
    return this.bucketsForUserGQL.watch({
      email: this.profileService.getEmail(),
    });
  }

  createBucket(title: string): Observable<any> {
    return this.apollo.mutate<any>({
      variables: {
        title,
        email: this.profileService.getEmail(),
      },
      mutation: gql`
        mutation ($title: String!, $email: String!) {
          createBucket(
            data: {
              stream: { create: {} }
              title: $title
              owner: { connect: { email: $email } }
            }
          ) {
            id
            title
          }
        }
      `,
    });
  }
  getBucketsById(bucketId: string): Observable<ApolloQueryResult<GqlBucketByIdQuery>> {
    return this.bucketsByIdGQL.fetch({
      bucketId,
    });
  }

  deleteById(id: FieldWrapper<Scalars['String']>): Observable<FetchResult<GqlDeleteBucketByIdMutation>> {
    return this.deleteBucketByIddGQL.mutate({
      id,
    });
  }

  updateBucket(bucket: GqlBucket): Observable<FetchResult<GqlUpdateBucketMutation>> {
    return this.updteBucketdGQL.mutate({
      id: bucket.id,
      description: bucket.description || '',
      listed: bucket.listed,
      title: bucket.title,
    });
  }

  updateFilterExpression(
    bucketId: string,
    filterExpressions: string
  ): Observable<any> {
    return this.apollo.mutate<any>({
      variables: {
        id: bucketId,
        filterExpressions,
      },
      mutation: gql`
        mutation ($filters: String!, $id: String!) {
          updateBucket(
            data: { filter_expr: $filterExpressions }
            where: { id: $id }
          ) {
            id
          }
        }
      `,
    });
  }
}
