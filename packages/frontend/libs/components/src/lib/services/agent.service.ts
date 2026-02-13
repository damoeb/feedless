import { inject, Injectable } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import {
  Agents,
  GetElementType,
  GqlAgentsQuery,
  GqlAgentsQueryVariables,
} from '@feedless/graphql-api';
import { Observable, of, switchMap } from 'rxjs';
import { Observable as ZenObservable } from 'zen-observable-ts';
import { AuthService } from './auth.service';

export type Agent = GetElementType<GqlAgentsQuery['agents']>;

export const zenToRx = <T>(zenObservable: ZenObservable<T>): Observable<T> =>
  new Observable((observer) => zenObservable.subscribe(observer));

@Injectable({
  providedIn: 'root',
})
export class AgentService {
  private readonly authService = inject(AuthService);
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);

  getAgents(): Observable<Array<Agent>> {
    return this.authService.authorizationChange().pipe(
      switchMap((authentication) => {
        if (authentication?.loggedIn) {
          return zenToRx(
            this.apollo
              .watchQuery<GqlAgentsQuery, GqlAgentsQueryVariables>({
                query: Agents,
              })
              .map((response) => {
                return response.data.agents;
              }),
          );
        } else {
          return of([]);
        }
      }),
    );
  }
}
