import { Injectable } from '@angular/core';
import {
  DeleteRecordsById,
  GqlDeleteRecordsByIdMutation,
  GqlDeleteRecordsByIdMutationVariables,
  GqlDeleteRecordsInput,
  GqlRecordByIdsQuery,
  GqlRecordByIdsQueryVariables,
  GqlRecordsInput,
  RecordByIds,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { Record } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class RecordService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  findAllByRepositoryId(
    data: GqlRecordsInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<Record[]> {
    return this.apollo
      .query<GqlRecordByIdsQuery, GqlRecordByIdsQueryVariables>({
        query: RecordByIds,
        variables: {
          data,
        },
        fetchPolicy,
      })
      .then((response) => {
        return response.data.records;
      });
  }

  removeById(data: GqlDeleteRecordsInput) {
    return this.apollo
      .mutate<
        GqlDeleteRecordsByIdMutation,
        GqlDeleteRecordsByIdMutationVariables
      >({
        mutation: DeleteRecordsById,
        variables: {
          data,
        },
      })
      .then((response) => {
        return response.data.deleteRecords;
      });
  }

  createRecordFromUpload(caption: string, file: File, dataUrl: URL) {
    return Promise.resolve(undefined);
  }

  createRecord(caption: string, data: string) {
    return Promise.resolve(undefined);
  }
}
