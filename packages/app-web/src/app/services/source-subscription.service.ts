import { Injectable } from '@angular/core';
import {
  CreateSourceSubscriptions,
  DeleteSourceSubscription,
  GqlCreateSourceSubscriptionsMutation,
  GqlCreateSourceSubscriptionsMutationVariables,
  GqlDeleteSourceSubscriptionMutation,
  GqlDeleteSourceSubscriptionMutationVariables,
  GqlListSourceSubscriptionsQuery,
  GqlListSourceSubscriptionsQueryVariables,
  GqlSourceSubscriptionByIdQuery,
  GqlSourceSubscriptionByIdQueryVariables,
  GqlSourceSubscriptionsCreateInput,
  GqlSourceSubscriptionsInput,
  GqlSourceSubscriptionUniqueWhereInput,
  ListSourceSubscriptions,
  SourceSubscriptionById
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { SourceSubscription } from '../graphql/types';

@Injectable({
  providedIn: 'root'
})
export class SourceSubscriptionService {
  constructor(private readonly apollo: ApolloClient<any>) {
  }

  createSubscriptions(
    data: GqlSourceSubscriptionsCreateInput
  ): Promise<SourceSubscription[]> {
    return this.apollo
      .mutate<
        GqlCreateSourceSubscriptionsMutation,
        GqlCreateSourceSubscriptionsMutationVariables
      >({
        mutation: CreateSourceSubscriptions,
        variables: {
          data
        }
      })
      .then((response) => response.data.createSourceSubscriptions);
  }

  deleteSubscription(
    data: GqlSourceSubscriptionUniqueWhereInput
  ): Promise<void> {
    return this.apollo
      .mutate<
        GqlDeleteSourceSubscriptionMutation,
        GqlDeleteSourceSubscriptionMutationVariables
      >({
        mutation: DeleteSourceSubscription,
        variables: {
          data
        }
      })
      .then();
  }

  listSourceSubscriptions(
    data: GqlSourceSubscriptionsInput,
    fetchPolicy: FetchPolicy = 'cache-first'
  ): Promise<SourceSubscription[]> {
    return this.apollo
      .query<
        GqlListSourceSubscriptionsQuery,
        GqlListSourceSubscriptionsQueryVariables
      >({
        query: ListSourceSubscriptions,
        variables: {
          data
        },
        fetchPolicy
      })
      .then((response) => response.data.sourceSubscriptions);
  }

  async getSubscriptionById(
    id: string,
    fetchPolicy: FetchPolicy = 'cache-first'
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
              id
            }
          }
        }
      })
      .then((response) => response.data.sourceSubscription);
  }
}
