import { Injectable } from '@angular/core';
import {
  ArticleById,
  DeleteArticles,
  GqlArticle,
  GqlArticleByIdQuery,
  GqlArticleByIdQueryVariables,
  GqlArticlesDeleteWhereInput,
  GqlArticlesPagedInput,
  GqlArticlesUpdateWhereInput,
  GqlContent,
  GqlDeleteArticlesMutation,
  GqlDeleteArticlesMutationVariables,
  GqlEnclosure,
  GqlSearchArticlesQuery,
  GqlSearchArticlesQueryVariables,
  GqlUpdateArticlesMutation,
  GqlUpdateArticlesMutationVariables,
  GqlWebDocument,
  Maybe,
  SearchArticles,
  UpdateArticles,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { Pagination } from './pagination.service';
import { BasicBucket } from './bucket.service';
import { BasicNativeFeed } from './feed.service';

export type Enclosure = Pick<GqlEnclosure, 'length' | 'type' | 'url'>;
export type Content = Pick<
  GqlContent,
  | 'title'
  | 'description'
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

export type BasicArticle = Pick<
  GqlArticle,
  'id' | 'status' | 'type' | 'nativeFeedId' | 'streamId' | 'createdAt'
>;
export type BasicContent = Pick<
  GqlContent,
  | 'title'
  | 'description'
  | 'contentTitle'
  | 'contentText'
  | 'contentRaw'
  | 'contentRawMime'
  | 'url'
  | 'imageUrl'
  | 'publishedAt'
  | 'startingAt'
  | 'updatedAt'
  | 'tags'
  | 'createdAt'
> & {
  enclosures?: Maybe<Array<Pick<GqlEnclosure, 'length' | 'type' | 'url'>>>;
};
export type Article = BasicArticle & { content: BasicContent };

export type BasicWebDocument = Pick<
  GqlWebDocument,
  'id' | 'title' | 'description' | 'type' | 'url' | 'imageUrl' | 'createdAt'
>;
export type BasicContext = {
  // articles: Array<
  //   BasicArticle & {
  //     content: BasicContent;
  //   }
  // >;
  links: Array<BasicWebDocument>;
};

export type ArticleWithContext = BasicArticle & {
  content: BasicContent;
  bucket?: BasicBucket;
  nativeFeed: BasicNativeFeed;
  context: BasicContext;
};

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  findAllByStreamId(
    data: GqlArticlesPagedInput
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
