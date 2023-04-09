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

export type Feature = Pick<GqlFeature, 'name' | 'state'> & {
  value?: Maybe<{
    boolVal?: Maybe<Pick<GqlFeatureBooleanValue, 'value'>>;
    numVal?: Maybe<Pick<GqlFeatureIntValue, 'value'>>;
  }>;
};
export type Plan = Pick<
  GqlPlan,
  'id' | 'name' | 'availability' | 'isPrimary' | 'costs'
> & { features: Array<Feature> };

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
