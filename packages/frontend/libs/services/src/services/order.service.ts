import { inject, Injectable } from '@angular/core';
import {
  GqlOrderCreateInput,
  GqlOrdersInput,
  GqlOrdersQuery,
  GqlOrdersQueryVariables,
  GqlOrderUpdateInput,
  GqlOrderWhereUniqueInput,
  GqlUpsertOrderMutation,
  GqlUpsertOrderMutationVariables,
  Orders,
  UpsertOrder,
} from '@feedless/graphql-api';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { Nullable, Order } from '@feedless/shared-types';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);

  async orders(
    data: GqlOrdersInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<Order[]> {
    return this.apollo
      .query<GqlOrdersQuery, GqlOrdersQueryVariables>({
        query: Orders,
        variables: {
          data,
        },
        fetchPolicy,
      })
      .then((response) => response.data.orders);
  }

  async upsertOrder(
    create: GqlOrderCreateInput,
    where: Nullable<GqlOrderWhereUniqueInput> = undefined,
    update: Nullable<GqlOrderUpdateInput> = undefined,
  ): Promise<Order> {
    return this.apollo
      .mutate<GqlUpsertOrderMutation, GqlUpsertOrderMutationVariables>({
        mutation: UpsertOrder,
        variables: {
          data: {
            create,
            update,
            where,
          },
        },
      })
      .then((response) => response.data.upsertOrder);
  }
}
