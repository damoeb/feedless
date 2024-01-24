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
  GqlProfileQuery,
  GqlProfileQueryVariables,
  GqlUpdateCurrentUserInput,
  GqlUpdateCurrentUserMutation,
  GqlUpdateCurrentUserMutationVariables,
  Logout,
  Profile as ProfileQuery,
  UpdateCurrentUser
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { Profile, UserSecret } from '../graphql/types';
import { BehaviorSubject, filter, Observable, ReplaySubject } from 'rxjs';
import { isNull, isUndefined } from 'lodash-es';

export const dateFormat = 'dd.MM.YYYY';
export const dateTimeFormat = 'HH:mm, dd.MM.YYYY';
export const TimeFormat = 'HH:mm, dd.MM.YYYY';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private profile: Profile = {} as any;
  private darkModePipe: ReplaySubject<boolean>;
  private profilePipe: BehaviorSubject<Profile>;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.profilePipe = new BehaviorSubject(null);
    this.detectColorScheme();
  }

  getProfile(): Observable<Profile> {
    return this.profilePipe
      .asObservable()
      .pipe(filter((profile) => !isNull(profile) && !isUndefined(profile)));
  }

  watchColorScheme(): Observable<boolean> {
    return this.darkModePipe.asObservable();
  }

  setColorScheme(dark: boolean): void {
    this.darkModePipe.next(dark);
  }

  async fetchProfile(fetchPolicy: FetchPolicy = 'cache-first'): Promise<void> {
    await this.apollo
      .query<GqlProfileQuery, GqlProfileQueryVariables>({
        query: ProfileQuery,
        fetchPolicy
      })
      .then((response) => response.data.profile)
      .then(async (profile) => {
        this.authService.changeAuthStatus(profile.isLoggedIn);
        this.profile = profile;
        this.profilePipe.next(profile);

        if (profile.isLoggedIn) {
          if (!profile.user.hasAcceptedTerms) {
            await this.authService.showTermsAndConditions();
          }
        }
      });
  }

  async acceptTermsAndConditions(): Promise<void> {
    const { dateFormat, timeFormat } = this.getBrowserDateTimeFormats();
    await this.updateCurrentUser({
      acceptedTermsAndServices: {
        set: true
      },
      timeFormat: {
        set: timeFormat
      },
      dateFormat: {
        set: dateFormat
      }
    })
      .then(() => this.fetchProfile('network-only'))
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
          data
        }
      })
      .then(() => this.fetchProfile('network-only'));
  }

  async createUserSecret(): Promise<UserSecret> {
    return this.apollo
      .mutate<
        GqlCreateUserSecretMutation,
        GqlCreateUserSecretMutationVariables
      >({
        mutation: CreateUserSecret
      })
      .then((response) => response.data.createUserSecret);
  }

  async logout(): Promise<void> {
    await this.apollo
      .mutate<GqlLogoutMutation, GqlLogoutMutationVariables>({
        mutation: Logout
      })
      .then(() => new Promise((resolve) => setTimeout(resolve, 200)))
      .then(() => this.apollo.clearStore())
      .then(() => this.fetchProfile('network-only'));
  }

  getUserId(): string {
    return this.profile?.user?.id;
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
        data
      }
    });
  }

  private detectColorScheme() {
    const isDarkMode = window.matchMedia(
      '(prefers-color-scheme: dark)'
    ).matches;
    this.darkModePipe = new ReplaySubject<boolean>(1);
    this.darkModePipe.next(isDarkMode);
    this.darkModePipe.subscribe((isDarkMode) => {
      if (isDarkMode) {
        document.body.className = 'dark';
      } else {
        document.body.className = 'light';
      }
    });
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
