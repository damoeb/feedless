import { Injectable } from '@angular/core';
import {
  CreateImporters,
  DeleteImporter,
  GqlCreateImportersMutation,
  GqlCreateImportersMutationVariables,
  GqlDeleteImporterMutation,
  GqlDeleteImporterMutationVariables,
  GqlGenericFeed,
  GqlImporter,
  GqlImporterByIdQuery,
  GqlImporterByIdQueryVariables,
  GqlImporterInput,
  GqlImportersCreateInput,
  GqlImportersPagedInput,
  GqlImportersQuery,
  GqlImportersQueryVariables,
  ImporterById,
  Importers,
  Maybe,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { BasicNativeFeed } from './feed.service';
import { Pagination } from './pagination.service';

export type BasicImporter = Pick<
  GqlImporter,
  | 'id'
  | 'email'
  | 'filter'
  | 'webhook'
  | 'autoRelease'
  | 'createdAt'
  | 'nativeFeedId'
  | 'bucketId'
  | 'title'
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

  createImporters(data: GqlImportersCreateInput): Promise<BasicImporter[]> {
    return this.apollo
      .mutate<GqlCreateImportersMutation, GqlCreateImportersMutationVariables>({
        mutation: CreateImporters,
        variables: {
          data,
        },
      })
      .then((response) => response.data.createImporters);
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

  async getImporter(data: GqlImporterInput): Promise<Importer> {
    return this.apollo
      .query<GqlImporterByIdQuery, GqlImporterByIdQueryVariables>({
        query: ImporterById,
        variables: {
          data,
        },
      })
      .then((response) => response.data.importer as Importer);
  }

  async getImporters(
    data: GqlImportersPagedInput
  ): Promise<{ importers: Importer[]; pagination: Pagination }> {
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
