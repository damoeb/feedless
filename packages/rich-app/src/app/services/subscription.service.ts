import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  constructor(private readonly apollo: Apollo) {}

  discoverFeeds(queryString: string) {
    return this.apollo.watchQuery<any>({
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
}
