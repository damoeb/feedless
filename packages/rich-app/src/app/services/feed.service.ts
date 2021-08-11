import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';

@Injectable({
  providedIn: 'root',
})
export class FeedService {
  constructor(private readonly apollo: Apollo) {}

  findById(feedId: string) {
    return this.apollo.query<any>({
      variables: {
        feedId,
      },
      query: gql`
        query ($feedId: String!) {
          feed(where: { id: $feedId }) {
            id
            feed_url
            home_page_url
            tags
            lastUpdatedAt
          }
        }
      `,
    });
  }

  getEventsById(feedId: string, take: number = 10, skip: number = 0) {
    return this.apollo.query<any>({
      variables: {
        feedId,
        take,
        skip,
      },
      query: gql`
        query ($id: String!, $take: Int!, $skip: Int!) {
          feedEvents(
            where: { feedId: { equals: $id } }
            orderBy: { createdAt: desc }
            take: $take
            skip: $skip
          ) {
            id
            message
            createdAt
            is_error
          }
        }
      `,
    });
  }

  metadataForNativeFeed(feedUrl: string) {
    return this.apollo.query<any>({
      variables: {
        feedUrl,
      },
      query: gql`
        query ($feedUrl: String!) {
          metadataForNativeFeedByUrl(feedUrl: $feedUrl) {
            feed_url
            home_page_url
            title
          }
        }
      `,
    });
  }
}
