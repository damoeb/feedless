import { Injectable } from '@angular/core';
import {
  CreateImporter,
  DeleteImporter,
  GqlCreateImporterMutation,
  GqlCreateImporterMutationVariables,
  GqlDeleteImporterMutation,
  GqlDeleteImporterMutationVariables, GqlGenericFeed,
  GqlImporter, GqlImporterByIdQuery, GqlImporterByIdQueryVariables,
  GqlImporterCreateInput, GqlImporterWhereInput, GqlNativeFeed, ImporterById, Maybe
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

export type Importer = (
  Pick<GqlImporter, 'id' | 'autoRelease' | 'createdAt' | 'bucketId'>
  & { nativeFeed: (
    Pick<GqlNativeFeed, 'id' | 'createdAt' | 'websiteUrl' | 'title' | 'description' | 'feedUrl' | 'status' | 'lastUpdatedAt' | 'domain'>
    & { genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>> }
    ) }
  );

export type CreateImporterResponse = Pick<GqlImporter, 'id' | 'autoRelease' | 'createdAt' | 'nativeFeedId'>;
@Injectable({
  providedIn: 'root',
})
export class ImporterService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  createImporter(
    data: GqlImporterCreateInput
  ): Promise<CreateImporterResponse> {
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
          importerId: id,
        },
      },
    });
  }

  async getImporter(data: GqlImporterWhereInput): Promise<Importer> {
    return this.apollo.query<
      GqlImporterByIdQuery,
      GqlImporterByIdQueryVariables
    >({
      query: ImporterById,
      variables: {
        data
      },
    }).then(response => response.data.importer as Importer);
  }
}
