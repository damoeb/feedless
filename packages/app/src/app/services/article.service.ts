import { Injectable } from '@angular/core';
import {
  ArticleById,
  GqlArticle,
  GqlArticleByIdQuery,
  GqlArticleByIdQueryVariables,
  GqlArticleType,
  GqlContent,
  GqlEnclosure,
  GqlArticleReleaseStatus,
  GqlSearchArticlesQuery,
  GqlSearchArticlesQueryVariables,
  GqlWebDocument,
  Maybe,
  SearchArticles,
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
// export type ArticleWithContext = BasicArticle & {
//   content: BasicContent;
//   bucket: BasicBucket;
//   nativeFeed: BasicNativeFeed;
//   context: BasicContext;
// };

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  findAllByStreamId(
    streamId: string,
    page: number,
    query: string = '',
    types = [GqlArticleType.Feed],
    status = [GqlArticleReleaseStatus.Released]
  ): Promise<{ articles?: Array<Article>; pagination: Pagination }> {
    return this.apollo
      .query<GqlSearchArticlesQuery, GqlSearchArticlesQueryVariables>({
        query: SearchArticles,
        variables: {
          data: {
            page,
            where: {
              streamId,
              query,
              status: status
                ? {
                    oneOf: status,
                  }
                : null,
              type: types
                ? {
                    oneOf: types,
                  }
                : null,
            },
          },
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
}
