import { Injectable } from '@angular/core';
import {
  ArticleById,
  DeleteArticles,
  GqlArticleByIdQuery,
  GqlArticleByIdQueryVariables,
  GqlArticlesDeleteWhereInput,
  GqlArticlesInput,
  GqlArticlesUpdateWhereInput,
  GqlDeleteArticlesMutation,
  GqlDeleteArticlesMutationVariables,
  GqlSearchArticlesQuery,
  GqlSearchArticlesQueryVariables,
  GqlUpdateArticlesMutation,
  GqlUpdateArticlesMutationVariables,
  SearchArticles,
  UpdateArticles,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { Article, ArticleWithContext, Pagination } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  findAllByStreamId(
    data: GqlArticlesInput
  ): Promise<{ articles?: Array<Article>; pagination: Pagination }> {
    return this.apollo
      .query<GqlSearchArticlesQuery, GqlSearchArticlesQueryVariables>({
        query: SearchArticles,
        variables: {
          data,
        },
      })
      .then((response) => {
        const rdata = response.data.articles;
        return {
          articles: rdata.articles,
          pagination: rdata.pagination,
        };
      });
  }

  findById(id: string): Promise<ArticleWithContext> {
    return this.apollo
      .query<GqlArticleByIdQuery, GqlArticleByIdQueryVariables>({
        query: ArticleById,
        variables: {
          data: {
            where: { id },
          },
          linksPage: 0,
        },
      })
      .then((response) => response.data.article);
  }

  deleteArticles(data: GqlArticlesDeleteWhereInput): Promise<boolean> {
    return this.apollo
      .mutate<GqlDeleteArticlesMutation, GqlDeleteArticlesMutationVariables>({
        mutation: DeleteArticles,
        variables: {
          data,
        },
      })
      .then((response) => response.data.deleteArticles);
  }

  updateArticles(data: GqlArticlesUpdateWhereInput): Promise<boolean> {
    return this.apollo
      .mutate<GqlUpdateArticlesMutation, GqlUpdateArticlesMutationVariables>({
        mutation: UpdateArticles,
        variables: {
          data,
        },
      })
      .then((response) => response.data.updateArticles);
  }
}
