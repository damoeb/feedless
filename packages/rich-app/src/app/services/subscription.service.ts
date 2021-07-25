import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { GqlSubscription } from '../../generated/graphql';

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  constructor(private readonly apollo: Apollo) {}

  discoverFeeds(queryString: string) {
    return this.apollo.query<any>({
      query: gql`query {
        discoverFeedsByQuery(query: "${queryString}") {
          id
          title
          description
          url
          type
        }
      }
      `,
    });
  }

  createSubscription(feedUrl: string, bucketId: string) {
    return this.apollo.mutate<any>({
      mutation: gql`
        mutation {
          subscribeToFeed(feedUrl: "${feedUrl}", bucketId: "${bucketId}", email: "karl@may.ch") {
            id
          }
        }
      `,
    });
  }

  updateSubscription(subscription: GqlSubscription, tags: string[]) {
    return this.apollo.mutate<any>({
      variables: {
        tags,
      },
      mutation: gql`
        mutation($tags: [String]) {
          updateSubscription(
            data: { tags: { set: $tags } }
            where: { id: "${subscription.id}" }
          ) {
            id
          }
        }
      `,
    });
  }

  unsubscribe(id: string) {
    return this.apollo.mutate<any>({
      mutation: gql`
        mutation {
          deleteSubscription(where: {id: "${id}"}) {
            id
          }
        }
      `,
    });
  }

  findById(id: string) {
    return this.apollo.query<any>({
      query: gql`
        query {
          subscription(where: { id: "${id}" }) {
            id
            tags
            feed {
              title
              feed_url
              status
              streamId
            }
            updatedAt
            createdAt
          }
        }

      `,
    });
  }
}
