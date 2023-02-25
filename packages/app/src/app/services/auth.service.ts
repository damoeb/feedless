import { Injectable } from '@angular/core';
import {
  AuthAnonymous, AuthViaMail,
  GqlAuthAnonymousMutation,
  GqlAuthAnonymousMutationVariables,
  GqlAuthentication,
  GqlAuthViaMailSubscription, GqlAuthViaMailSubscriptionVariables
} from '../../generated/graphql';
import { ApolloClient, FetchResult, Observable } from '@apollo/client/core';
import jwt_decode from 'jwt-decode';

export type ActualAuthentication = Pick<GqlAuthentication, 'token' | 'authorities'>;

interface RichAuthToken {
  authorities: string[];
  exp: number;
  iat: number;
  id: string;
  iss: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private readonly authTokenKey = 'authToken';

  constructor(private readonly apollo: ApolloClient<any>) {}

  requestAuthForUser(email: string): Observable<FetchResult<GqlAuthViaMailSubscription>> {
    return this.apollo
      .subscribe<
        GqlAuthViaMailSubscription,
        GqlAuthViaMailSubscriptionVariables
        >({
        query: AuthViaMail,
        variables: {
          data: email,
        },
        context: {
          headers: {
            authorization: `Bearer ${localStorage.getItem(this.authTokenKey)}`
          }
        }
      });
  }

  async requireAnyAuthToken(): Promise<void> {
    if (!this.hasAuthToken()) {
      const token = await this.requestAuthForAnonymous().then(response => response.token);
      localStorage.setItem(this.authTokenKey, token);
      console.log('Authenticated');
    }
    this.apollo.defaultOptions.query = {
      context: {
        headers: {
          authorization: `Bearer ${localStorage.getItem(this.authTokenKey)}`
        }
      }
    };
  }

  private requestAuthForAnonymous(): Promise<ActualAuthentication> {
    return this.apollo.mutate<GqlAuthAnonymousMutation, GqlAuthAnonymousMutationVariables>({
      mutation: AuthAnonymous,
    }).then(response => response.data.authAnonymous);
  }

  private hasAuthToken(): boolean {
    const authToken = localStorage.getItem(this.authTokenKey);
    if (authToken) {
      const jwt = jwt_decode<RichAuthToken>(authToken);
      console.log('jwt', jwt);
      return jwt.exp * 1000 > new Date().getTime();
    }
    return false;
  }
}
