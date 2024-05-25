import { Injectable } from '@angular/core';
import { CreateUser, GqlCreateUserInput, GqlCreateUserMutation, GqlCreateUserMutationVariables } from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private readonly apollo: ApolloClient<any>) {}

  async createUser(
    data: GqlCreateUserInput,
  ): Promise<GqlCreateUserMutation['createUser']> {
    return this.apollo
      .mutate<GqlCreateUserMutation, GqlCreateUserMutationVariables>({
        mutation: CreateUser,
        variables: {
          data,
        },
      })
      .then((r) => r.data.createUser);
  }
}
