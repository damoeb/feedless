import { Injectable } from '@angular/core';
import {
  Features,
  GqlFeaturesQuery,
  GqlFeaturesQueryVariables,
  GqlUpdateFeatureInput,
  GqlUpdateFeatureMutation,
  GqlUpdateFeatureMutationVariables,
  UpdateFeature
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { Feature } from '../graphql/types';
import { zenToRx } from './agent.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class FeatureService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  findAll(fetchPolicy: FetchPolicy = 'cache-first'): Observable<Feature[]> {
    return zenToRx(
      this.apollo
        .watchQuery<GqlFeaturesQuery, GqlFeaturesQueryVariables>({
          query: Features,
          fetchPolicy,
        })
        .map((response) => response.data.features),
    );
  }

  updateFeature(data: GqlUpdateFeatureInput) {
    return this.apollo
      .mutate<GqlUpdateFeatureMutation, GqlUpdateFeatureMutationVariables>({
        mutation: UpdateFeature,
        variables: {
          data,
        },
      })
      .then((response) => response.data.updateFeature);
  }
}
