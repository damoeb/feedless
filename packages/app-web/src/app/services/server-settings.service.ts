import { Injectable } from '@angular/core';
import {
  GqlFeatureName,
  GqlServerSettingsQuery,
  GqlServerSettingsQueryVariables,
  ServerSettings
} from '../../generated/graphql';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client/core';
import { AlertController } from '@ionic/angular';
import { Feature } from '../graphql/types';
import { environment } from '../../environments/environment';

export interface Config {
  apiUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class ServerSettingsService {
  apiUrl: string;
  private features: Array<Feature>;

  constructor(
    private readonly httpClient: HttpClient,
    private readonly alertCtrl: AlertController
  ) {
  }

  async fetchServerSettings(): Promise<void> {
    try {
      const config = await firstValueFrom(
        this.httpClient.get<Config>('/config.json')
      );
      this.apiUrl = config.apiUrl;
      const { features } = await this.createApolloClient()
        .query<GqlServerSettingsQuery, GqlServerSettingsQueryVariables>({
          query: ServerSettings,
          variables: {
            data: {
              host: location.host,
              product: environment.product()
            }
          }
        })
        .then((response) => response.data.serverSettings);
      this.features = features;
    } catch (e) {
      this.showOfflineAlert();
      throw e;
    }
  }

  createApolloClient(): ApolloClient<any> {
    return new ApolloClient<any>({
      link: new HttpLink({ uri: `${this.apiUrl}/graphql` }),
      cache: new InMemoryCache()
    });
  }

  isEnabled(featureName: GqlFeatureName): boolean {
    const feature = this.features.find((ft) => ft.name === featureName);
    if (feature) {
      return feature.value.boolVal.value;
    }
    console.warn(`Feature ${featureName} not listed`);
    return false;
  }

  private async showOfflineAlert() {
    const alert = await this.alertCtrl.create({
      header: 'Server is not reachable',
      backdropDismiss: false,
      message: 'Either you are offline or the server is down.',
      buttons: [
        {
          text: 'Retry',
          role: 'confirm',
          handler: () => {
            location.reload();
          }
        }
      ]
    });

    await alert.present();
  }
}
