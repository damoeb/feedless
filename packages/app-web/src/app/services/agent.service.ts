import { Injectable } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import {
  Agents,
  GqlAgent,
  GqlAgentsQuery,
  GqlAgentsQueryVariables,
} from '../../generated/graphql';
import { GetElementType } from '../graphql/types';
import { Observable } from 'rxjs';
import Zen from 'zen-observable-ts';

export type Agent = GetElementType<GqlAgentsQuery['agents']>;

export const zenToRx = <T>(zenObservable: Zen.Observable<T>): Observable<T> =>
  new Observable((observer) => zenObservable.subscribe(observer));

@Injectable({
  providedIn: 'root',
})
export class AgentService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  getAgents(): Observable<Array<Agent>> {
    return zenToRx(
      this.apollo
        .watchQuery<GqlAgentsQuery, GqlAgentsQueryVariables>({
          query: Agents,
        })
        .map((response) => {
          return response.data.agents;
        }),
    );
  }
}
