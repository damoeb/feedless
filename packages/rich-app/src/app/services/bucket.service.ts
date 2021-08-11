import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import {
  FieldWrapper,
  GqlBucket,
  GqlQuery,
  GqlUser,
  Maybe,
  Scalars,
} from '../../generated/graphql';

@Injectable({
  providedIn: 'root',
})
export class BucketService {
  constructor(private readonly apollo: Apollo) {}

  getBucketsForUser() {
    return this.apollo.watchQuery<any>({
      query: gql`
        query {
          findFirstUser(where: { email: { equals: "karl@may.ch" } }) {
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
              listed
              lastUpdatedAt
              subscriptions(orderBy: { title: asc }) {
                ownerId
                title
                feed {
                  feed_url
                  home_page_url
                  description
                  title
                  status
                  broken
                }
              }
            }
          }
        }
      `,
    });
  }

  createBucket(bucket: Partial<GqlBucket>) {
    return this.apollo.mutate<any>({
      mutation: gql`
        mutation {
          createBucket(
            data: {
              stream: {
                create: {}
              }
              title: "${bucket.title}"
              description: "${bucket.description}"
              listed: ${bucket.listed}
              owner: { connect: { email: "karl@may.ch" } }
            }
          ) {
            id
          }
        }
      `,
    });
  }
  getBucketsById(bucketId: string) {
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
            streamId
            lastUpdatedAt
            subscriptions(orderBy: { title: asc }) {
              id
              tags
              title
              lastUpdatedAt
              feed {
                title
                feed_url
                home_page_url
                description
                status
                streamId
                broken
              }
            }
          }
        }
      `,
    });
  }

  delteById(id: FieldWrapper<Scalars['String']>) {
    // todo mag
  }

  updateBucket(bucket: GqlBucket) {
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

  updateFilterExpression(bucketId: string, filterExpressions: string) {
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
