import { inject, Injectable } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import {
  ConnectedAppById,
  DeleteConnectedApp,
  GqlConnectedAppByIdQuery,
  GqlConnectedAppByIdQueryVariables,
  GqlDeleteConnectedAppMutation,
  GqlDeleteConnectedAppMutationVariables,
  GqlUpdateConnectedAppMutation,
  GqlUpdateConnectedAppMutationVariables,
  UpdateConnectedApp,
} from '@feedless/graphql-api';

export type ConnectedApp = GqlConnectedAppByIdQuery['connectedApp'];

@Injectable({
  providedIn: 'root',
})
export class ConnectedAppService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);

  async findById(id: string): Promise<ConnectedApp> {
    return this.apollo
      .query<GqlConnectedAppByIdQuery, GqlConnectedAppByIdQueryVariables>({
        query: ConnectedAppById,
        variables: {
          id,
        },
      })
      .then((response) => response.data.connectedApp);
  }

  async updateConnectedApp(id: string, authorize: boolean): Promise<void> {
    await this.apollo.mutate<
      GqlUpdateConnectedAppMutation,
      GqlUpdateConnectedAppMutationVariables
    >({
      mutation: UpdateConnectedApp,
      variables: {
        id,
        authorize,
      },
    });
  }

  async deleteConnectedApp(id: string): Promise<void> {
    await this.apollo.mutate<
      GqlDeleteConnectedAppMutation,
      GqlDeleteConnectedAppMutationVariables
    >({
      mutation: DeleteConnectedApp,
      variables: {
        id,
      },
    });
  }
}
