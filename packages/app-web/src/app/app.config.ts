import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { RouteReuseStrategy } from '@angular/router';

import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppLoadModule } from './app-load.module';
import {
  IonicRouteStrategy,
  provideIonicAngular,
} from '@ionic/angular/standalone';
import {
  ApolloClient,
  ApolloLink,
  HttpLink,
  InMemoryCache,
  split,
} from '@apollo/client/core';
import { HttpErrorInterceptorService } from './services/http-error-interceptor.service';
import { ServerConfigService } from './services/server-config.service';
import { AppConfigService } from './services/app-config.service';
import { ApolloAbortControllerService } from './services/apollo-abort-controller.service';
import { environment } from '../environments/environment';
import { getMainDefinition } from '@apollo/client/utilities';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { removeTypenameFromVariables } from '@apollo/client/link/remove-typename';
import { onError } from '@apollo/client/link/error';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

// export const appConfig: ApplicationConfig = {
//   providers: [provideZoneChangeDetection({ eventCoalescing: true }), provideRouter([]), provideClientHydration(withEventReplay())]
// };
export const appConfig: ApplicationConfig = {
  providers: [
    importProvidersFrom(
      BrowserModule,
      AppRoutingModule,
      AppLoadModule,
      // IonApp,
      // IonRouterOutlet,
    ),
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    {
      provide: ApolloClient,
      deps: [
        HttpErrorInterceptorService,
        ServerConfigService,
        AppConfigService,
        ApolloAbortControllerService,
      ],
      useFactory: (
        httpErrorInterceptorService: HttpErrorInterceptorService,
        serverConfig: ServerConfigService,
        appConfig: AppConfigService,
        abortController: ApolloAbortControllerService,
      ): ApolloClient<any> => {
        const wsUrl = `${serverConfig.apiUrl.replace('http', 'ws')}/subscriptions`;
        const newCorrId = (Math.random() + 1)
          .toString(36)
          .substring(7)
          .toUpperCase();
        if (!localStorage.getItem('corrId')) {
          localStorage.setItem('corrId', newCorrId);
        }
        const corrId = localStorage.getItem('corrId') || newCorrId;
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
          link: split(
            ({ query }) => {
              const definition = getMainDefinition(query);
              return (
                definition.kind === 'OperationDefinition' &&
                definition.operation === 'subscription'
              );
            },
            new GraphQLWsLink(
              createClient({
                url: wsUrl,
              }),
            ),
            ApolloLink.from([
              removeTypenameFromVariables(),
              onError(({ graphQLErrors, networkError }) => {
                if (networkError) {
                  httpErrorInterceptorService.interceptNetworkError(
                    networkError,
                  );
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
                  'x-product': appConfig.activeProductConfig.product,
                },
              }),
            ]),
          ),
          cache: new InMemoryCache(),
        });
      },
    },
    provideHttpClient(withInterceptorsFromDi()),
    provideIonicAngular(),
    provideAnimations(),
  ],
};
