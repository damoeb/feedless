import { Injectable } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import {
  Agents,
  GqlAgent,
  GqlAgentsQuery,
  GqlAgentsQueryVariables,
} from '../../generated/graphql';

export type Agent = Pick<
  GqlAgent,
  'addedAt' | 'osInfo' | 'ownerId' | 'secretKeyId' | 'version'
>;

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
