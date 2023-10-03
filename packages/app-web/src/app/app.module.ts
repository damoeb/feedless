import { NgModule } from '@angular/core';
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
import { FeedlessMenuModule } from './sidemenus/feedless-menu/feedless-menu.module';
import { ReaderMenuModule } from './sidemenus/reader-menu/reader-menu.module';
import { VisualDiffMenuModule } from './sidemenus/visual-diff-menu/visual-diff-menu.module';

export type AppProduct =
  | 'visual-diff'
  | 'reader'
  | 'feedless'
  | 'rss'
  | 'page-change-tracker'
  | 'blog';

export interface AppEnvironment {
  production: boolean;
  product: () => AppProduct;
}

export interface ModalCancel {
  cancel: true;
}
export interface ModalSuccess {
  cancel: false;
  data?: any;
}
export type ModalDismissal = ModalCancel | ModalSuccess;

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    IonicModule.forRoot(),
    AppRoutingModule,
    HttpClientModule,
    TermsModalModule,
    AppLoadModule,
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: environment.production,
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000',
    }),
    FeedlessMenuModule,
    ReaderMenuModule,
    VisualDiffMenuModule,
  ],
  providers: [
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    {
      provide: ApolloClient,
      deps: [HttpErrorInterceptorService, ServerSettingsService],
      useFactory: (
        httpErrorInterceptorService: HttpErrorInterceptorService,
        serverSettings: ServerSettingsService,
      ): ApolloClient<any> => {
        const wsUrl = `${serverSettings.apiUrl.replace(
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
                uri: `${serverSettings.apiUrl}/graphql`,
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
