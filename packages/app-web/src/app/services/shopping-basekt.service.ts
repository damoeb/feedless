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
export class ShoppingBasketService {
  constructor() {}
}
