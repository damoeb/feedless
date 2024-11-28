import { Injectable, inject } from '@angular/core';
import {
  FeatureGroups,
  GqlFeatureGroupsQuery,
  GqlFeatureGroupsQueryVariables,
  GqlFeatureGroupWhereInput,
  GqlUpdateFeatureValueInput,
  GqlUpdateFeatureValueMutation,
  GqlUpdateFeatureValueMutationVariables,
  UpdateFeatureValue,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { FeatureGroup } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class FeatureService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);


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
}
