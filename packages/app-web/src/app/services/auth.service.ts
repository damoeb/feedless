import { Injectable } from '@angular/core';
import {
  AuthAnonymous,
  AuthRoot,
  AuthViaMail,
  ConfirmCode,
  GqlAuthAnonymousMutation,
  GqlAuthAnonymousMutationVariables,
  GqlAuthRootInput,
  GqlAuthRootMutation,
  GqlAuthRootMutationVariables,
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
  filter,
  firstValueFrom,
  lastValueFrom,
  map,
  Observable,
  ReplaySubject,
  Subject,
  take,
  takeLast,
} from 'rxjs';
import { TermsModalComponent } from '../modals/terms-modal/terms-modal.component';
import { ModalController } from '@ionic/angular';
import jwt_decode from 'jwt-decode';
import { Router } from '@angular/router';
import { ServerSettingsService } from './server-settings.service';
import { ActualAuthentication } from '../graphql/types';

interface RichAuthToken {
  authorities: string[];
  exp: number;
  iat: number;
  id: string;
  iss: string;
  // eslint-disable-next-line @typescript-eslint/naming-convention
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
  private modalIsOpen = false;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly modalCtrl: ModalController
  ) {
    this.authStatus = new BehaviorSubject(null);
  }

  authorizationChange(): Observable<Authentication> {
    return this.authStatus.asObservable();
  }

  async requestAuthForUser(
    email: string
  ): Promise<ApolloObservable<FetchResult<GqlAuthViaMailSubscription>>> {
    const authentication = await this.requestAuthForAnonymous();
    return this.apollo.subscribe<
      GqlAuthViaMailSubscription,
      GqlAuthViaMailSubscriptionVariables
    >({
      query: AuthViaMail,
      variables: {
        email,
        token: authentication.token,
      },
    });
  }

  async requestAuthForRoot(data: GqlAuthRootInput): Promise<void> {
    return this.apollo
      .mutate<GqlAuthRootMutation, GqlAuthRootMutationVariables>({
        mutation: AuthRoot,
        variables: {
          data,
        },
      })
      .then((response) =>
        this.handleAuthenticationToken(response.data.authRoot.token)
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
            if (authenticated) {
              const authentication = await this.requestAuthForAnonymous();
              await this.handleAuthenticationToken(authentication.token);
            }
          })
        )
    );
  }

  async handleAuthenticationToken(token: string) {
    const decodedToken = jwt_decode<RichAuthToken>(token);
    console.log('handleAuthenticationToken', decodedToken);
    // todo mag add timeout when token expires to trigger change event
    this.authStatus.next({
      loggedIn: decodedToken.user_id.length > 0,
    });
  }

  isAuthenticated(): Observable<boolean> {
    return this.authorizationChange().pipe(
      map((status) => status?.loggedIn === true)
    );
  }

  changeAuthStatus(loggedIn: boolean) {
    this.authStatus.next({ loggedIn });
  }

  // async isAuthenticatedOrRedirect(): Promise<boolean> {
  //   const isAuthenticated = await this.isAuthenticated();
  //   if (!isAuthenticated) {
  //     await this.router.navigateByUrl('/login');
  //     return false;
  //   }
  //   return true;
  // }

  async showTermsAndConditions() {
    if (this.modalIsOpen) {
      return;
    }
    try {
      this.modalIsOpen = true;
      const modal = await this.modalCtrl.create({
        component: TermsModalComponent,
        backdropDismiss: false,
      });
      await modal.present();
      await modal.onDidDismiss();
    } finally {
      this.modalIsOpen = false;
    }
  }

  private requestAuthForAnonymous(): Promise<ActualAuthentication> {
    return this.apollo
      .mutate<GqlAuthAnonymousMutation, GqlAuthAnonymousMutationVariables>({
        mutation: AuthAnonymous,
      })
      .then((response) => response.data.authAnonymous);
  }
}
