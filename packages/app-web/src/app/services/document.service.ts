import { Injectable } from '@angular/core';
import {
  DeleteWebDocumentsById,
  GqlDeleteWebDocumentsByIdMutation,
  GqlDeleteWebDocumentsByIdMutationVariables,
  GqlDeleteWebDocumentsInput,
  GqlWebDocumentByIdsQuery,
  GqlWebDocumentByIdsQueryVariables,
  GqlWebDocumentsInput,
  WebDocumentByIds,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { WebDocument } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class DocumentService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  findAllByStreamId(
    data: GqlWebDocumentsInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<WebDocument[]> {
    return this.apollo
      .query<GqlWebDocumentByIdsQuery, GqlWebDocumentByIdsQueryVariables>({
        query: WebDocumentByIds,
        variables: {
          data,
        },
        fetchPolicy,
      })
      .then((response) => {
        return response.data.webDocuments;
      });
  }

  // findById(id: string): Promise<WebDocument> {
  //   return this.apollo
  //     .query<GqlWebDocumentByIdQuery, GqlWebDocumentByIdQueryVariables>({
  //       query: WebDocumentById,
  //       variables: {
  //         data: {
  //           where: { id },
  //         },
  //       },
  //     })
  //     .then((response) => response.data.webDocument);
  // }
  removeById(data: GqlDeleteWebDocumentsInput) {
    return this.apollo
      .mutate<
        GqlDeleteWebDocumentsByIdMutation,
        GqlDeleteWebDocumentsByIdMutationVariables
      >({
        mutation: DeleteWebDocumentsById,
        variables: {
          data,
        },
      })
      .then((response) => {
        return response.data.deleteWebDocuments;
      });
  }
}
