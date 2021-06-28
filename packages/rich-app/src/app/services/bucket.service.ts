import { Injectable } from '@angular/core';
import {Apollo, gql} from 'apollo-angular';
import {Query} from '../../generated/graphql';

@Injectable({
  providedIn: 'root'
})
export class BucketService {

  constructor(private readonly apollo: Apollo) { }

  getBucketsForUser() {
    return this.apollo.watchQuery<any>({
      query: gql`query {
        findFirstUser(where: { email: {equals: "karl@may.ch"} }) {
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
            subscriptions {
              ownerId
              feed {
                url
                title
              }
            }
          }
        }
      }
      `
    });
  }
  getBucketsById(bucketId: string) {
    return this.apollo.watchQuery<any>({
      query: gql`query {
        bucket(where: { id: "${bucketId}" }) {
          id
          title
          description
          listed
          subscriptions {
            id
            feed {
              title
              url
            }
            updatedAt
          }
        }
      }

      `
    });
  }
}
