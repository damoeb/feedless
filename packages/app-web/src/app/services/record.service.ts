import { Injectable } from '@angular/core';
import {
  CreateRecords,
  DeleteRecordsById,
  FullRecordByIds,
  GqlCreateRecordInput,
  GqlCreateRecordsMutation,
  GqlCreateRecordsMutationVariables,
  GqlDeleteRecordsByIdMutation,
  GqlDeleteRecordsByIdMutationVariables,
  GqlDeleteRecordsInput,
  GqlFullRecordByIdsQuery,
  GqlFullRecordByIdsQueryVariables,
  GqlRecordByIdsQuery,
  GqlRecordByIdsQueryVariables,
  GqlRecordsInput,
  GqlUpdateRecordInput,
  GqlUpdateRecordMutation,
  GqlUpdateRecordMutationVariables,
  RecordByIds,
  UpdateRecord,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { Record, RecordFull } from '../graphql/types';

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

  findAllFullByRepositoryId(
    data: GqlRecordsInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<RecordFull[]> {
    return this.apollo
      .query<GqlFullRecordByIdsQuery, GqlFullRecordByIdsQueryVariables>({
        query: FullRecordByIds,
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
        return response.data!.deleteRecords;
      });
  }

  createRecordFromUpload(
    caption: string,
    file: File,
    dataUrl: URL | string,
  ): Promise<Record> {
    return Promise.resolve(undefined);
  }

  createRecords(records: GqlCreateRecordInput[]) {
    return this.apollo
      .mutate<GqlCreateRecordsMutation, GqlCreateRecordsMutationVariables>({
        mutation: CreateRecords,
        variables: {
          records,
        },
      })
      .then((response) => {
        return response.data!.createRecords;
      });
  }

  updateRecord(data: GqlUpdateRecordInput) {
    return this.apollo
      .mutate<GqlUpdateRecordMutation, GqlUpdateRecordMutationVariables>({
        mutation: UpdateRecord,
        variables: {
          data,
        },
      })
      .then((response) => {
        return response.data!.updateRecord;
      });
  }
}
