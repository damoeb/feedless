import { Injectable } from '@angular/core';
import {
  CreateImporters,
  DeleteImporter,
  GqlCreateImportersMutation,
  GqlCreateImportersMutationVariables,
  GqlDeleteImporterMutation,
  GqlDeleteImporterMutationVariables,
  GqlImporterByIdQuery,
  GqlImporterByIdQueryVariables,
  GqlImporterInput,
  GqlImportersCreateInput,
  GqlImportersInput,
  GqlImportersQuery,
  GqlImportersQueryVariables,
  GqlImporterUpdateInput,
  GqlUpdateImporterMutation,
  GqlUpdateImporterMutationVariables,
  ImporterById,
  Importers,
  UpdateImporter,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { BasicImporter, Importer, Pagination } from '../graphql/types';

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

  getImporter(data: GqlImporterInput): Promise<Importer> {
    return this.apollo
      .query<GqlImporterByIdQuery, GqlImporterByIdQueryVariables>({
        query: ImporterById,
        variables: {
          data,
        },
      })
      .then((response) => response.data.importer as Importer);
  }

  getImporters(
    data: GqlImportersInput,
    fetchPolicy: FetchPolicy
  ): Promise<{ importers: Importer[]; pagination: Pagination }> {
    return this.apollo
      .query<GqlImportersQuery, GqlImportersQueryVariables>({
        query: Importers,
        variables: {
          data,
        },
        fetchPolicy,
      })
      .then((response) => response.data.importers);
  }

  updateImporter(data: GqlImporterUpdateInput): Promise<void> {
    return this.apollo
      .mutate<GqlUpdateImporterMutation, GqlUpdateImporterMutationVariables>({
        mutation: UpdateImporter,
        variables: {
          data,
        },
      })
      .then();
  }
}
