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
  GqlLicenseQuery,
  GqlLicenseQueryVariables,
  GqlListPluginsQuery,
  GqlListPluginsQueryVariables,
  GqlListRepositoriesQuery,
  GqlListRepositoriesQueryVariables,
  GqlPlansQuery,
  GqlPlansQueryVariables,
  GqlProductName,
  GqlScrapeQuery,
  GqlScrapeQueryVariables,
  GqlScrapeResponse,
  GqlServerSettings,
  GqlServerSettingsQuery,
  GqlServerSettingsQueryVariables,
  GqlRepository,
  GqlRepositoryByIdQuery,
  GqlRepositoryByIdQueryVariables,
  GqlVisibility,
  License,
  ListPlugins,
  ListRepositories,
  Plans,
  Scrape,
  ServerSettings,
  RepositoryById,
} from '../generated/graphql';
import { isUndefined } from 'lodash-es';
import { TestBed } from '@angular/core/testing';
import {
  FeedlessAppConfig,
  ServerSettingsService,
} from './services/server-settings.service';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, of } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { ProductConfig, ProductService } from './services/product.service';

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
  imports: [
    HttpClientTestingModule,
    RouterTestingModule.withRoutes([]),
    IonicModule.forRoot(),
  ],
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

    const productConfig: ProductConfig = {
      id: 'feedless',
      product: GqlProductName.Feedless,
      localSetup: '',
      title: '',
      titleHtml: '',
      imageUrl: '',
      pageTitle: '',
      routes: [],
      subtitle: '',
      descriptionMarkdown: '',
      descriptionHtml: '',
      videoUrl: '',
      costs: 0,
      features: [],
      summary: '',
      isUnstable: false,
      offlineSupport: false,
      sideMenu: null,
    };
    const productServiceMock = {
      getActiveProductConfigChange: () => new BehaviorSubject(productConfig),
    } as any;

    return {
      ngModule: AppTestModule,
      providers: [
        { provide: ProductService, useValue: productServiceMock },
        { provide: ApolloMockController, useValue: apolloMockController },
        { provide: ApolloClient, useValue: apolloMockController.client() },
      ],
    };
  }
}

export function mockPlans(apolloMockController: ApolloMockController) {
  return apolloMockController
    .mockQuery<GqlPlansQuery, GqlPlansQueryVariables>(Plans)
    .and.resolveOnce(async () => {
      return {
        data: {
          plans: [],
        },
      };
    });
}

export function mockPlugins(apolloMockController: ApolloMockController) {
  return apolloMockController
    .mockQuery<GqlListPluginsQuery, GqlListPluginsQueryVariables>(ListPlugins)
    .and.resolveOnce(async () => {
      return {
        data: {
          plugins: [],
        },
      };
    });
}

export type Mocks = {
  repository: GqlRepository;
  scrapeResponse: GqlScrapeResponse;
  license: GqlLicenseQuery['license'];
};
export const mocks: Mocks = {
  repository: {
    id: '',
    description: '',
    title: '',
    ownerId: '',
    sources: [],
    archived: false,
    documentCount: 0,
    activity: {
      items: [],
    },
    refreshCron: '',
    plugins: [],
    visibility: GqlVisibility.IsPrivate,
    createdAt: 0,
    lastUpdatedAt: new Date(),
    retention: {},
    documentCountSinceCreation: 0,
  },
  scrapeResponse: {
    url: '',
    failed: false,
    debug: {
      corrId: '',
      prerendered: false,
      network: [],
      contentType: 'text/html',
      console: [],
      cookies: [],
      statusCode: 200,
      html: '',
      screenshot: '',
      metrics: {
        render: 1,
        queue: 1,
      },
    },
    elements: [],
  },
  license: {
    isLocated: false,
    isTrial: true,
    isValid: false,
  },
};

export function mockRepository(apolloMockController: ApolloMockController) {
  return apolloMockController
    .mockQuery<GqlRepositoryByIdQuery, GqlRepositoryByIdQueryVariables>(
      RepositoryById,
    )
    .and.resolveOnce(async () => {
      return {
        data: {
          repository: mocks.repository,
        },
      };
    });
}

export function mockRepositories(apolloMockController: ApolloMockController) {
  return apolloMockController
    .mockQuery<GqlListRepositoriesQuery, GqlListRepositoriesQueryVariables>(
      ListRepositories,
    )
    .and.resolveOnce(async () => {
      return {
        data: {
          repositories: [mocks.repository],
        },
      };
    });
}

export function mockScrape(apolloMockController: ApolloMockController) {
  return apolloMockController
    .mockQuery<GqlScrapeQuery, GqlScrapeQueryVariables>(Scrape)
    .and.resolveOnce(async () => {
      return {
        data: {
          scrape: mocks.scrapeResponse,
        },
      };
    });
}

export function mockLicense(
  apolloMockController: ApolloMockController,
): ApolloMockController {
  return apolloMockController
    .mockQuery<GqlLicenseQuery, GqlLicenseQueryVariables>(License)
    .and.resolveOnce(async () => {
      return {
        data: {
          license: mocks.license,
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
      const serverSettings: GqlServerSettings = {
        appUrl: '',
        gatewayUrl: '',
        // license: {},
        buildFrom: 0,
        profiles: [],
        version: '',
        features: [],
      };
      return {
        data: {
          serverSettings: serverSettings,
        },
      };
    });

  serverSettingsService.createApolloClient = jasmine
    .createSpy()
    .and.returnValue(apolloClient);
  const httpClient = TestBed.inject(HttpClient);
  const mockConfig: FeedlessAppConfig = {
    apiUrl: '',
    products: [],
  };
  httpClient.get = jasmine
    .createSpy('mockHttpGet')
    .and.returnValue(of(mockConfig));
  await serverSettingsService.fetchServerSettings();
  return apolloMockController;
}
