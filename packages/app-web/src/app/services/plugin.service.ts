import { Injectable } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import { GqlListPluginsQuery, GqlListPluginsQueryVariables, ListPlugins } from '../../generated/graphql';
import { FeedlessPlugin } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class PluginService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  async listPlugins(): Promise<FeedlessPlugin[]> {
    return this.apollo
      .query<GqlListPluginsQuery, GqlListPluginsQueryVariables>({
        query: ListPlugins,
      })
      .then((response) => response.data.plugins);
  }
}
