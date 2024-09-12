import { Injectable } from '@angular/core';
import {
  AuthAnonymous,
  AuthUser,
  AuthViaMail,
  ConfirmCode,
  GqlAuthAnonymousMutation,
  GqlAuthAnonymousMutationVariables,
  GqlAuthUserInput,
  GqlAuthUserMutation,
  GqlAuthUserMutationVariables,
  GqlAuthViaMailSubscription,
  GqlAuthViaMailSubscriptionVariables,
  GqlConfirmCodeMutation,
  GqlConfirmCodeMutationVariables,
} from '../../generated/graphql';
import {
  ApolloClient,
  FetchResult,
  Observable as ApolloObservable,
} from '@apollo/client/core';
import {
  BehaviorSubject,
  firstValueFrom,
  map,
  Observable,
  Subject,
  take,
} from 'rxjs';
import jwt_decode from 'jwt-decode';
import { ActualAuthentication } from '../graphql/types';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';

interface RichAuthToken {
  authorities: string[];
  exp: number;
  iat: number;
  id: string;
  iss: string;
  user_id: string;
}

export interface Authentication {
  loggedIn: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly authStatus: Subject<Authentication>;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly router: Router,
  ) {
    this.authStatus = new BehaviorSubject(null);
  }

  authorizationChange(): Observable<Authentication> {
    return this.authStatus.asObservable();
  }

  async authorizeUserViaMail(
    email: string,
  ): Promise<ApolloObservable<FetchResult<GqlAuthViaMailSubscription>>> {
    const authentication = await this.authorizeAnonymous();
    return this.apollo.subscribe<
      GqlAuthViaMailSubscription,
      GqlAuthViaMailSubscriptionVariables
    >({
      query: AuthViaMail,
      variables: {
        data: {
          email,
          token: authentication.token,
          product: environment.product,
          osInfo: ``,
          allowCreate: true,
        },
      },
    });
  }

  async authorizeUser(data: GqlAuthUserInput): Promise<void> {
    return this.apollo
      .mutate<GqlAuthUserMutation, GqlAuthUserMutationVariables>({
        mutation: AuthUser,
        variables: {
          data,
        },
      })
      .then((response) =>
        this.handleAuthenticationToken(response.data.authUser.token),
      );
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
    return firstValueFrom(
      this.isAuthenticated()
        .pipe(take(1))
        .pipe(
          map(async (authenticated) => {
            if (!authenticated) {
              const authentication = await this.authorizeAnonymous();
              await this.handleAuthenticationToken(authentication.token);
            }
          }),
        ),
    );
  }

  async handleAuthenticationToken(token: string) {
    const decodedToken = jwt_decode<RichAuthToken>(token);
    // console.log('handleAuthenticationToken', decodedToken);
    // todo mag add timeout when token expires to trigger change event
    this.authStatus.next({
      loggedIn: decodedToken.user_id?.length > 0,
    });
  }

  isAuthenticated(): Observable<boolean> {
    return this.authorizationChange().pipe(
      map((status) => status?.loggedIn === true),
    );
  }

  changeAuthStatus(loggedIn: boolean) {
    console.log('changeAuthStatus', loggedIn);
    if (loggedIn) {
      const redirectUrl = localStorage.getItem('redirectUrl');
      if (redirectUrl) {
        this.router.navigateByUrl(redirectUrl);
        localStorage.removeItem('redirectUrl');
      }
    }
    this.authStatus.next({ loggedIn });
  }

  private authorizeAnonymous(): Promise<ActualAuthentication> {
    return this.apollo
      .mutate<GqlAuthAnonymousMutation, GqlAuthAnonymousMutationVariables>({
        mutation: AuthAnonymous,
      })
      .then((response) => response.data.authAnonymous);
  }

  rememberRedirectUrl(redirectUrl: string) {
    console.log('remeber', redirectUrl);
    localStorage.setItem('redirectUrl', redirectUrl);
  }
}
