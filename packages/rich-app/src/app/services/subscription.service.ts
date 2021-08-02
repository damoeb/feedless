import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { GqlSubscription } from '../../generated/graphql';

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  constructor(private readonly apollo: Apollo) {}

  discoverFeedsByUrl(url: string) {
    return this.apollo.query<any>({
      variables: {
        url,
      },
      query: gql`
        query ($url: String!) {
          discoverFeedsByUrl(url: $url) {
            nativeFeeds {
              feed_url
              title
              description
            }
            generatedFeeds {
              url
              feeds {
                rule {
                  count
                  score
                  linkXPath
                  contextXPath
                  extendContext
                }
                articles {
                  title
                  link
                  text
                }
              }
            }
          }
        }
      `,
    });
  }

  searchFeeds(queryString: string) {
    return this.apollo.query<any>({
      variables: {
        q: queryString,
      },
      query: gql`
        query ($q: String!) {
          feeds(take: 10, where: { fulltext_data: { contains: $q } }) {
            title
            description
            feed_url
            home_page_url
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
