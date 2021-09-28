import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import {
  FieldWrapper,
  GqlFeed,
  GqlNativeFeedRef,
  GqlProxyFeed,
  GqlSubscription,
  Scalars,
} from '../../generated/graphql';
import { ProfileService } from './profile.service';

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  constructor(
    private readonly apollo: Apollo,
    private readonly profileService: ProfileService
  ) {}

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

  createSubscription(feed: GqlNativeFeedRef | GqlProxyFeed, bucketId: string) {
    return this.apollo.mutate<any>({
      variables: {
        feedUrl: feed.feed_url,
        email: this.profileService.getEmail(),
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
    subscription: GqlSubscription,
    feed: GqlFeed,
    tags: string[] = []
  ) {
    return this.apollo.mutate<any>({
      variables: {
        tags,
        title: subscription.title,
        feedUrl: feed.feed_url,
        homepageUrl: feed.home_page_url,
        subscriptionId: subscription.id,
      },
      mutation: gql`
        mutation updateSubscription(
          $feedUrl: String!
          $homepageUrl: String
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
                  create: {
                    feed_url: $feedUrl
                    home_page_url: $homepageUrl
                    title: $title
                    stream: { create: {} }
                  }
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
      variables: {
        id,
      },
      mutation: gql`
        mutation ($id: String!) {
          deleteSubscription(where: { id: $id }) {
            id
          }
        }
      `,
    });
  }

  findById(id: string) {
    return this.apollo.query<any>({
      variables: {
        id,
      },
      query: gql`
        query ($id: String!) {
          subscription(where: { id: $id }) {
            id
            title
            tags
            lastUpdatedAt
            feed {
              id
              title
              feed_url
              broken
              home_page_url
              description
              status
              streamId
              ownerId
            }
          }
        }
      `,
    });
  }

  async subscribeToNativeFeed(feed: GqlNativeFeedRef) {}

  async subscribeToGeneratedFeed(feed: GqlProxyFeed) {}

  disableById(id: string, disabled: boolean) {
    return this.apollo.mutate<any>({
      variables: {
        id,
        disabled,
      },
      mutation: gql`
        mutation ($id: String!, $disabled: Boolean!) {
          updateSubscription(
            data: { inactive: { set: $disabled } }
            where: { id: $id }
          ) {
            id
          }
        }
      `,
    });
  }

  findAllByBucket(id: FieldWrapper<Scalars['String']>) {
    return this.apollo.query<any>({
      variables: {
        bucketId: id,
      },
      query: gql`
        query ($bucketId: String!) {
          subscriptions(where: { bucketId: { equals: $bucketId } }) {
            id
            title
            tags
            lastUpdatedAt
            feed {
              id
              title
              feed_url
              broken
              home_page_url
              description
              status
              streamId
              ownerId
            }
          }
        }
      `,
    });
  }
}
