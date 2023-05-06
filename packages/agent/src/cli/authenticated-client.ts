import { ApolloClient, ApolloError } from '@apollo/client/core';
import {
  Articles,
  GqlArticlesQuery,
  GqlArticlesQueryVariables,
  GqlArticlesWhereInput,
  GqlSortOrder,
} from '../generated/graphql';
import { Observable, Subject } from 'rxjs';

const waitSec = (sec: number) =>
  new Promise((resolve) => setTimeout(resolve, sec * 1000));

export class AuthenticatedClient {
  constructor(private readonly httpClient: ApolloClient<any>) {}

  articles<T>(where: GqlArticlesWhereInput): Observable<T> {
    const s = new Subject<T>();

    this.fetchArticles(where, s);

    return s.asObservable();
  }

  private fetchArticles<T>(
    where: GqlArticlesWhereInput,
    s: Subject<T>,
    page: number = 0,
    pageSize: number = 1,
  ) {
    this.httpClient
      .query<GqlArticlesQuery, GqlArticlesQueryVariables>({
        query: Articles,
        variables: {
          data: {
            where,
            orderBy: {
              createdAt: GqlSortOrder.Desc,
            },
            cursor: {
              page,
              pageSize,
            },
          },
        },
      })
      .then((response) => response.data.articles)
      .then((response) => {
        response.articles.forEach((article) => s.next(article as T));

        const pagination = response.pagination;
        if (pagination.isLast) {
          // console.error('last reached');
          return waitSec(60).then(() =>
            this.fetchArticles(where, s, page, pageSize),
          );
        } else {
          return this.fetchArticles(where, s, page + 1, pageSize);
        }
      })
      .catch((err: ApolloError) => {
        if (
          err.graphQLErrors.some(
            (e) => e.message === 'HostOverloadingException',
          )
        ) {
          // console.error('host overloaded');
          return waitSec(20).then(() =>
            this.fetchArticles(where, s, page, pageSize),
          );
        }
        throw err;
      });
  }
}
