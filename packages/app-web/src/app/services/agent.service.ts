import { Injectable } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import { Agents, GqlAgentsQuery, GqlAgentsQueryVariables } from '../../generated/graphql';
import { GetElementType } from '../graphql/types';

export type Agent = GetElementType<GqlAgentsQuery['agents']>;

@Injectable({
  providedIn: 'root',
})
export class AgentService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  getAgents(): Promise<Agent[]> {
    return this.apollo
      .query<GqlAgentsQuery, GqlAgentsQueryVariables>({
        query: Agents,
      })
      .then((response) => {
        return response.data.agents;
      });
  }
}
