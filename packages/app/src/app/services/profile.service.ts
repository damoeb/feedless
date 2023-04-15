import { Injectable } from '@angular/core';
import {
  AcceptTermsAndConditions,
  GqlAcceptTermsAndConditionsMutation,
  GqlAcceptTermsAndConditionsMutationVariables,
  GqlLogoutMutation,
  GqlLogoutMutationVariables,
  GqlPlan,
  GqlPlanSubscription,
  GqlProfile,
  GqlProfileQuery,
  GqlProfileQueryVariables,
  GqlUser,
  Logout,
  Maybe,
  Profile,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

export type Profile = Pick<
  GqlProfile,
  'minimalFeatureState' | 'preferFulltext' | 'preferReader' | 'isLoggedIn'
> & {
  user?: Maybe<
    Pick<
      GqlUser,
      'id' | 'acceptedTermsAndServices' | 'name' | 'notificationsStreamId'
    > & {
      subscription?: Maybe<
        Pick<GqlPlanSubscription, 'expiry' | 'startedAt'> & {
          plan: Pick<
            GqlPlan,
            'id' | 'name' | 'availability' | 'isPrimary' | 'costs'
          >;
        }
      >;
    }
  >;
};

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private profile: Profile;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  useFulltext(): boolean {
    return this.profile.preferFulltext;
  }

  getProfile(): Profile {
    return this.profile;
  }

  getName(): string {
    return this.profile.user?.name;
  }

  async fetchProfile(fetchPolicy: FetchPolicy = 'cache-first'): Promise<void> {
    await this.apollo
      .query<GqlProfileQuery, GqlProfileQueryVariables>({
        query: Profile,
        fetchPolicy,
      })
      .then((response) => response.data.profile)
      .then(async (profile) => {
        this.authService.changeAuthStatus(profile.isLoggedIn);
        this.profile = profile;

        if (profile.isLoggedIn) {
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
      .then(() => this.fetchProfile('network-only'))
      .then(() => this.router.navigateByUrl('/buckets'));
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
    return this.profile.user.notificationsStreamId;
  }

  getUserId(): string {
    return this.profile?.user?.id;
  }

  isAuthenticated() {
    return this.getUserId()?.length > 0;
  }
}
