import { Inject, Injectable } from '@angular/core';
import {
  GqlApiUrls,
  GqlFeature,
  GqlFeatureName,
  GqlFeatureState,
  GqlServerSettingsQuery,
  GqlServerSettingsQueryVariables,
  ServerSettings,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { GRAPHQL_HTTP } from '../app.module';
import { find } from 'lodash';

export type Feature = Pick<GqlFeature, 'name' | 'state'>;

export type ApiUrls = Pick<GqlApiUrls, 'webToFeed'>;

@Injectable({
  providedIn: 'root',
})
export class ServerSettingsService {
  publicUrl = 'localhost:8080';
  private features: Array<Feature>;
  private apiUrls: ApiUrls;
  private expectedFeatureState: GqlFeatureState = GqlFeatureState.Stable;
  constructor(
    @Inject(GRAPHQL_HTTP) private readonly apollo: ApolloClient<any>
  ) {}

  async fetchServerSettings(): Promise<void> {
    const { features, apiUrls } = await this.apollo
      .query<GqlServerSettingsQuery, GqlServerSettingsQueryVariables>({
        query: ServerSettings,
        variables: {},
      })
      .then((response) => response.data.serverSettings);
    this.features = features;
    // console.log('feature', features);
    this.apiUrls = apiUrls;
  }

  getFeature(featureName: GqlFeatureName): Feature {
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
    featuresOverwrites: Feature[],
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
}
