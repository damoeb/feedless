import { Injectable } from '@angular/core';
import { GqlPlansQuery, GqlPlansQueryVariables, Plans } from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { Plan } from '../graphql/types';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class PlanService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  async fetchPlans(): Promise<Plan[]> {
    return this.apollo
      .query<GqlPlansQuery, GqlPlansQueryVariables>({
        query: Plans,
        variables: {
          product: environment.product,
        },
      })
      .then((response) => response.data.plans);
  }
}
