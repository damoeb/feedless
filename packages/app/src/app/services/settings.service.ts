import { Injectable } from '@angular/core';
import {
  DeleteBucket, ExportOpml,
  GqlDeleteBucketMutation,
  GqlDeleteBucketMutationVariables, GqlExportOpmlMutation, GqlExportOpmlMutationVariables, GqlExportOpmlResponse, GqlImportOpmlInput,
  GqlImportOpmlMutation,
  GqlImportOpmlMutationVariables, ImportOpml
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

@Injectable({
  providedIn: 'root',
})
export class SettingsService {
  private corrId = 'A2F4';

  constructor(private readonly apollo: ApolloClient<any>) {}

  getCorrId(): string {
    return this.corrId;
  }

  useFulltext(): boolean {
    return true;
  }

  importOpml(data: GqlImportOpmlInput) {
    return this.apollo.mutate<GqlImportOpmlMutation,
      GqlImportOpmlMutationVariables>({
      mutation: ImportOpml,
      variables: {
        data
      },
    }).then(response => response.data.importOpml);
  }

  exportOpml(): Promise<Pick<GqlExportOpmlResponse, 'data'>> {
    return this.apollo.mutate<
      GqlExportOpmlMutation,
      GqlExportOpmlMutationVariables
      >({
      mutation: ExportOpml,
    }).then(response => response.data.exportOpml);
  }
}
