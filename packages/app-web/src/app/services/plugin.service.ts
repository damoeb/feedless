import { Injectable } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import { GqlListPluginsQuery, GqlListPluginsQueryVariables, ListPlugins } from '../../generated/graphql';
import { Plugin } from '../graphql/types';

@Injectable({
  providedIn: 'root'
})
export class PluginService {
  constructor(private readonly apollo: ApolloClient<any>) {
  }

  async listPlugins(): Promise<Plugin[]> {
    return this.apollo
      .query<GqlListPluginsQuery, GqlListPluginsQueryVariables>({
        query: ListPlugins
      })
      .then((response) => response.data.plugins);
  }
}
