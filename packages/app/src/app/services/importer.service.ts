import { Injectable } from '@angular/core';
import {
  CreateImporter,
  DeleteImporter,
  GqlCreateImporterMutation,
  GqlCreateImporterMutationVariables,
  GqlDeleteImporterMutation,
  GqlDeleteImporterMutationVariables,
  GqlGenericFeed,
  GqlImporter,
  GqlImporterByIdQuery,
  GqlImporterByIdQueryVariables,
  GqlImporterCreateInput, GqlImportersQuery, GqlImportersQueryVariables, GqlImportersWhereInput,
  GqlImporterWhereInput,
  ImporterById, Importers,
  Maybe
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { BasicNativeFeed } from './feed.service';
import { BasicBucket } from './bucket.service';
import { Pagination } from './pagination.service';

export type BasicImporter = Pick<
  GqlImporter,
  | 'id'
  | 'email'
  | 'filter'
  | 'webhook'
  | 'autoRelease'
  | 'createdAt'
  | 'health'
  | 'nativeFeedId'
  | 'bucketId'
  | 'lastUpdatedAt'
>;

export type Importer = BasicImporter & {
  nativeFeed: BasicNativeFeed & {
    genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
  };
};

@Injectable({
  providedIn: 'root',
})
export class ImporterService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  createImporter(data: GqlImporterCreateInput): Promise<BasicImporter> {
    return this.apollo
      .mutate<GqlCreateImporterMutation, GqlCreateImporterMutationVariables>({
        mutation: CreateImporter,
        variables: {
          data,
        },
      })
      .then((response) => response.data.createImporter);
  }

  async deleteImporter(id: string): Promise<void> {
    await this.apollo.mutate<
      GqlDeleteImporterMutation,
      GqlDeleteImporterMutationVariables
    >({
      mutation: DeleteImporter,
      variables: {
        data: {
          where: { id },
        },
      },
    });
  }

  async getImporter(data: GqlImporterWhereInput): Promise<Importer> {
    return this.apollo
      .query<GqlImporterByIdQuery, GqlImporterByIdQueryVariables>({
        query: ImporterById,
        variables: {
          data,
        },
      })
      .then((response) => response.data.importer as Importer);
  }

  async getImporters(data: GqlImportersWhereInput): Promise<{ importers: Importer[]; pagination: Pagination }> {
    return this.apollo
      .query<GqlImportersQuery, GqlImportersQueryVariables>({
        query: Importers,
        variables: {
          data,
        },
      })
      .then((response) => response.data.importers);
  }
}
