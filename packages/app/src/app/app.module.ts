import { InjectionToken, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouteReuseStrategy } from '@angular/router';

import { IonicModule, IonicRouteStrategy } from '@ionic/angular';
import { HttpClientModule } from '@angular/common/http';
import {
  ApolloClient,
  ApolloLink,
  HttpLink,
  InMemoryCache,
  split,
} from '@apollo/client/core';
import { onError } from '@apollo/client/link/error';
import { getMainDefinition } from '@apollo/client/utilities';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { HttpErrorInterceptorService } from './services/http-error-interceptor.service';
import { ServiceWorkerModule } from '@angular/service-worker';
import { environment } from '../environments/environment';
import { AppLoadModule } from './app-load.module';
import { ServerSettingsService } from './services/server-settings.service';
import { TermsModalModule } from './modals/terms-modal/terms-modal.module';

export interface ModalCancel {
  cancel: true;
}
export interface ModalSuccess {
  cancel: false;
  data?: any;
}
export type ModalDismissal = ModalCancel | ModalSuccess;

export const GRAPHQL_HTTP = new InjectionToken<ApolloClient<any>>(
  'graphql http',
  {
    providedIn: 'root',
    factory: () =>
      new ApolloClient<any>({
        link: new HttpLink({
          uri: '/graphql',
          headers: {
            'x-CORR-ID': (Math.random() + 1)
              .toString(36)
              .substring(7)
              .toUpperCase(),
          },
        }),
        cache: new InMemoryCache(),
      }),
  }
);

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    IonicModule.forRoot(),
    AppRoutingModule,
    TermsModalModule,
    HttpClientModule,
    AppLoadModule,
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: environment.production,
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000',
    }),
  ],
  providers: [
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    {
      provide: ApolloClient,
      deps: [HttpErrorInterceptorService, GRAPHQL_HTTP, ServerSettingsService],
      useFactory: (
        httpErrorInterceptorService: HttpErrorInterceptorService,
        graphqlHttp: ApolloClient<any>,
        serverSettings: ServerSettingsService
      ): ApolloClient<any> => {
        const wsUrl = `ws://${serverSettings.publicUrl}/subscriptions`;
        return new ApolloClient<any>({
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
                connectionParams: {
                  authToken: 'user.authToken',
                },
              })
            ),
            ApolloLink.from([
              onError(({ graphQLErrors, networkError }) => {
                if (networkError) {
                  httpErrorInterceptorService.interceptNetworkError(
                    networkError
                  );
                }

                if (graphQLErrors) {
                  httpErrorInterceptorService.interceptGraphQLErrors(
                    graphQLErrors
                  );
                }
              }),
              graphqlHttp.link,
            ])
          ),
          cache: new InMemoryCache(),
        });
      },
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
