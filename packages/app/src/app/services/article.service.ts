import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import {
  GqlArticleByIdGQL,
  GqlArticleByIdQuery,
} from '../../generated/graphql';
import { Observable } from 'rxjs';
import { ApolloQueryResult } from '@apollo/client/core';

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  constructor(
    private readonly apollo: Apollo,
    private readonly articleByIdGQL: GqlArticleByIdGQL
  ) {}

  findById(
    articleId: string
  ): Observable<ApolloQueryResult<GqlArticleByIdQuery>> {
    return this.articleByIdGQL.fetch({
      id: articleId,
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
    if (!value) {
      return '';
    }
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
            content_raw
            tags
          }
        }
      `,
    });
  }
}
