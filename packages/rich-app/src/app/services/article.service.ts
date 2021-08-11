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
              content_html
              tags
            }
          }
        }
      `,
    });
  }

  findArticleRef(articleId: string) {
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
    function removePrefix(value: string, prefix: string) {
      if (value.startsWith(prefix)) {
        return value.substring(prefix.length);
      }
      return value;
    }
    function removeSuffix(value: string, suffix: string) {
      if (value.endsWith(suffix)) {
        return value.substring(0, value.length - suffix.length);
      }
      return value;
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
