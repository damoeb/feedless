import { Injectable } from '@angular/core';
import {
  GqlFeatureName,
  GqlFeatureState,
  GqlServerSettingsQuery,
  GqlServerSettingsQueryVariables,
  ServerSettings,
} from '../../generated/graphql';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client/core';
import { AlertController } from '@ionic/angular';
import { ApiUrls, FlatFeature } from '../graphql/types';

interface Config {
  apiUrl: string;
}

@Injectable({
  providedIn: 'root',
})
export class ServerSettingsService {
  apiUrl: string;
  private features: Array<FlatFeature>;
  private apiUrls: ApiUrls;
  private expectedFeatureState: GqlFeatureState = GqlFeatureState.Stable;
  constructor(
    private readonly httpClient: HttpClient,
    private readonly alertCtrl: AlertController
  ) {}

  async fetchServerSettings(): Promise<void> {
    try {
      const config = await firstValueFrom(
        this.httpClient.get<Config>('/config.json')
      );
      this.apiUrl = config.apiUrl;
      const { features, apiUrls } = await new ApolloClient<any>({
        link: new HttpLink({ uri: `${config.apiUrl}/graphql` }),
        cache: new InMemoryCache(),
      })
        .query<GqlServerSettingsQuery, GqlServerSettingsQueryVariables>({
          query: ServerSettings,
        })
        .then((response) => response.data.serverSettings);
      this.features = features;
      this.apiUrls = apiUrls;
    } catch (e) {
      this.showOfflineAlert();
      throw e;
    }
  }

  getFeature(featureName: GqlFeatureName): FlatFeature {
    return this.features.find((ft) => ft.name === featureName);
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

  getApiUrls(): ApiUrls {
    return this.apiUrls;
  }

  applyProfile(
    featuresOverwrites: FlatFeature[],
    minimalFeatureState: GqlFeatureState
  ) {
    this.expectedFeatureState = minimalFeatureState;
    featuresOverwrites.forEach((featureOverwrite) => {
      console.log('featureOverwrite', featureOverwrite);
      // const existingState = find(this.features, {name: featureOverwrite.name});
      // if (existingState) {
      //   existingState.state = featureOverwrite.state;
      // } else {
      //   this.features.push(featureOverwrite);
      // }
    });
  }

  canUseFeatures(featureNames: GqlFeatureName[]) {
    return featureNames.every((featureName) => this.canUseFeature(featureName));
  }

  private async showOfflineAlert() {
    const alert = await this.alertCtrl.create({
      header: 'Connection Troubles',
      backdropDismiss: false,
      message:
        'Server is not reachable, either you are offline or the server is under maintenance.',
      buttons: [
        {
          text: 'Understood',
          role: 'confirm',
        },
      ],
    });

    await alert.present();
  }
}
