import { Injectable } from '@angular/core';
import {
  CreateApiToken,
  DeleteApiTokens,
  GqlCreateApiTokenMutation,
  GqlCreateApiTokenMutationVariables,
  GqlDeleteApiTokensInput,
  GqlDeleteApiTokensMutation,
  GqlDeleteApiTokensMutationVariables,
  GqlLogoutMutation,
  GqlLogoutMutationVariables,
  GqlProfileQuery,
  GqlProfileQueryVariables,
  GqlUpdateCurrentUserInput,
  GqlUpdateCurrentUserMutation,
  GqlUpdateCurrentUserMutationVariables,
  Logout,
  Profile as ProfileQuery,
  UpdateCurrentUser,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { Profile, UserSecret } from '../graphql/types';
import { BehaviorSubject, filter, Observable, ReplaySubject } from 'rxjs';
import { isNull, isUndefined } from 'lodash-es';

@Injectable({
  providedIn: 'root',
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

  useFulltext(): boolean {
    return this.profile.preferFulltext;
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
        fetchPolicy,
      })
      .then((response) => response.data.profile)
      .then(async (profile) => {
        this.authService.changeAuthStatus(profile.isLoggedIn);
        this.profile = profile;
        console.log('profile', profile);
        this.profilePipe.next(profile);

        if (profile.isLoggedIn) {
          if (!profile.user.acceptedTermsAndServices) {
            await this.authService.showTermsAndConditions();
          }
        }
      });
  }

  async acceptTermsAndConditions(): Promise<void> {
    await this.updateCurrentUser({
      acceptedTermsAndServices: {
        set: true,
      },
    })
      .then(() => this.fetchProfile('network-only'))
      .then(() => this.router.navigateByUrl('/buckets'));
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
      .then(() => this.fetchProfile('network-only'));
  }

  async createApiToken(): Promise<UserSecret> {
    return this.apollo
      .mutate<GqlCreateApiTokenMutation, GqlCreateApiTokenMutationVariables>({
        mutation: CreateApiToken,
      })
      .then((response) => response.data.createApiToken);
  }

  async logout(): Promise<void> {
    await this.apollo
      .mutate<GqlLogoutMutation, GqlLogoutMutationVariables>({
        mutation: Logout,
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

  async deleteApiTokens(data: GqlDeleteApiTokensInput) {
    await this.apollo.mutate<
      GqlDeleteApiTokensMutation,
      GqlDeleteApiTokensMutationVariables
    >({
      mutation: DeleteApiTokens,
      variables: {
        data,
      },
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
}
