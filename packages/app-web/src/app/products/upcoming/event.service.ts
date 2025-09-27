import { inject, Injectable } from '@angular/core';
import {
  EventsByIds,
  GqlEventsByIdsQuery,
  GqlEventsByIdsQueryVariables,
  GqlRecordsInput,
} from '../../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import type { DefaultContext } from '@apollo/client/core/types';
import { GetElementType } from '../../graphql/types';

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
