import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import {
  FieldWrapper,
  GqlBucket,
  GqlQuery,
  GqlUser,
  Maybe,
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
            feeds {
              title
              feedType
            }
            buckets {
              id
              title
              streamId
              subscriptions {
                ownerId
                feed {
                  feed_url
                  title
                  status
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
      query: gql`query {
        bucket(where: { id: "${bucketId}" }) {
          id
          title
          description
          listed
          streamId
          subscriptions {
            id
            tags
            feed {
              title
              feed_url
              status
              streamId
            }
            createdAt
            updatedAt
          }
        }
      }
      `,
    });
  }
}
