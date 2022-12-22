import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouteReuseStrategy } from '@angular/router';

import { IonicModule, IonicRouteStrategy } from '@ionic/angular';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { HttpClientModule } from '@angular/common/http';
import {
  ApolloClient,
  ApolloLink,
  HttpLink,
  InMemoryCache,
} from '@apollo/client/core';
import { HttpErrorInterceptorService } from './services/http-error-interceptor.service';
import { onError } from '@apollo/client/link/error';
import { ServiceWorkerModule } from '@angular/service-worker';
import { environment } from '../environments/environment';

const uri = '/graphql';

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
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: environment.production,
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000'
    })
  ],
  providers: [
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    {
      provide: ApolloClient,
      deps: [HttpErrorInterceptorService],
      useFactory: (
        httpErrorInterceptorService: HttpErrorInterceptorService
      ): ApolloClient<any> =>
        new ApolloClient<any>({
          link: ApolloLink.from([
            onError(({ graphQLErrors, networkError }) => {
              if (networkError) {
                httpErrorInterceptorService.interceptNetworkError(networkError);
              }

              if (graphQLErrors) {
                httpErrorInterceptorService.interceptGraphQLErrors(
                  graphQLErrors
                );
              }
            }),
            new HttpLink({ uri, headers: { 'x-CORR-ID': 'foo-bar' } }),
          ]),
          cache: new InMemoryCache(),
        }),
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
