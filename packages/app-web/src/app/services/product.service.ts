import { Injectable } from '@angular/core';
import { GqlListProductsQuery, GqlListProductsQueryVariables, GqlProductsWhereInput, ListProducts } from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { Product } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  constructor(private readonly apollo: ApolloClient<any>) {}

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
