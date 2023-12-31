import { Injectable } from '@angular/core';
import {
  CreateSourceSubscriptions,
  GqlCreateSourceSubscriptionsMutation,
  GqlCreateSourceSubscriptionsMutationVariables,
  GqlSourceSubscriptionsCreateInput,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { SourceSubscription } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  createSubscriptions(
    data: GqlSourceSubscriptionsCreateInput,
  ): Promise<SourceSubscription[]> {
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
  }
}
