import { Injectable } from '@angular/core';
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
  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly authService: AuthService,
  ) {}

  async scrape(scrapeRequest: GqlSourceInput): Promise<ScrapeResponse> {
    await this.authService.requireAnyAuthToken();
    return this.apollo
      .query<GqlScrapeQuery, GqlScrapeQueryVariables>({
        query: Scrape,
        variables: {
          data: scrapeRequest,
        },
      })
      .then((response) => {
        return response.data.scrape;
      });
  }
}
