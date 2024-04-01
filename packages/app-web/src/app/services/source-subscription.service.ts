import { Injectable } from '@angular/core';
import {
  CreateSourceSubscriptions,
  DeleteSourceSubscription,
  GqlCreateSourceSubscriptionsMutation,
  GqlCreateSourceSubscriptionsMutationVariables,
  GqlDeleteSourceSubscriptionMutation,
  GqlDeleteSourceSubscriptionMutationVariables,
  GqlFeatureName,
  GqlListSourceSubscriptionsQuery,
  GqlListSourceSubscriptionsQueryVariables,
  GqlSourceSubscriptionByIdQuery,
  GqlSourceSubscriptionByIdQueryVariables,
  GqlSourceSubscriptionsCreateInput,
  GqlSourceSubscriptionsInput,
  GqlSourceSubscriptionUniqueWhereInput,
  GqlSourceSubscriptionUpdateInput,
  GqlUpdateSourceSubscriptionMutation,
  GqlUpdateSourceSubscriptionMutationVariables,
  ListSourceSubscriptions,
  SourceSubscriptionById,
  UpdateSourceSubscription,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { SourceSubscription } from '../graphql/types';
import { ServerSettingsService } from './server-settings.service';
import { ProfileService } from './profile.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class SourceSubscriptionService {
  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly serverSetting: ServerSettingsService,
    private readonly router: Router,
    private readonly profileService: ProfileService,
  ) {}

  async createSubscriptions(
    data: GqlSourceSubscriptionsCreateInput,
  ): Promise<SourceSubscription[]> {
    if (
      this.profileService.isAuthenticated() ||
      this.serverSetting.isEnabled(GqlFeatureName.CanCreateAsAnonymous)
    ) {
      return this.apollo
        .mutate<
          GqlCreateSourceSubscriptionsMutation,
          GqlCreateSourceSubscriptionsMutationVariables
        >({
          mutation: CreateSourceSubscriptions,
          variables: {
            data,
          },
        })
        .then((response) => response.data.createSourceSubscriptions);
    } else {
      // todo mag handle
      // if (this.serverSetting.isEnabled(GqlFeatureName.HasWaitList) && !this.serverSetting.isEnabled(GqlFeatureName.CanSignUp)) {
      if (this.serverSetting.isEnabled(GqlFeatureName.CanSignUp)) {
        await this.router.navigateByUrl('/login');
      } else {
        if (this.serverSetting.isEnabled(GqlFeatureName.HasWaitList)) {
          await this.router.navigateByUrl('/join');
        }
      }
    }
  }

  deleteSubscription(
    data: GqlSourceSubscriptionUniqueWhereInput,
  ): Promise<void> {
    return this.apollo
      .mutate<
        GqlDeleteSourceSubscriptionMutation,
        GqlDeleteSourceSubscriptionMutationVariables
      >({
        mutation: DeleteSourceSubscription,
        variables: {
          data,
        },
      })
      .then();
  }

  updateSubscription(
    data: GqlSourceSubscriptionUpdateInput,
  ): Promise<SourceSubscription> {
    return this.apollo
      .mutate<
        GqlUpdateSourceSubscriptionMutation,
        GqlUpdateSourceSubscriptionMutationVariables
      >({
        mutation: UpdateSourceSubscription,
        variables: {
          data,
        },
      })
      .then((response) => response.data.updateSourceSubscription);
  }

  listSourceSubscriptions(
    data: GqlSourceSubscriptionsInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<SourceSubscription[]> {
    return this.apollo
      .query<
        GqlListSourceSubscriptionsQuery,
        GqlListSourceSubscriptionsQueryVariables
      >({
        query: ListSourceSubscriptions,
        variables: {
          data,
        },
        fetchPolicy,
      })
      .then((response) => response.data.sourceSubscriptions);
  }

  async getSubscriptionById(
    id: string,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<SourceSubscription> {
    return this.apollo
      .query<
        GqlSourceSubscriptionByIdQuery,
        GqlSourceSubscriptionByIdQueryVariables
      >({
        query: SourceSubscriptionById,
        fetchPolicy,
        variables: {
          data: {
            where: {
              id,
            },
          },
        },
      })
      .then((response) => response.data.sourceSubscription);
  }
}
