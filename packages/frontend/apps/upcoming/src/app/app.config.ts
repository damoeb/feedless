import {
  ApplicationConfig,
  importProvidersFrom,
  PLATFORM_ID,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import {
  PreloadAllModules,
  provideRouter,
  RouteReuseStrategy,
  RouterModule,
} from '@angular/router';
import { appRoutes } from './app.routes';
import {
  BrowserModule,
  provideClientHydration,
  withEventReplay,
} from '@angular/platform-browser';
import {
  IonicRouteStrategy,
  provideIonicAngular,
} from '@ionic/angular/standalone';
import { provideAnimations } from '@angular/platform-browser/animations';
import { onError } from '@apollo/client/link/error';
import { removeTypenameFromVariables } from '@apollo/client/link/remove-typename';
import {
  provideHttpClient,
  withFetch,
  withInterceptorsFromDi,
} from '@angular/common/http';
import {
  ApolloAbortControllerService,
  HttpErrorInterceptorService,
  ServerConfigService,
} from '@feedless/components';
import {
  ApolloClient,
  ApolloLink,
  HttpLink,
  InMemoryCache,
} from '@apollo/client/core';
import { environment } from '@feedless/core';
import { AppLoadModule } from './app-load.module';
import { isPlatformBrowser } from '@angular/common';

function resolveCorrId(isBrowser: boolean) {
  const newCorrId = (Math.random() + 1).toString(36).substring(7).toUpperCase();
  if (isBrowser) {
    if (!localStorage.getItem('corrId')) {
      localStorage.setItem('corrId', newCorrId);
    }
    return localStorage.getItem('corrId') || newCorrId;
  } else {
    return newCorrId;
  }
}

export const appConfig: ApplicationConfig = {
  providers: [
    importProvidersFrom(
      BrowserModule,
      RouterModule.forRoot([], {
        preloadingStrategy: PreloadAllModules,
        paramsInheritanceStrategy: 'always',
        enableTracing: false,
      }),
      AppLoadModule,
      // IonApp,
      // IonRouterOutlet,
    ),
    provideClientHydration(withEventReplay()),
    provideBrowserGlobalErrorListeners(),
    provideRouter(appRoutes),
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    {
      provide: ApolloClient,
      deps: [
        HttpErrorInterceptorService,
        ServerConfigService,
        ApolloAbortControllerService,
        PLATFORM_ID,
      ],
      useFactory: (
        httpErrorInterceptorService: HttpErrorInterceptorService,
        serverConfig: ServerConfigService,
        abortController: ApolloAbortControllerService,
        platformId: object,
      ): ApolloClient<any> => {
        const corrId = resolveCorrId(isPlatformBrowser(platformId));

        return new ApolloClient<any>({
          credentials: 'include',
          connectToDevTools: !environment.production,
          defaultOptions: {
            query: {
              context: {
                fetchOptions: {
                  signal: abortController.signal,
                },
              },
            },
          },
          link: ApolloLink.from([
            removeTypenameFromVariables(),
            onError(({ graphQLErrors, networkError }) => {
              if (networkError) {
                httpErrorInterceptorService.interceptNetworkError(networkError);
              }
              if (graphQLErrors) {
                httpErrorInterceptorService.interceptGraphQLErrors(
                  graphQLErrors,
                );
              }
            }),
            new HttpLink({
              uri: `${serverConfig.apiUrl}/graphql`,
              credentials: 'include',
              headers: {
                'x-corr-id': corrId,
                // 'x-product': appConfig.activeProductConfig.product,
              },
            }),
          ]),
          cache: new InMemoryCache(),
        });
      },
    },
    provideIonicAngular(),
    provideHttpClient(withInterceptorsFromDi(), withFetch()),
    provideAnimations(),
  ],
};
