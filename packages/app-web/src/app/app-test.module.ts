import { NgModule } from '@angular/core';

import { IonicModule } from '@ionic/angular';
import { ApolloClient, DocumentNode } from '@apollo/client/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { SwUpdateMock } from '../test/sw-update.mock';
import { SwUpdate } from '@angular/service-worker';
import {
  ApolloQueryResult,
  OperationVariables,
} from '@apollo/client/core/types';
import {
  AuthAnonymous,
  GqlAuthAnonymousMutation,
  GqlAuthAnonymousMutationVariables,
  GqlNativeFeedByIdQuery,
  GqlNativeFeedByIdQueryVariables,
  GqlPlansQuery,
  GqlPlansQueryVariables,
  GqlScrapeEmitType,
  GqlScrapeQuery,
  GqlScrapeQueryVariables,
  GqlSearchArticlesQuery,
  GqlSearchArticlesQueryVariables,
  GqlSearchNativeFeedsQuery,
  GqlSearchNativeFeedsQueryVariables,
  GqlServerSettingsQuery,
  GqlServerSettingsQueryVariables,
  NativeFeedById,
  Plans,
  Scrape,
  SearchArticles,
  SearchNativeFeeds,
  ServerSettings,
} from '../generated/graphql';
import { isUndefined } from 'lodash-es';
import { NativeFeed, ScrapeResponse } from './graphql/types';
import { TestBed } from '@angular/core/testing';
import {
  Config,
  ServerSettingsService,
} from './services/server-settings.service';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';

export type MockedRequestResolver<R, V> = (
  args: V,
) => Promise<Partial<ApolloQueryResult<R>>>;
export type MockCondition<V> = (args: V) => boolean;

interface MockedRequest {
  query?: DocumentNode;
  mutate?: DocumentNode;
  condition?: MockCondition<unknown>;
  resolver: MockedRequestResolver<unknown, unknown>;
}

export class ApolloMockController {
  private mockedRequests: MockedRequest[] = [];
  client() {
    return {
      query: jasmine.createSpy('query').and.callFake((args) => {
        const mock = this.mockedRequests
          .filter((it) => it.query === args.query)
          .find((it) => isUndefined(it.condition) || it.condition(args));

        if (mock) {
          return mock.resolver(args);
        }
      }),
      mutate: jasmine.createSpy('mutate').and.callFake((args) => {
        const mock = this.mockedRequests
          .filter((it) => it.mutate === args.mutate)
          .find((it) => isUndefined(it.condition) || it.condition(args));

        if (mock) {
          return mock.resolver(args);
        }
      }),
      // subscribe: jasmine.createSpy('subscribe').and.callFake((args) => {
      //   if (args.mutate === AuthAnonymous) {
      //     return Promise.resolve({
      //       data: {
      //         authAnonymous: {
      //           token: ''
      //         }
      //       }
      //     } as ApolloQueryResult<GqlAuthAnonymousMutation>)
      //   }
      // }),
    };
  }

  mockQuery<
    T = any,
    TVariables extends OperationVariables = OperationVariables,
  >(query: DocumentNode, condition?: MockCondition<TVariables>) {
    return {
      and: {
        resolveOnce: (
          resolver: (
            args: TVariables,
          ) => Promise<Partial<ApolloQueryResult<Partial<T>>>>,
        ) => {
          this.mockedRequests.push({
            query,
            condition,
            resolver,
          });
          return this;
        },
      },
    };
  }

  mockMutate<
    T = any,
    TVariables extends OperationVariables = OperationVariables,
  >(query: DocumentNode, condition?: MockCondition<TVariables>) {
    return {
      and: {
        resolveOnce: (
          resolver: (
            args: TVariables,
          ) => Promise<Partial<ApolloQueryResult<Partial<T>>>>,
        ) => {
          this.mockedRequests.push({
            query,
            condition,
            resolver,
          });
          return this;
        },
      },
    };
  }

