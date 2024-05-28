import { Injectable } from '@angular/core';
import {
  Billings,
  GqlBillingCreateInput,
  GqlBillingsInput,
  GqlBillingsQuery,
  GqlBillingsQueryVariables,
  GqlBillingUpdateInput,
  GqlBillingWhereUniqueInput,
  GqlUpsertBillingMutation,
  GqlUpsertBillingMutationVariables,
  UpsertBilling
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { Billing } from '../types';

@Injectable({
  providedIn: 'root',
})
export class BillingService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  async billings(
    data: GqlBillingsInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<Billing[]> {
    return this.apollo
      .query<GqlBillingsQuery, GqlBillingsQueryVariables>({
        query: Billings,
        variables: {
          data,
        },
        fetchPolicy,
      })
      .then((response) => response.data.billings);
  }

  async upsertBilling(
    create: GqlBillingCreateInput,
    where: GqlBillingWhereUniqueInput = null,
    update: GqlBillingUpdateInput = null,
  ): Promise<Billing> {
    return this.apollo
      .mutate<GqlUpsertBillingMutation, GqlUpsertBillingMutationVariables>({
        mutation: UpsertBilling,
        variables: {
          create,
          update,
          where,
        },
      })
      .then((response) => response.data.upsertBilling);
  }
}
