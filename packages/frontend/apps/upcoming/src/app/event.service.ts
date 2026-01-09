import { inject, Injectable } from '@angular/core';
import {
  EventsByIds,
  GetElementType,
  GqlEventsByIdsQuery,
  GqlEventsByIdsQueryVariables,
  GqlRecordsInput,
} from '@feedless/graphql-api';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import type { DefaultContext } from '@apollo/client/core/types';

export type LocalizedEvent = GetElementType<GqlEventsByIdsQuery['records']>;

@Injectable({
  providedIn: 'root',
})
export class EventService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);

  findAllByRepositoryId(
    data: GqlRecordsInput,
    fetchPolicy: FetchPolicy = 'cache-first',
    context: DefaultContext = null,
  ): Promise<LocalizedEvent[]> {
    return this.apollo
      .query<GqlEventsByIdsQuery, GqlEventsByIdsQueryVariables>({
        query: EventsByIds,
        context,
        variables: {
          data,
        },
        fetchPolicy,
      })
      .then((response) => {
        return response.data.records;
      });
  }
}
