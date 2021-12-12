import { Injectable } from '@angular/core';
import { Apollo, gql, QueryRef } from 'apollo-angular';
import { FieldWrapper, GqlBucket, Scalars } from '../../generated/graphql';
import { ProfileService } from './profile.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class BucketService {
  constructor(
    private readonly apollo: Apollo,
    private readonly profileService: ProfileService
  ) {}

  getBucketsForUser(): QueryRef<any> {
    return this.apollo.watchQuery<any>({
      variables: {
        email: this.profileService.getEmail(),
      },
      query: gql`
        query ($email: String!) {
          findFirstUser(where: { email: { equals: $email } }) {
            id
            email
            name
            notebooks(orderBy: { name: asc }) {
              id
              name
              description
              readonly
              streamId
            }
            buckets(orderBy: { title: asc }) {
              id
              title
              streamId
              in_focus
              listed
              subscriptions(orderBy: { title: asc }) {
                id
                ownerId
                title
                tags
                lastUpdatedAt
                feed {
                  id
                  title
                  feed_url
                  home_page_url
                  status
                  broken
                  ownerId
                  lastUpdatedAt
                  is_private
                }
              }
            }
          }
        }
      `,
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
  getBucketsById(bucketId: string): Observable<any> {
    return this.apollo.query<any>({
      variables: {
        bucketId,
      },
      query: gql`
        query ($bucketId: String!) {
          bucket(where: { id: $bucketId }) {
            id
            title
            description
            listed
            in_focus
            streamId
            subscriptions(orderBy: { title: asc }) {
              id
              feed {
                id
                status
                broken
                ownerId
              }
            }
          }
        }
      `,
    });
  }

  deleteById(id: FieldWrapper<Scalars['String']>): Observable<any> {
    return this.apollo.mutate<any>({
      variables: {
        id,
      },
      mutation: gql`
        mutation ($id: String!) {
          deleteBucket(where: { id: $id }) {
            id
          }
        }
      `,
    });
  }

  updateBucket(bucket: GqlBucket): Observable<any> {
    return this.apollo.mutate<any>({
      variables: {
        id: bucket.id,
        description: bucket.description || '',
        listed: bucket.listed,
        title: bucket.title,
      },
      mutation: gql`
        mutation (
          $id: String!
          $title: String!
          $description: String!
          $listed: Boolean!
        ) {
          updateBucket(
            where: { id: $id }
            data: {
              description: { set: $description }
              listed: { set: $listed }
              title: { set: $title }
            }
          ) {
            id
          }
        }
      `,
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
