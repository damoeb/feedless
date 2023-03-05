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

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private preferFulltext: boolean;
  private preferReader: boolean;
  private name: string;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly authService: AuthService,
    private readonly serverSettingsService: ServerSettingsService
  ) {}

  useFulltext(): boolean {
    return this.preferFulltext;
  }

  getName(): string {
    return this.name;
  }

  async fetchProfile(fetchPolicy: FetchPolicy = 'cache-first'): Promise<void> {
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
          if (!profile.user.acceptedTermsAndServices) {
            await this.authService.showTermsAndConditions();
          }
        }
      });
  }

  async acceptTermsAndConditions(): Promise<void> {
    await this.apollo
      .mutate<
        GqlAcceptTermsAndConditionsMutation,
        GqlAcceptTermsAndConditionsMutationVariables
      >({
        mutation: AcceptTermsAndConditions,
      })
      .then(() => this.fetchProfile());
  }

  async logout(): Promise<void> {
    await this.apollo
      .mutate<GqlLogoutMutation, GqlLogoutMutationVariables>({
        mutation: Logout,
      })
      .then(() => this.fetchProfile('network-only'));
  }
}
