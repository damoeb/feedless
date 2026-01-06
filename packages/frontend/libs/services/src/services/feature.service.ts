import { inject, Injectable } from '@angular/core';
import {
  Feature,
  FeatureGroup,
  FeatureGroups,
  GqlFeatureGroupsQuery,
  GqlFeatureGroupsQueryVariables,
  GqlFeatureGroupWhereInput,
  GqlFeatureName,
  GqlUpdateFeatureValueInput,
  GqlUpdateFeatureValueMutation,
  GqlUpdateFeatureValueMutationVariables,
  UpdateFeatureValue,
} from '@feedless/graphql-api';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { SessionService } from './session.service';

@Injectable({
  providedIn: 'root',
})
export class FeatureService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);
  private readonly sessionService = inject(SessionService);
  private features!: Feature[];

  findAll(
    where: GqlFeatureGroupWhereInput,
    inherit: boolean,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<FeatureGroup[]> {
    return this.apollo
      .query<GqlFeatureGroupsQuery, GqlFeatureGroupsQueryVariables>({
        query: FeatureGroups,
        variables: {
          where,
          inherit,
        },
        fetchPolicy,
      })
      .then((response) => response.data.featureGroups);
  }

  updateFeatureValue(data: GqlUpdateFeatureValueInput) {
    return this.apollo
      .mutate<
        GqlUpdateFeatureValueMutation,
        GqlUpdateFeatureValueMutationVariables
      >({
        mutation: UpdateFeatureValue,
        variables: {
          data,
        },
      })
      .then((response) => response.data!.updateFeatureValue);
  }

  getFeatureValueInt(featureName: GqlFeatureName): number | undefined {
    const feature = this.sessionService.getFeature(featureName);
    if (feature) {
      return feature.value.numVal!.value;
    }
    return;
  }

  getFeatureValueBool(featureName: GqlFeatureName): boolean {
    const feature = this.sessionService.getFeature(featureName);
    if (feature) {
      return feature.value.boolVal!.value;
    }
    console.warn(`Feature ${featureName} not listed`);
    return false;
  }
}
