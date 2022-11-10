import { Injectable } from '@angular/core';
import {
  ArticleById,
  GqlArticle,
  GqlArticleByIdQuery,
  GqlArticleByIdQueryVariables,
  GqlArticleType,
  GqlContent,
  GqlEnclosure,
  GqlReleaseStatus, GqlSearchArticlesQuery, GqlSearchArticlesQueryVariables,
  Maybe, SearchArticles
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { Pagination } from './pagination.service';

export type Enclosure = Pick<GqlEnclosure, 'length' | 'type' | 'url'>;
export type Content = Pick<
  GqlContent,
  | 'title'
  | 'description'
  | 'hasFulltext'
  | 'contentTitle'
  | 'contentText'
  | 'contentRaw'
  | 'contentRawMime'
  | 'url'
  | 'imageUrl'
  | 'publishedAt'
  | 'updatedAt'
  | 'tags'
>;
export type Article = (
  Pick<GqlArticle, 'id' | 'status' | 'type' | 'nativeFeedId' | 'streamId' | 'createdAt'>
  & { content: (
    Pick<GqlContent, 'title' | 'description' | 'hasFulltext' | 'contentTitle' | 'contentText' | 'contentRaw' | 'contentRawMime' | 'url' | 'imageUrl' | 'publishedAt' | 'updatedAt' | 'tags' | 'createdAt'>
    & { enclosures?: Maybe<Array<Pick<GqlEnclosure, 'length' | 'type' | 'url'>>> }
    ) }
  );

export type ArticleMatch = (
  Pick<GqlArticle, 'id' | 'status' | 'type' | 'nativeFeedId' | 'streamId' | 'createdAt'>
  & { content: (
    Pick<GqlContent, 'title' | 'description' | 'hasFulltext' | 'contentTitle' | 'contentText' | 'contentRaw' | 'contentRawMime' | 'url' | 'imageUrl' | 'publishedAt' | 'updatedAt' | 'tags' | 'createdAt'>
    & { enclosures?: Maybe<Array<Pick<GqlEnclosure, 'length' | 'type' | 'url'>>> }
    ) }
  );

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  constructor(
    private readonly apollo: ApolloClient<any>
  ) {}

  findAllByStreamId(
    streamId: string,
    page: number,
    types = [GqlArticleType.Feed],
    status = [GqlReleaseStatus.NeedsApproval]
  ): Promise<{ articles?: Array<ArticleMatch>; pagination: Pagination }> {
    return this.apollo
      .query<GqlSearchArticlesQuery, GqlSearchArticlesQueryVariables>({
        query: SearchArticles,
        variables: {
          data: {
            page,
            where: {
              streamId,
              status: status ? {
                oneOf: status
              } : null,
              type: types ? {
                oneOf: types
              } : null
            }
          }
        },
      })
      .then((response) => {
        const data = response.data.articles;
        return {
          articles: data.articles,
          pagination: data.pagination,
        };
      });
  }

  findById(id: string): Promise<Article> {
    return this.apollo
      .query<GqlArticleByIdQuery, GqlArticleByIdQueryVariables>({
        query: ArticleById,
        variables: {
          id,
        },
      })
      .then((response) => response.data.article);
  }
}
