import { Injectable } from '@angular/core';
import {
  ExportOpml,
  GqlExportOpmlMutation,
  GqlExportOpmlMutationVariables,
  GqlExportOpmlResponse,
  GqlImportOpmlInput,
  GqlImportOpmlMutation,
  GqlImportOpmlMutationVariables,
  ImportOpml,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

@Injectable({
  providedIn: 'root',
})
export class OpmlService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  importOpml(data: GqlImportOpmlInput) {
    return this.apollo
      .mutate<GqlImportOpmlMutation, GqlImportOpmlMutationVariables>({
        mutation: ImportOpml,
        variables: {
          data,
        },
      })
      .then((response) => response.data.importOpml);
  }

  exportOpml(): Promise<Pick<GqlExportOpmlResponse, 'data'>> {
    return this.apollo
      .mutate<GqlExportOpmlMutation, GqlExportOpmlMutationVariables>({
        mutation: ExportOpml,
      })
      .then((response) => response.data.exportOpml);
  }
}
