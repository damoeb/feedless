import { Injectable } from '@angular/core';
import {
  GqlFeature,
  GqlFeatureBooleanValue,
  GqlFeatureIntValue,
  GqlPlan,
  GqlPlansQuery,
  GqlPlansQueryVariables,
  Maybe,
  Plans,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { Plan } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class PlanService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  async fetchPlans(): Promise<Plan[]> {
    return this.apollo
      .query<GqlPlansQuery, GqlPlansQueryVariables>({
        query: Plans,
      })
      .then((response) => response.data.plans);
  }
}
