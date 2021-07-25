import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';

@Injectable({
  providedIn: 'root',
})
export class StreamService {
  constructor(private readonly apollo: Apollo) {}

  getArticles(streamId: string) {
    console.log('streamId', streamId);
    return this.apollo.query<any>({
      query: gql`
        query {
          articleRefs(
            take: 10
            orderBy: { createdAt: desc }
            where: {
              stream: {
                every: { id: { equals: "${streamId}" } }
              }
            }
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
              content_text
              tags
            }
          }
        }
      `,
    });
  }
}
