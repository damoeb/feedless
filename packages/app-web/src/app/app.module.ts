import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouteReuseStrategy } from '@angular/router';

import { IonicModule, IonicRouteStrategy } from '@ionic/angular';
import { HttpClientModule } from '@angular/common/http';
import { ApolloClient, ApolloLink, HttpLink, InMemoryCache, split } from '@apollo/client/core';
import { onError } from '@apollo/client/link/error';
import { getMainDefinition } from '@apollo/client/utilities';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { HttpErrorInterceptorService } from './services/http-error-interceptor.service';
import { environment } from '../environments/environment';
import { AppLoadModule } from './app-load.module';
import { ServerConfigService } from './services/server-config.service';
import { FinalizeProfileModalModule } from './modals/finalize-profile-modal/finalize-profile-modal.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { GqlProductCategory } from '../generated/graphql';
import { ProductTitleModule } from './components/product-title/product-title.module';
import { ApolloAbortControllerService } from './services/apollo-abort-controller.service';
import { removeTypenameFromVariables } from '@apollo/client/link/remove-typename';
import { isNull, isUndefined } from 'lodash-es';

export interface AppEnvironment {
  production: boolean;
  offlineSupport: boolean;
  product: GqlProductCategory;
  officialFeedlessUrl: string;
}

export interface ModalCancel {
  cancel: true;
}

export interface ModalSuccess {
  cancel: false;
  data?: any;
}

export const isUrl = (value: string): boolean => {
  if (!value || value.length < 3) {
    return false;
  }
  const potentialUrl = value.toLowerCase();
  if (
    potentialUrl.startsWith('http://') ||
    potentialUrl.startsWith('https://')
  ) {
    try {
      new URL(value);

      const urlPattern =
        /[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{2,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)?/gi;
      return !!potentialUrl.match(new RegExp(urlPattern));
    } catch (e) {
      return false;
    }
  } else {
    return isUrl(`https://${potentialUrl}`);
  }
};

export const isValidUrl = (value: string): boolean => {
  const potentialUrl = value.trim();
  return (
    potentialUrl.toLowerCase().startsWith('http://') ||
    potentialUrl.toLowerCase().startsWith('https://')
  );
};
export const fixUrl = (value: string): string => {
  const potentialUrl = value?.trim();
  if (isValidUrl(potentialUrl)) {
    return potentialUrl;
  } else {
    if (isNull(value) || isUndefined(value)) {
      throw new Error('invalid url');
    } else {
      try {
        const fixedUrl = `https://${potentialUrl}`;
        new URL(fixedUrl);
        return fixedUrl;
      } catch (e) {
        throw new Error('invalid url');
      }
    }
  }
};

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    IonicModule.forRoot(),
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FinalizeProfileModalModule,
    AppLoadModule,
    ProductTitleModule,
  ],
  providers: [
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    {
      provide: ApolloClient,
      deps: [
        HttpErrorInterceptorService,
        ServerConfigService,
        ApolloAbortControllerService,
      ],
      useFactory: (
        httpErrorInterceptorService: HttpErrorInterceptorService,
        serverConfig: ServerConfigService,
        abortController: ApolloAbortControllerService,
      ): ApolloClient<any> => {
        const wsUrl = `${serverConfig.apiUrl.replace(
          'http',
          'ws',
        )}/subscriptions`;
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
                  'x-CORR-ID': corrId,
                },
              }),
            ]),
          ),
          cache: new InMemoryCache(),
        });
      },
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
