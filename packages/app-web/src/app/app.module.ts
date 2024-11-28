import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouteReuseStrategy } from '@angular/router';

import {
  IonicRouteStrategy,
  provideIonicAngular,
  IonApp,
  IonRouterOutlet,
  ModalController,
} from '@ionic/angular/standalone';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
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
import { environment } from '../environments/environment';
import { AppLoadModule } from './app-load.module';
import { ServerConfigService } from './services/server-config.service';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { GqlVertical } from '../generated/graphql';

import { ApolloAbortControllerService } from './services/apollo-abort-controller.service';
import { removeTypenameFromVariables } from '@apollo/client/link/remove-typename';
import { isNull, isUndefined } from 'lodash-es';
import { AppConfigService } from './services/app-config.service';

export interface AppEnvironment {
  production: boolean;
  offlineSupport: boolean;
  product: GqlVertical;
  officialFeedlessUrl: string;
}

export interface ModalCancel {
  cancel: true;
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
