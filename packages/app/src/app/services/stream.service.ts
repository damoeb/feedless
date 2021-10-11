import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';

@Injectable({
  providedIn: 'root',
})
export class StreamService {
  constructor(private readonly apollo: Apollo) {}

  getArticles(streamId: string, skip: number = 0, take: number = 10) {
    console.log('getArticles', streamId, skip, take);
    return this.apollo.query<any>({
      variables: {
        streamId,
        take,
        skip,
      },
      query: gql`
        query ($streamId: String!, $take: Int!, $skip: Int!) {
          articleRefs(
            take: $take
            skip: $skip
            orderBy: { date_released: desc }
            where: { stream: { every: { id: { equals: $streamId } } } }
          ) {
            createdAt
            favored
            tags
            article {
              id
              date_published
              url
              author
              title
              content_raw
              content_raw_mime
              tags
            }
          }
        }
      `,
    });
  }
}
