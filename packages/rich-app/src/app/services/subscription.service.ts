import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import {
  GqlNativeFeedRef,
  GqlProxyFeed,
  GqlSubscription,
} from '../../generated/graphql';

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
              home_page_url
              feeds {
                feed_url
                home_page_url
                title
                rule {
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
      variables: {
        feedUrl,
        email: 'karl@may.ch',
        bucketId,
      },
      mutation: gql`
        mutation subscribeToFeed(
          $feedUrl: String!
          $bucketId: String!
          $email: String!
        ) {
          subscribeToFeed(
            feedUrl: $feedUrl
            bucketId: $bucketId
            email: $email
          ) {
            id
          }
        }
      `,
    });
  }

  updateSubscription(
    subscriptionId: string,
    feedUrl: string,
    title: string = '',
    tags: string[] = []
  ) {
    return this.apollo.mutate<any>({
      variables: {
        tags,
        title,
        feedUrl,
        subscriptionId,
      },
      mutation: gql`
        mutation updateSubscription(
          $feedUrl: String!
          $subscriptionId: String!
          $title: String!
          $tags: JSON!
        ) {
          updateSubscription(
            data: {
              title: { set: $title }
              tags: $tags
              feed: {
                connectOrCreate: {
                  where: { feed_url: $feedUrl }
                  create: { feed_url: $feedUrl, stream: { create: {} } }
                }
              }
            }
            where: { id: $subscriptionId }
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
              home_page_url
              description
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

  async subscribeToNativeFeed(feed: GqlNativeFeedRef) {}

  async subscribeToGeneratedFeed(feed: GqlProxyFeed) {}

  disableById(id: string) {
    return this.apollo.mutate<any>({
      variables: {
        id,
      },
      mutation: gql`
        query ($id: String!) {
          subscription(where: { id: $id }) {
            id
            tags
            feed {
              title
              feed_url
              home_page_url
              description
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
