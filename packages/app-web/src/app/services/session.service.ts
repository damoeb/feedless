import { Injectable } from '@angular/core';
import {
  CreateUserSecret,
  DeleteUserSecrets,
  GqlCreateUserSecretMutation,
  GqlCreateUserSecretMutationVariables,
  GqlDeleteUserSecretsInput,
  GqlDeleteUserSecretsMutation,
  GqlDeleteUserSecretsMutationVariables,
  GqlLogoutMutation,
  GqlLogoutMutationVariables,
  GqlSessionQuery,
  GqlSessionQueryVariables,
  GqlUpdateCurrentUserInput,
  GqlUpdateCurrentUserMutation,
  GqlUpdateCurrentUserMutationVariables,
  Logout,
  Session as SessionQuery,
  UpdateCurrentUser,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { Session, UserSecret } from '../graphql/types';
import { BehaviorSubject, filter, Observable, ReplaySubject } from 'rxjs';
import { isNull, isUndefined } from 'lodash-es';

export const dateFormat = 'dd.MM.YYYY';
export const dateTimeFormat = 'HH:mm, dd.MM.YYYY';
export const TimeFormat = 'HH:mm, dd.MM.YYYY';

@Injectable({
  providedIn: 'root',
})
export class SessionService {
  private session: Session = {} as any;
  private darkModePipe: ReplaySubject<boolean>;
  private sessionPipe: BehaviorSubject<Session>;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {
    this.sessionPipe = new BehaviorSubject(null);
    this.detectColorScheme();
  }

  getSession(): Observable<Session> {
    return this.sessionPipe
      .asObservable()
      .pipe(filter((session) => !isNull(session) && !isUndefined(session)));
  }

  watchColorScheme(): Observable<boolean> {
    return this.darkModePipe.asObservable();
  }

  setColorScheme(dark: boolean): void {
    this.darkModePipe.next(dark);
  }

  async fetchSession(fetchPolicy: FetchPolicy = 'cache-first'): Promise<void> {
    await this.apollo
      .query<GqlSessionQuery, GqlSessionQueryVariables>({
        query: SessionQuery,
        fetchPolicy,
      })
      .then((response) => response.data.session)
      .then(async (session) => {
        this.authService.changeAuthStatus(session.isLoggedIn);
        this.session = session;
        this.sessionPipe.next(session);

        if (session.isLoggedIn) {
          if (!session.user.hasAcceptedTerms) {
            await this.authService.showTermsAndConditions();
          }
        }
      });
  }

  async acceptTermsAndConditions(): Promise<void> {
    const { dateFormat, timeFormat } = this.getBrowserDateTimeFormats();
    await this.updateCurrentUser({
      acceptedTermsAndServices: {
        set: true,
      },
      timeFormat: {
        set: timeFormat,
      },
      dateFormat: {
        set: dateFormat,
      },
    })
      .then(() => this.fetchSession('network-only'))
      .then(() => this.router.navigateByUrl('/'));
  }

  async updateCurrentUser(data: GqlUpdateCurrentUserInput): Promise<void> {
    await this.apollo
      .mutate<
        GqlUpdateCurrentUserMutation,
        GqlUpdateCurrentUserMutationVariables
      >({
        mutation: UpdateCurrentUser,
        variables: {
          data,
        },
      })
      .then(() => this.fetchSession('network-only'));
  }

  async createUserSecret(): Promise<UserSecret> {
    return this.apollo
      .mutate<
        GqlCreateUserSecretMutation,
        GqlCreateUserSecretMutationVariables
      >({
        mutation: CreateUserSecret,
      })
      .then((response) => response.data.createUserSecret);
  }

  async logout(): Promise<void> {
    await this.apollo
      .mutate<GqlLogoutMutation, GqlLogoutMutationVariables>({
        mutation: Logout,
      })
      .then(() => new Promise((resolve) => setTimeout(resolve, 200)))
      .then(() => this.apollo.clearStore())
      .then(() => this.fetchSession('network-only'));
  }

  getUserId(): string {
    return this.session?.user?.id;
  }

  isAuthenticated() {
    return this.getUserId()?.length > 0;
  }

  async deleteUserSecrets(data: GqlDeleteUserSecretsInput) {
    await this.apollo.mutate<
      GqlDeleteUserSecretsMutation,
      GqlDeleteUserSecretsMutationVariables
    >({
      mutation: DeleteUserSecrets,
      variables: {
        data,
      },
    });
  }

  private detectColorScheme() {
    const isDarkMode = window.matchMedia(
      '(prefers-color-scheme: dark)',
    ).matches;
    this.darkModePipe = new ReplaySubject<boolean>(1);
    this.darkModePipe.next(isDarkMode);
  }

  private getBrowserDateTimeFormats() {
    const now = new Date(2013, 11, 31, 12, 1, 2);
    const dateFormat = now
      .toLocaleDateString()
      .replace('31', 'dd')
      .replace('12', 'MM')
      .replace('2013', 'yyyy');

    const timeFormat = now
      .toLocaleTimeString()
      .replace('12', 'HH')
      .replace('01', 'mm')
      .replace('AM', 'a');
    return { dateFormat, timeFormat };
  }
}
