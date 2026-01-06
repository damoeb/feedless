import { inject, Injectable } from '@angular/core';
import {
  GqlListProductsQuery,
  GqlListProductsQueryVariables,
  GqlProductsWhereInput,
  ListProducts,
  Product,
} from '@feedless/graphql-api';
import { ApolloClient } from '@apollo/client/core';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);

  listProducts(data: GqlProductsWhereInput): Promise<Product[]> {
    return this.apollo
      .query<GqlListProductsQuery, GqlListProductsQueryVariables>({
        query: ListProducts,
        variables: {
          data,
        },
        // fetchPolicy,
      })
      .then((response) => response.data.products);
  }
}
