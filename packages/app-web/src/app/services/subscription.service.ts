import { Injectable } from '@angular/core';
import {
  CreateImporters, CreateSourceSubscriptions,
  DeleteImporter,
  GqlCreateImportersMutation,
  GqlCreateImportersMutationVariables, GqlCreateSourceSubscriptionsMutation, GqlCreateSourceSubscriptionsMutationVariables,
  GqlDeleteImporterMutation,
  GqlDeleteImporterMutationVariables,
  GqlImporterByIdQuery,
  GqlImporterByIdQueryVariables,
  GqlImporterInput,
  GqlImportersCreateInput,
  GqlImportersInput,
  GqlImportersQuery,
  GqlImportersQueryVariables,
  GqlImporterUpdateInput, GqlSourceSubscriptionsCreateInput,
  GqlUpdateImporterMutation,
  GqlUpdateImporterMutationVariables,
  ImporterById,
  Importers,
  UpdateImporter
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { BasicImporter, Importer, Pagination, SourceSubscription } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  createSubscriptions(data: GqlSourceSubscriptionsCreateInput): Promise<SourceSubscription[]> {
    return this.apollo
      .mutate<GqlCreateSourceSubscriptionsMutation, GqlCreateSourceSubscriptionsMutationVariables>({
        mutation: CreateSourceSubscriptions,
        variables: {
          data,
        },
      })
      .then((response) => response.data.createSourceSubscriptions);
  }

}
