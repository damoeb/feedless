import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import {
  FieldWrapper,
  GqlDiscoverFeedsInSiteGQL,
  GqlDiscoverFeedsInSiteQuery,
  GqlFeed,
  GqlGenericFeedRule,
  GqlNativeFeedRef,
  GqlSubscription,
  GqlSubscriptionByIdGQL,
  GqlSubscriptionByIdQuery,
  GqlSubscriptionsByBucketIdGQL,
  GqlSubscriptionsByBucketIdQuery,
  Scalars
} from '../../generated/graphql';
import { ProfileService } from './profile.service';
import { Observable } from 'rxjs';
import { ApolloQueryResult } from '@apollo/client';

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  constructor(
    private readonly apollo: Apollo,
    private readonly profileService: ProfileService,
    private readonly subscriptionByIdGQL: GqlSubscriptionByIdGQL,
    private readonly subscriptionsByBucketIdGQL: GqlSubscriptionsByBucketIdGQL,
    private readonly discoverFeedsInSiteGQL: GqlDiscoverFeedsInSiteGQL
  ) {}

  discoverFeedsByUrl(url: string): Observable<ApolloQueryResult<GqlDiscoverFeedsInSiteQuery>> {
    return this.discoverFeedsInSiteGQL.fetch({
      url,
    });
  }

  createSubscription(
    feed: GqlNativeFeedRef | GqlGenericFeedRule,
    bucketId: string
  ) {
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
        feedId: feed.id,
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
                update: {
                  feed_url: { set: $feedUrl }
                  home_page_url: { set: $homepageUrl }
                  broken: { set: false }
                  status: { set: "ok" }
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

  findById(id: string): Observable<ApolloQueryResult<GqlSubscriptionByIdQuery>> {
    return this.subscriptionByIdGQL.fetch({
      id,
    });
  }

  async subscribeToNativeFeed(feed: GqlNativeFeedRef) {}

  async subscribeToGeneratedFeed(feed: GqlGenericFeedRule) {}

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

  findAllByBucket(id: FieldWrapper<Scalars['String']>): Observable<ApolloQueryResult<GqlSubscriptionsByBucketIdQuery>> {
    return this.subscriptionsByBucketIdGQL.fetch({
      bucketId: id,
    });
  }
}
