import { Injectable } from '@angular/core';
import {
  GqlFeatureName,
  GqlFeatureState,
  GqlServerSettingsQuery,
  GqlServerSettingsQueryVariables,
  ServerSettings
} from '../../generated/graphql';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client/core';
import { AlertController } from '@ionic/angular';
import { FlatFeature } from '../graphql/types';

export interface Config {
  apiUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class ServerSettingsService {
  apiUrl: string;
  private features: Array<FlatFeature>;
  private expectedFeatureState: GqlFeatureState = GqlFeatureState.Stable;

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
              host: location.host
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

  getFeature(featureName: GqlFeatureName): FlatFeature {
    return this.features.find((ft) => ft.name === featureName);
  }

  isFeatureOff(featureName: GqlFeatureName): boolean {
    const feature = this.features.find((ft) => ft.name === featureName);
    if (feature) {
      return feature.state === GqlFeatureState.Off;
    }
    console.warn(`Feature ${featureName} not listed`);
    return false;
  }

  canUseFeature(featureName: GqlFeatureName): boolean {
    const expectedState = this.expectedFeatureState;
    const feature = this.features.find((ft) => ft.name === featureName);
    if (feature) {
      if (feature.state === GqlFeatureState.Off) {
        return false;
      }
      switch (expectedState) {
        case GqlFeatureState.Experimental:
          return true;
        case GqlFeatureState.Beta:
          return feature.state !== GqlFeatureState.Experimental;
        case GqlFeatureState.Stable:
          return feature.state === GqlFeatureState.Stable;
      }
    } else {
      return false;
    }
  }

  canUseFeatures(featureNames: GqlFeatureName[]) {
    return featureNames.every((featureName) => this.canUseFeature(featureName));
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
