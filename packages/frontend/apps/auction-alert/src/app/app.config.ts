import {
  ApplicationConfig,
  PLATFORM_ID,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { appRoutes } from './app.routes';
import {
  provideClientHydration,
  withEventReplay,
} from '@angular/platform-browser';
import { provideIonicAngular } from '@ionic/angular/standalone';
import {
  ApolloClient,
  ApolloLink,
  HttpLink,
  InMemoryCache,
} from '@apollo/client/core';
import {
  ApolloAbortControllerService,
  HttpErrorInterceptorService,
  ServerConfigService,
} from '@feedless/components';
import { environment } from '@feedless/core';
import { removeTypenameFromVariables } from '@apollo/client/link/remove-typename';
import { onError } from '@apollo/client/link/error';

export const appConfig: ApplicationConfig = {
  providers: [
    provideClientHydration(withEventReplay()),
    provideBrowserGlobalErrorListeners(),
    provideRouter(appRoutes),
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
                'x-corr-id': '',
                // 'x-product': appConfig.activeProductConfig.product,
              },
            }),
          ]),
          cache: new InMemoryCache(),
        });
      },
    },
    provideIonicAngular(),
  ],
};
