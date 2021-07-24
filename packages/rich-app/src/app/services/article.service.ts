import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  constructor(private readonly apollo: Apollo) {}

  findById(articleId: string) {
    return this.apollo.query<any>({
      query: gql`
        query {
          article(where: { id: "${articleId}" }) {
            id
            date_published
            url
            author
            title
            content_text
            tags
          }
        }
      `,
    });
  }
}
