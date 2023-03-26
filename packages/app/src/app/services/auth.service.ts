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
import {
  ApolloClient,
  FetchResult,
  Observable as ApolloObservable,
} from '@apollo/client/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Router } from '@angular/router';
import { TermsModalComponent } from '../modals/terms-modal/terms-modal.component';
import { ModalController } from '@ionic/angular';
import Cookies from 'js-cookie';
import jwt_decode from 'jwt-decode';

export type ActualAuthentication = Pick<GqlAuthentication, 'token' | 'corrId'>;

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
  private authenticated = false;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly modalCtrl: ModalController,
    private readonly router: Router
  ) {
    this.authStatus = new BehaviorSubject({
      loggedIn: false,
    });
    this.authorizationChange().subscribe(({ loggedIn }) => {
      this.authenticated = loggedIn;
    });
  }

  authorizationChange(): Observable<Authentication> {
    return this.authStatus.asObservable();
  }

  async requestAuthForUser(
    email: string
  ): Promise<ApolloObservable<FetchResult<GqlAuthViaMailSubscription>>> {
    await this.requireAnyAuthToken();
    return this.apollo.subscribe<
      GqlAuthViaMailSubscription,
      GqlAuthViaMailSubscriptionVariables
    >({
      query: AuthViaMail,
      variables: {
        data: email,
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
    if (!this.isAuthenticated()) {
      const authentication = await this.requestAuthForAnonymous();
      await this.handleAuthenticationToken(authentication.token);
    }
  }

  async handleAuthenticationToken(token: string) {
    const decodedToken = jwt_decode<RichAuthToken>(token);
    console.log('handleAuthenticationToken', decodedToken);
    Cookies.set('TOKEN', token, { expires: decodedToken.exp });
    // todo mag add timeout when token expires to trigger change event
    this.authStatus.next({
      loggedIn: decodedToken.user_id.length > 0,
    });
  }

  isAuthenticated(): boolean {
    return this.authenticated;
  }

  changeAuthStatus(loggedIn: boolean) {
    this.authStatus.next({ loggedIn });
  }

  async showTermsAndConditions() {
    console.log('showTermsAndConditions');
    const modal = await this.modalCtrl.create({
      component: TermsModalComponent,
      backdropDismiss: false,
    });
    await modal.present();
    await modal.onDidDismiss();
  }

  private requestAuthForAnonymous(): Promise<ActualAuthentication> {
    return this.apollo
      .mutate<GqlAuthAnonymousMutation, GqlAuthAnonymousMutationVariables>({
        mutation: AuthAnonymous,
      })
      .then((response) => response.data.authAnonymous);
  }
}