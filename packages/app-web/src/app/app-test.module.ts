import { NgModule } from '@angular/core';
import { ApolloClient, DocumentNode } from '@apollo/client/core';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { SwUpdateMock } from '../test/sw-update.mock';
import { SwUpdate } from '@angular/service-worker';
import {
  ApolloQueryResult,
  OperationVariables,
} from '@apollo/client/core/types';
import {
  AuthAnonymous,
  FindEvents,
  FullRecordByIds,
  GqlAuthAnonymousMutation,
  GqlAuthAnonymousMutationVariables,
  GqlFindEventsQuery,
  GqlFindEventsQueryVariables,
  GqlFullRecordByIdsQuery,
  GqlFullRecordByIdsQueryVariables,
  GqlListPluginsQuery,
  GqlListPluginsQueryVariables,
  GqlListProductsQuery,
  GqlListProductsQueryVariables,
  GqlListRepositoriesQuery,
  GqlListRepositoriesQueryVariables,
  GqlOrdersQuery,
  GqlOrdersQueryVariables,
  GqlVertical,
  GqlRecordByIdsQuery,
  GqlRecordByIdsQueryVariables,
  GqlRepository,
  GqlRepositoryByIdQuery,
  GqlRepositoryByIdQueryVariables,
  GqlScrapeQuery,
  GqlScrapeQueryVariables,
  GqlScrapeResponse,
  GqlServerSettings,
  GqlServerSettingsQuery,
  GqlServerSettingsQueryVariables,
  GqlVisibility,
  ListPlugins,
  ListProducts,
  ListRepositories,
  Orders,
  RecordByIds,
  RepositoryById,
  Scrape,
  ServerSettings,
} from '../generated/graphql';
import { assignIn, isUndefined } from 'lodash-es';
import { TestBed } from '@angular/core/testing';
import { ServerConfigService } from './services/server-config.service';
import {
  HttpClient,
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { BehaviorSubject, of } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import {
  VerticalSpecWithRoutes,
  AppConfigService,
} from './services/app-config.service';
import {
  ModalController,
  PopoverController,
  ToastController,
} from '@ionic/angular/standalone';
import { VerticalAppConfig } from './types';

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
            condition: condition as any,
            resolver: resolver as any,
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
            condition: condition as any,
            resolver: resolver as any,
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

export type AppTestOptions = {
  configurer?: (apolloMockController: ApolloMockController) => void;
  mockAppConfig?: boolean;
};

const defaultAppTestModuleConfig: AppTestOptions = {
  mockAppConfig: true,
};

@NgModule({
  imports: [RouterTestingModule.withRoutes([])],
  providers: [
    { provide: SwUpdate, useClass: SwUpdateMock },
    provideHttpClient(withInterceptorsFromDi()),
    provideHttpClientTesting(),
  ],
})
export class AppTestModule {
  static withDefaults(options: AppTestOptions = null) {
    const config = assignIn({}, defaultAppTestModuleConfig, options);
    const apolloMockController = new ApolloMockController();
    apolloMockController
      .mockMutate<
        GqlAuthAnonymousMutation,
        GqlAuthAnonymousMutationVariables
      >(AuthAnonymous)
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

    if (config.configurer) {
      config.configurer(apolloMockController);
    }

    const productConfig: VerticalSpecWithRoutes = {
      id: 'feedless',
      product: GqlVertical.Feedless,
      localSetupBash: '',
      title: '',
      titleHtml: '',
      imageUrl: '',
      pageTitle: '',
      routes: [],
      links: [],
      subtitle: '',
      descriptionMarkdown: '',
      descriptionHtml: '',
      videoUrl: '',
      costs: [],
      version: [],
      listed: true,
      features: [],
      summary: '',
      phase: 'planning',
      offlineSupport: false,
      sideMenu: null,
    };
    const productServiceMock = {
      getActiveProductConfigChange: () => new BehaviorSubject(productConfig),
    } as any;

    const providers = [
      { provide: AppConfigService, useValue: productServiceMock },
      { provide: ModalController, useValue: {} },
      { provide: PopoverController, useValue: {} },
      { provide: ToastController, useValue: {} },
      { provide: ApolloMockController, useValue: apolloMockController },
      { provide: ApolloClient, useValue: apolloMockController.client() },
    ];

    if (config.mockAppConfig) {
      providers.push({
        provide: AppConfigService,
        useValue: {
          setPageTitle: () => {},
          getActiveProductConfigChange: () => of(),
        },
      });
    }

    return {
      ngModule: AppTestModule,
      providers,
    };
  }
}

export function mockProducts(apolloMockController: ApolloMockController) {
  return apolloMockController
    .mockQuery<
      GqlListProductsQuery,
      GqlListProductsQueryVariables
    >(ListProducts)
    .and.resolveOnce(async () => {
      return {
        data: {
          products: [],
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

export function mockRecords(apolloMockController: ApolloMockController) {
  return apolloMockController
    .mockQuery<GqlRecordByIdsQuery, GqlRecordByIdsQueryVariables>(RecordByIds)
    .and.resolveOnce(async () => {
      return {
        data: {
          records: [],
        },
      };
    });
}

export function mockFullRecords(apolloMockController: ApolloMockController) {
  return apolloMockController
    .mockQuery<
      GqlFullRecordByIdsQuery,
      GqlFullRecordByIdsQueryVariables
    >(FullRecordByIds)
    .and.resolveOnce(async () => {
      return {
        data: {
          records: [],
        },
      };
    });
}

export type Mocks = {
  repository: GqlRepository;
  scrapeResponse: GqlScrapeResponse;
};
export const mocks: Mocks = {
  repository: {
    id: '',
    product: GqlVertical.RssProxy,
    description: '',
    shareKey: '',
    title: '',
    tags: [],
    ownerId: '',
    archived: false,
    sourcesCount: 0,
    documentCount: 0,
    frequency: [],
    sourcesCountWithProblems: 0,
    currentUserIsOwner: false,
    pushNotificationsMuted: false,
    pullsPerMonth: 0,
    refreshCron: '',
    plugins: [],
    visibility: GqlVisibility.IsPrivate,
    createdAt: 0,
    lastUpdatedAt: new Date(),
    retention: {},
  },
  scrapeResponse: {
    outputs: [],
    ok: true,
    errorMessage: '',
    logs: [],
  },
};

export function mockRepository(apolloMockController: ApolloMockController) {
  return apolloMockController
    .mockQuery<
      GqlRepositoryByIdQuery,
      GqlRepositoryByIdQueryVariables
    >(RepositoryById)
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
    .mockQuery<
      GqlListRepositoriesQuery,
      GqlListRepositoriesQueryVariables
    >(ListRepositories)
    .and.resolveOnce(async () => {
      return {
        data: {
          repositories: [mocks.repository],
        },
      };
    });
}

export function mockBillings(apolloMockController: ApolloMockController) {
  return apolloMockController
    .mockQuery<GqlOrdersQuery, GqlOrdersQueryVariables>(Orders)
    .and.resolveOnce(async () => {
      return {
        data: {
          orders: [],
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

export function mockEvents(apolloMockController: ApolloMockController) {
  return apolloMockController
    .mockQuery<GqlFindEventsQuery, GqlFindEventsQueryVariables>(FindEvents)
    .and.resolveOnce(async () => {
      return {
        data: {
          recordsFrequency: [],
        },
      };
    });
}

export async function mockServerSettings(
  apolloMockController: ApolloMockController,
  serverSettingsService: ServerConfigService,
  apolloClient: ApolloClient<any>,
) {
  apolloMockController
    .mockQuery<
      GqlServerSettingsQuery,
      GqlServerSettingsQueryVariables
    >(ServerSettings)
    .and.resolveOnce(async () => {
      const serverSettings: GqlServerSettings = {
        // license: {},
        build: {
          date: 0,
          commit: '1234',
        },
        profiles: [],
        version: '',
        features: [],
        license: {
          isLocated: false,
          isTrial: true,
          isValid: true,
        },
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
  const mockConfig: VerticalAppConfig = {
    apiUrl: '',
    attributionHtml: '',
    product: GqlVertical.Feedless,
  };
  httpClient.get = jasmine
    .createSpy('mockHttpGet')
    .and.returnValue(of(mockConfig));
  await serverSettingsService.fetchServerSettings();
  return apolloMockController;
}
