import { Injectable, inject } from '@angular/core';
import {
  GqlScrapeQuery,
  GqlScrapeQueryVariables,
  GqlSourceInput,
  Scrape,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { AuthService } from './auth.service';
import { ScrapeResponse } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class ScrapeService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);
  private readonly authService = inject(AuthService);


  async scrape(source: GqlSourceInput): Promise<ScrapeResponse> {
    await this.authService.requireAnyAuthToken();
    return this.apollo
      .query<GqlScrapeQuery, GqlScrapeQueryVariables>({
        query: Scrape,
        variables: {
          data: source,
        },
      })
      .then((response) => {
        return response.data.scrape;
      });
  }
}
