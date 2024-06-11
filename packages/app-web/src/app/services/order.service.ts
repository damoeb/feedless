import { Injectable } from '@angular/core';
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
  UpsertOrder
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { Order } from '../types';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  constructor(private readonly apollo: ApolloClient<any>) {}

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
    where: GqlOrderWhereUniqueInput = null,
    update: GqlOrderUpdateInput = null,
  ): Promise<Order> {
    return this.apollo
      .mutate<GqlUpsertOrderMutation, GqlUpsertOrderMutationVariables>({
        mutation: UpsertOrder,
        variables: {
          create,
          update,
          where,
        },
      })
      .then((response) => response.data.upsertOrder);
  }
}
