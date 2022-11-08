import { Injectable } from '@angular/core';
import {
  CreateImporter, DeleteImporter,
  GqlCreateImporterMutation,
  GqlCreateImporterMutationVariables, GqlDeleteImporterMutation, GqlDeleteImporterMutationVariables,
  GqlImporter, GqlImporterCreateInput,
  GqlNativeFeed
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

export type Importer = GqlImporter;

export type CreateImporterResponse =     Pick<GqlImporter, 'id' | 'active'>
  & { feed: Pick<GqlNativeFeed, 'id'> }

@Injectable({
  providedIn: 'root'
})
export class ImporterService {

  constructor(private readonly apollo: ApolloClient<any>) { }

  createImporter(data: GqlImporterCreateInput): Promise<CreateImporterResponse> {
    return this.apollo
      .mutate<GqlCreateImporterMutation, GqlCreateImporterMutationVariables>({
        mutation: CreateImporter,
        variables: {
          data
        }
      }).then(response => response.data.createImporter)
  }

  async deleteImporter(id: string): Promise<void> {
    await this.apollo
      .mutate<GqlDeleteImporterMutation, GqlDeleteImporterMutationVariables>({
        mutation: DeleteImporter,
        variables: {
          id
        }
      })
  }
}
