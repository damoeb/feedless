import { inject, Injectable } from '@angular/core';
import {
  EventsByIds,
  GetElementType,
  GqlEventsByIdsQuery,
  GqlEventsByIdsQueryVariables,
  GqlRecordByIdQuery,
  GqlRecordByIdQueryVariables,
  GqlRecordsInput,
  RecordById,
} from '@feedless/graphql-api';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import type { DefaultContext } from '@apollo/client/core/types';
import { Dayjs } from 'dayjs';
import { getDateConstraints } from './pages/event-calendar/event-calendar.page';
import { dedupeEvents } from './event-dedup';

export type LocalizedEvent = GetElementType<GqlEventsByIdsQuery['records']>;

@Injectable({
  providedIn: 'root',
})
export class EventService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);

  fetchEventsBetweenDates(
    date: Dayjs,
    repositoryId: string,
    lat: number,
    lng: number,
  ): Promise<LocalizedEvent[]> {
    const { minDate, maxDate } = getDateConstraints(date);
    return this.findAllByRepositoryId({
      cursor: {
        page: 0,
        pageSize: 50,
      },
      where: {
        repository: {
          id: repositoryId,
        },
        latLng: {
          near: {
            point: {
              lat,
              lng,
            },
            distanceKm: 10,
          },
        },
        startedAt: {
          after: minDate.startOf('day').valueOf(),
          before: maxDate.endOf('day').valueOf(),
        },
      },
    }).then(dedupeEvents);
  }

  /**
   * Ein einzelnes Event für die Detailseite. `record(data:)` und der
   * id-Filter existieren im Schema, es braucht keinen Codegen-Lauf.
   */
  findById(id: string): Promise<LocalizedEvent | null> {
    return this.apollo
      .query<GqlRecordByIdQuery, GqlRecordByIdQueryVariables>({
        query: RecordById,
        variables: {
          data: {
            where: { id },
          },
        },
        fetchPolicy: 'cache-first',
      })
      .then((response) => (response.data.record as LocalizedEvent) ?? null)
      .catch((): null => null);
  }

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
