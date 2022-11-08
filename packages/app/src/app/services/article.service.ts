import { Injectable } from '@angular/core';
import {
  ArticleById,
  ArticlesByStreamId,
  GqlArticle,
  GqlArticleByIdQuery,
  GqlArticleByIdQueryVariables,
  GqlArticleContent,
  GqlArticleInStream,
  GqlArticlesByStreamIdQuery,
  GqlArticlesByStreamIdQueryVariables,
  GqlArticleType,
  GqlEnclosure,
  GqlReleaseStatus,
  Maybe
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { Pagination } from './pagination.service';

export type Enclosure = Pick<GqlEnclosure, 'length' | 'type' | 'url'>;
export type Content = Pick<GqlArticleContent, 'title' | 'description' | 'hasFulltext' | 'contentTitle' | 'contentText' | 'contentRaw' | 'contentRawMime' | 'url' | 'imageUrl' | 'publishedAt' | 'updatedAt' | 'tags'>;
export type Article = (
  Pick<GqlArticle, 'id' | 'status' | 'type' | 'feedId' | 'streamId'>
  & { content: (
    Pick<GqlArticleContent, 'title' | 'description' | 'hasFulltext' | 'contentTitle' | 'contentText' | 'contentRaw' | 'contentRawMime' | 'url' | 'imageUrl' | 'publishedAt' | 'updatedAt' | 'tags'>
    & { enclosures?: Maybe<Array<Pick<GqlEnclosure, 'length' | 'type' | 'url'>>> }
    ) }
  );

export type ArticleFromFeed = Pick<GqlArticleInStream, 'articleId' | 'feedId' | 'streamId' | 'type' | 'status' | 'releasedAt'>

@Injectable({
  providedIn: 'root'
})
export class ArticleService {

  constructor(private readonly apollo: ApolloClient<any>) { }

  findAllByStreamId(streamId: string, page: number, type = GqlArticleType.Feed, status = GqlReleaseStatus.NeedsApproval): Promise<{articles?: Array<ArticleFromFeed>; pagination: Pagination }> {
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

  findById(id: string): Promise<Article> {
    return this.apollo
      .query<GqlArticleByIdQuery, GqlArticleByIdQueryVariables>({
        query: ArticleById,
        variables: {
          id
        }
      }).then(response => response.data.article)
  }
}