  // mockSubscribe<T = any, TVariables extends OperationVariables = OperationVariables>(options: SubscriptionOptions<TVariables, T>): Observable<FetchResult<T>> {
  //   return null
  // }
  reset() {
    this.mockedRequests = [];
    return this;
  }
}

@NgModule({
  imports: [HttpClientTestingModule, IonicModule.forRoot()],
  providers: [{ provide: SwUpdate, useClass: SwUpdateMock }],
})
export class AppTestModule {
  static withDefaults(
    configurer: (apolloMockController: ApolloMockController) => void = null,
  ) {
    const apolloMockController = new ApolloMockController();
    apolloMockController
      .mockMutate<GqlAuthAnonymousMutation, GqlAuthAnonymousMutationVariables>(
        AuthAnonymous,
      )
      .and.resolveOnce(async () => {
        return {
          data: {
            authAnonymous: {
              token:
                'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c',
              corrId: '',
            },
          },
        };
      });

    if (configurer) {
      configurer(apolloMockController);
    }

    return {
      ngModule: AppTestModule,
      providers: [
        { provide: ApolloMockController, useValue: apolloMockController },
        { provide: ApolloClient, useValue: apolloMockController.client() },
      ],
    };
  }
}

export function mockSearchArticles(apolloMockController: ApolloMockController) {
  apolloMockController
    .mockQuery<GqlSearchArticlesQuery, GqlSearchArticlesQueryVariables>(
      SearchArticles,
    )
    .and.resolveOnce(async () => {
      return {
        data: {
          articles: {
            articles: [],
            pagination: {} as any,
          },
        },
      };
    });
}

export function mockScrape(apolloMockController: ApolloMockController) {
  apolloMockController
    .mockQuery<GqlScrapeQuery, GqlScrapeQueryVariables>(Scrape)
    .and.resolveOnce(async () => {
      return {
        data: {
          scrape: {
            url: '',
            failed: false,
            debug: {
              contentType: 'text/html',
            },
            elements: [

            ]
          } as ScrapeResponse,
        },
      };
    });
}
export function mockSearchNativeFeeds(
  apolloMockController: ApolloMockController,
) {
  apolloMockController
    .mockQuery<GqlSearchNativeFeedsQuery, GqlSearchNativeFeedsQueryVariables>(
      SearchNativeFeeds,
    )
    .and.resolveOnce(async () => {
      return {
        data: {
          nativeFeeds: {
            nativeFeeds: [],
            pagination: {} as any,
          },
        },
      };
    });
}

export function mockNativeFeedById(apolloMockController: ApolloMockController) {
  apolloMockController
    .mockQuery<GqlNativeFeedByIdQuery, GqlNativeFeedByIdQueryVariables>(
      NativeFeedById,
    )
    .and.resolveOnce(async () => {
      return {
        data: {
          nativeFeed: {
            importers: [],
          } as NativeFeed,
        },
      };
    });
}

export function mockPlans(apolloMockController: ApolloMockController) {
  apolloMockController
    .mockQuery<GqlPlansQuery, GqlPlansQueryVariables>(Plans)
    .and.resolveOnce(async () => {
      return {
        data: {
          plans: [],
        },
      };
    });
}

export async function mockServerSettings(
  apolloMockController: ApolloMockController,
  serverSettingsService: ServerSettingsService,
  apolloClient: ApolloClient<any>,
) {
  apolloMockController
    .mockQuery<GqlServerSettingsQuery, GqlServerSettingsQueryVariables>(
      ServerSettings,
    )
    .and.resolveOnce(async () => {
      return {
        data: {
          serverSettings: {
            features: [],
            apiUrls: {
              webToPageChange: '',
              webToFeed: '',
            },
          },
        },
      };
    });

  serverSettingsService.createApolloClient = jasmine
    .createSpy()
    .and.returnValue(apolloClient);
  const httpClient = TestBed.inject(HttpClient);
  const mockConfig: Config = {
    apiUrl: '',
  };
  httpClient.get = jasmine
    .createSpy('mockHttpGet')
    .and.returnValue(of(mockConfig));
  await serverSettingsService.fetchServerSettings();
}
