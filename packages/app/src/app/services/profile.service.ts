import { Injectable } from '@angular/core';
import {
  AcceptTermsAndConditions,
  GqlAcceptTermsAndConditionsMutation,
  GqlAcceptTermsAndConditionsMutationVariables,
  GqlLogoutMutation,
  GqlLogoutMutationVariables,
  GqlProfileQuery,
  GqlProfileQueryVariables,
  Logout,
  Profile,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { ServerSettingsService } from './server-settings.service';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private preferFulltext: boolean;
  private preferReader: boolean;
  private name: string;
  private notificationsStreamId: string;
  private userId: string;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly serverSettingsService: ServerSettingsService
  ) {}

  useFulltext(): boolean {
    return this.preferFulltext;
  }

  getName(): string {
    return this.name;
  }

  async fetchProfile(fetchPolicy: FetchPolicy = 'cache-first'): Promise<void> {
    console.log('fetchProfile', fetchPolicy);
    await this.apollo
      .query<GqlProfileQuery, GqlProfileQueryVariables>({
        query: Profile,
        fetchPolicy,
      })
      .then((response) => response.data.profile)
      .then(async (profile) => {
        console.log('profile', profile);
        this.serverSettingsService.applyProfile(
          profile.featuresOverwrites,
          profile.minimalFeatureState
        );
        this.preferFulltext = profile.preferFulltext;
        this.authService.changeAuthStatus(profile.isLoggedIn);
        this.preferReader = profile.preferReader;

        if (profile.isLoggedIn) {
          this.name = profile.user.name;
          this.userId = profile.user.id;
          this.notificationsStreamId = profile.user.notificationsStreamId;
          if (!profile.user.acceptedTermsAndServices) {
            await this.authService.showTermsAndConditions();
          }
        }
      });
  }

  async acceptTermsAndConditions(): Promise<void> {
    console.log('acceptTermsAndConditions');
    await this.apollo
      .mutate<
        GqlAcceptTermsAndConditionsMutation,
        GqlAcceptTermsAndConditionsMutationVariables
      >({
        mutation: AcceptTermsAndConditions,
      })
      .then(() => this.fetchProfile('network-only'));
  }

  async logout(): Promise<void> {
    await this.apollo
      .mutate<GqlLogoutMutation, GqlLogoutMutationVariables>({
        mutation: Logout,
      })
      .then(() => new Promise((resolve) => setTimeout(resolve, 200)))
      .then(() => this.fetchProfile('network-only'));
  }

  getNotificationsStreamId(): string {
    return this.notificationsStreamId;
  }

  getUserId(): string {
    return this.userId;
  }

  isAuthenticated() {
    return this.userId?.length > 0;
  }
}
