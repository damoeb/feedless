import { inject, Injectable } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import {
  FeedlessPlugin,
  GqlListPluginsQuery,
  GqlListPluginsQueryVariables,
  ListPlugins,
} from '@feedless/graphql-api';

@Injectable({
  providedIn: 'root',
})
export class PluginService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);

  async listPlugins(): Promise<FeedlessPlugin[]> {
    return this.apollo
      .query<GqlListPluginsQuery, GqlListPluginsQueryVariables>({
        query: ListPlugins,
      })
      .then((response) => response.data.plugins);
  }
}
