import { Injectable } from '@angular/core';
import {
  CreateUser,
  CreateUserSecret,
  DeleteUserSecrets, GqlCreateUserInput, GqlCreateUserMutation, GqlCreateUserMutationVariables,
  GqlCreateUserSecretMutation,
  GqlCreateUserSecretMutationVariables,
  GqlDeleteUserSecretsInput,
  GqlDeleteUserSecretsMutation,
  GqlDeleteUserSecretsMutationVariables,
  GqlLogoutMutation,
  GqlLogoutMutationVariables,
  GqlProfileQuery,
  GqlProfileQueryVariables,
  GqlUpdateCurrentUserInput,
  GqlUpdateCurrentUserMutation,
  GqlUpdateCurrentUserMutationVariables, GqlUser,
  Logout,
  Profile as ProfileQuery,
  UpdateCurrentUser
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { Profile, UserSecret } from '../graphql/types';
import { BehaviorSubject, filter, Observable, ReplaySubject } from 'rxjs';
import { isNull, isUndefined } from 'lodash-es';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(
    private readonly apollo: ApolloClient<any>,
  ) {
  }

  async createUser(data: GqlCreateUserInput): Promise<Pick<GqlUser, "id">> {
    return this.apollo
      .mutate<
        GqlCreateUserMutation,
        GqlCreateUserMutationVariables
      >({
        mutation: CreateUser,
        variables: {
          data
        }
      })
      .then((r) => r.data.createUser);
  }

}
