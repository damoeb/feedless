import { inject, Injectable } from '@angular/core';
import {
  GqlAuthType,
  GqlProfileName,
  GqlServerSettingsQuery,
  GqlServerSettingsQueryVariables,
  GqlVertical,
  LocalizedLicense,
  ServerSettings,
} from '@feedless/graphql-api';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, firstValueFrom, Observable } from 'rxjs';
import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client/core';
import { environment } from '../../environments/environment';
import { VerticalAppConfig } from '@feedless/shared-types';

export type BuildInfo = GqlServerSettingsQuery['serverSettings']['build'];

export interface ConfigError {
  header: string;
  message: string;
  cssClass?: string;
  retryable?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class ServerConfigService {
  private readonly httpClient = inject(HttpClient);

  apiUrl!: string;
  private profiles!: GqlProfileName[];
  private authTypes!: GqlAuthType[];
  private connected = false;
  private build: BuildInfo = {
    commit: 'unknown',
    date: new Date(),
  };
  private version!: string;
  private license: LocalizedLicense;

  private readonly systemErrorSubject = new BehaviorSubject<ConfigError | null>(
    null,
  );
  public readonly systemError$: Observable<ConfigError | null> =
    this.systemErrorSubject.asObservable();

  async fetchConfig(): Promise<VerticalAppConfig> {
    try {
      const config = await firstValueFrom(
        this.httpClient.get<VerticalAppConfig>('/config.json'),
      );

      this.apiUrl = config.apiUrl;

      const product = config.product;

      const throwInvalidConfigError = (message: string) => {
        this.systemErrorSubject.next({
          header: 'Invalid Config',
          cssClass: 'fatal-alert medium-alert',
          message: message,
          retryable: false,
        });
        console.error(message);
      };

      if (product) {
        console.log(`enabling product ${product}`);
        const products: GqlVertical[] = Object.keys(GqlVertical).map(
          // @ts-ignore
          (p) => GqlVertical[p],
        );
        console.log(`Know products ${products.join(', ')}`);
        if (!products.some((otherProduct) => otherProduct == product)) {
          const message = `Product '${product}' does not exist. Know products are ${products.join(', ')}`;
          throwInvalidConfigError(message);
        }
      } else {
        throwInvalidConfigError(
          `Cannot map hostname ${location.hostname} to product`,
        );
      }

      return config;
    } catch (error) {
      this.systemErrorSubject.next({
        header: 'Configuration Error',
        message:
          'Failed to load application configuration. Please check your connection and try again.',
        retryable: true,
      });
      console.error(error);
    }
  }

  isConnected(): boolean {
    return this.connected;
  }

  async fetchServerSettings(): Promise<void> {
    try {
      const response = await this.createApolloClient()
        .query<GqlServerSettingsQuery, GqlServerSettingsQueryVariables>({
          query: ServerSettings,
          variables: {
            data: {
              host: location.host,
              product: environment.product,
            },
          },
        })
        .then((response) => response.data.serverSettings);

      this.connected = true;
      this.profiles = response.profiles;
      this.authTypes = response.auth;
      this.version = response.version;
      this.build = response.build;
      this.license = response.license;
    } catch (e) {
      if (!environment.offlineSupport) {
        this.systemErrorSubject.next({
          header: 'Connection lost',
          message: `Cannot reach the server, maybe you are offline`,
          retryable: true,
        });
        console.error(e);
      }
    }
  }

  createApolloClient(): ApolloClient<any> {
    return new ApolloClient<any>({
      link: new HttpLink({ uri: `${this.apiUrl}/graphql` }),
      cache: new InMemoryCache(),
    });
  }

  hasProfile(profile: GqlProfileName) {
    return this.profiles?.indexOf(profile) > -1;
  }

  hasAuthType(authType: GqlAuthType) {
    return this.authTypes?.indexOf(authType) > -1;
  }

  isSelfHosted() {
    return this.hasProfile(GqlProfileName.SelfHosted);
  }

  isSaas() {
    return !this.isSelfHosted();
  }

  getBuild(): BuildInfo {
    return this.build;
  }

  getVersion() {
    return this.version;
  }

  getLicense(): LocalizedLicense {
    return this.license;
  }

  setLicense(license: LocalizedLicense) {
    this.license = license;
  }
}
