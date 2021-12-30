import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { Observable } from 'rxjs';
import { GqlFeedByIdGQL, GqlFeedByIdQuery } from '../../generated/graphql';
import { ApolloQueryResult } from '@apollo/client/core';

@Injectable({
  providedIn: 'root',
})
export class FeedService {
  constructor(private readonly apollo: Apollo,
              private readonly feedByIdGQL: GqlFeedByIdGQL) {}

  findById(feedId: string): Observable<ApolloQueryResult<GqlFeedByIdQuery>> {
    return this.feedByIdGQL.fetch({
      feedId,
    });
  }

  // todo mag use
  // getEventsById(feedId: string, take: number = 10, skip: number = 0) {
  //   return this.apollo.query<any>({
  //     variables: {
  //       feedId,
  //       take,
  //       skip,
  //     },
  //     query: gql`
  //       query ($id: String!, $take: Int!, $skip: Int!) {
  //         feedEvents(
  //           where: { feedId: { equals: $id } }
  //           orderBy: { createdAt: desc }
  //           take: $take
  //           skip: $skip
  //         ) {
  //           id
  //           message
  //           createdAt
  //           is_error
  //         }
  //       }
  //     `,
  //   });
  // }

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
