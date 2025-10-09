import { enableProdMode, importProvidersFrom } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { environment } from './environments/environment';
import { RouteReuseStrategy } from '@angular/router';
import {
  IonicRouteStrategy,
  provideIonicAngular,
  ModalController,
  IonApp,
  IonRouterOutlet,
} from '@ionic/angular/standalone';
import { ApolloClient, split, ApolloLink, HttpLink, InMemoryCache } from '@apollo/client/core';
import { HttpErrorInterceptorService } from './app/services/http-error-interceptor.service';
import { ServerConfigService } from './app/services/server-config.service';
import { AppConfigService } from './app/services/app-config.service';
import { ApolloAbortControllerService } from './app/services/apollo-abort-controller.service';
import { getMainDefinition } from '@apollo/client/utilities';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { removeTypenameFromVariables } from '@apollo/client/link/remove-typename';
import { onError } from '@apollo/client/link/error';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { BrowserModule, bootstrapApplication } from '@angular/platform-browser';
import { AppRoutingModule } from './app/app-routing.module';
import { provideAnimations } from '@angular/platform-browser/animations';
import { AppLoadModule } from './app/app-load.module';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, appConfig).catch((err) => console.log(err));
