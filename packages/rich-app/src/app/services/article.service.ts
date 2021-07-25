import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  constructor(private readonly apollo: Apollo) {}

  findById(articleId: string) {
    return this.apollo.query<any>({
      variables: {
        id: articleId,
      },
      query: gql`
        query ($id: String) {
          findFirstArticleRef(
            where: { article: { is: { id: { equals: $id } } } }
          ) {
            favored
            tags
            ownerId
            related {
              articleId
            }
            stream {
              id
            }
            createdAt
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
