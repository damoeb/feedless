import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  constructor(private readonly apollo: Apollo) {}

  findById(articleId: string): any {
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
              content_html
              tags
            }
          }
        }
      `,
    });
  }

  findArticleRef(articleId: string): any {
    return this.apollo.query<any>({
      variables: {
        articleId,
      },
      query: gql`
        query ($articleId: String!) {
          findFirstArticleRef(where: { articleId: { equals: $articleId } }) {
            id
            ownerId
            createdAt
            favored
            has_seen
            tags
            stream {
              feeds {
                id
              }
            }
          }
        }
      `,
    });
  }

  removeXmlMetatags(value: string) {
    function removePrefix(otherValue: string, prefix: string) {
      if (otherValue && otherValue.startsWith(prefix)) {
        return otherValue.substring(prefix.length);
      }
      return otherValue;
    }
    function removeSuffix(otherValue: string, suffix: string) {
      if (otherValue && otherValue.endsWith(suffix)) {
        return otherValue.substring(0, otherValue.length - suffix.length);
      }
      return otherValue;
    }

    return removeSuffix(removePrefix(value, '<![CDATA['), ']]');
  }

  getArticlesForNativeFeed(feedUrl: string) {
    return this.apollo.query<any>({
      variables: {
        url: feedUrl,
      },
      query: gql`
        query ($url: String!) {
          articlesForFeedUrl(feedUrl: $url) {
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
