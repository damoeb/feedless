import { Injectable } from '@angular/core';
import {
  AuthAnonymous,
  AuthViaMail,
  ConfirmCode,
  GqlAuthAnonymousMutation,
  GqlAuthAnonymousMutationVariables,
  GqlAuthentication,
  GqlAuthViaMailSubscription,
  GqlAuthViaMailSubscriptionVariables,
  GqlConfirmCodeMutation,
  GqlConfirmCodeMutationVariables,
} from '../../generated/graphql';
import { ApolloClient, FetchResult, Observable } from '@apollo/client/core';
import jwt_decode from 'jwt-decode';

export type ActualAuthentication = Pick<
  GqlAuthentication,
  'token' | 'authorities' | 'corrId'
>;

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

  requestAuthForUser(
    email: string
  ): Observable<FetchResult<GqlAuthViaMailSubscription>> {
    return this.apollo.subscribe<
      GqlAuthViaMailSubscription,
      GqlAuthViaMailSubscriptionVariables
    >({
      query: AuthViaMail,
      variables: {
        data: email,
      },
      context: {
        headers: {
          authorization: `Bearer ${localStorage.getItem(this.authTokenKey)}`,
        },
      },
    });
  }

  sendConfirmationCode(confirmationCode: string, otpId: string) {
    return this.apollo.mutate<
      GqlConfirmCodeMutation,
      GqlConfirmCodeMutationVariables
    >({
      mutation: ConfirmCode,
      variables: {
        data: {
          code: confirmationCode,
          otpId,
        },
      },
    });
  }

  async requireAnyAuthToken(): Promise<void> {
    if (this.isAuthenticated()) {
      this.patchApollo();
    } else {
      const authentication = await this.requestAuthForAnonymous();
      this.handleAuthentication(authentication);
    }
  }

  handleAuthentication(authentication: ActualAuthentication) {
    localStorage.setItem(this.authTokenKey, authentication.token);
    console.log(
      'authenticated',
      jwt_decode<RichAuthToken>(authentication.token)
    );
    this.patchApollo();
  }

  isAuthenticated(): boolean {
    const authToken = localStorage.getItem(this.authTokenKey);
    if (authToken) {
      const jwt = jwt_decode<RichAuthToken>(authToken);
      return jwt.exp * 1000 > new Date().getTime();
    }
    return false;
  }

  private patchApollo() {
    this.apollo.defaultOptions.query = {
      context: {
        headers: {
          authorization: `Bearer ${localStorage.getItem(this.authTokenKey)}`,
        },
      },
    };
  }

  private requestAuthForAnonymous(): Promise<ActualAuthentication> {
    return this.apollo
      .mutate<GqlAuthAnonymousMutation, GqlAuthAnonymousMutationVariables>({
        mutation: AuthAnonymous,
      })
      .then((response) => response.data.authAnonymous);
  }
}
