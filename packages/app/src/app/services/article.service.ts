import { Injectable } from '@angular/core';
import {
  ArticleById,
  ArticlesByStreamId, GqlArticleByIdQuery, GqlArticleByIdQueryVariables,
  GqlArticlesByStreamIdQuery,
  GqlArticlesByStreamIdQueryVariables,
  GqlArticleType,
  GqlReleaseStatus
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { ActualPagination } from './pagination.service';

export type ActualEnclosure = { __typename?: 'Enclosure'; url: string; type: string; length?: number | null };
export type ActualArticle = { __typename?: 'Article', id: string, title: string, description: string, hasFulltext: boolean, contentTitle?: string | null, contentText: string, contentRaw?: string | null, contentRawMime?: string | null, url: string, imageUrl?: string | null, publishedAt: any, updatedAt: any, tags?: Array<string | null> | null, enclosures?: Array<{ __typename?: 'Enclosure', length?: number | null, type: string, url: string }> | null };

export type ArticleFromFeed = { __typename?: 'ArticleInStream', articleId: string, feedId: string, streamId: string, type: GqlArticleType, status: GqlReleaseStatus, releasedAt: any }

@Injectable({
  providedIn: 'root'
})
export class ArticleService {

  constructor(private readonly apollo: ApolloClient<any>) { }

  findAllByStreamId(streamId: string, page: number, type = GqlArticleType.Feed, status = GqlReleaseStatus.NeedsApproval): Promise<{ __typename?: "ArticlesInStreamResponse"; articles?: Array<ArticleFromFeed> | null; pagination: ActualPagination }> {
    return this.apollo
      .query<GqlArticlesByStreamIdQuery, GqlArticlesByStreamIdQueryVariables>({
        query: ArticlesByStreamId,
        variables: {
          data: {
            streamId,
            page,
            type,
            status,
          }
        }
      })
      .then(response => {
        const data = response.data.articlesByStreamId;
        return {
          articles: data.articles,
          pagination: data.pagination
        };
      });

  }

  findById(articleId: string): Promise<ActualArticle> {
    return this.apollo
      .query<GqlArticleByIdQuery, GqlArticleByIdQueryVariables>({
        query: ArticleById,
        variables: {
          id: articleId
        }
      }).then(response => response.data.article)
  }
}
