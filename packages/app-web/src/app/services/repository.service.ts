import { Injectable } from '@angular/core';
import {
  CreateRepositories,
  DeleteRepository,
  GqlCreateRepositoriesMutation,
  GqlCreateRepositoriesMutationVariables,
  GqlDeleteRepositoryMutation,
  GqlDeleteRepositoryMutationVariables,
  GqlFeatureName,
  GqlListRepositoriesQuery,
  GqlListRepositoriesQueryVariables,
  GqlRepositoryByIdQuery,
  GqlRepositoryByIdQueryVariables,
  GqlRepositoryCreateInput,
  GqlRepositoriesInput,
  GqlRepositoryUniqueWhereInput,
  GqlRepositoryUpdateInput,
  GqlUpdateRepositoryMutation,
  GqlUpdateRepositoryMutationVariables,
  ListRepositories,
  RepositoryById,
  UpdateRepository,
  GqlRepositoriesCreateInput,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { Repository } from '../graphql/types';
import { ServerSettingsService } from './server-settings.service';
import { SessionService } from './session.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class RepositoryService {
  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly serverSetting: ServerSettingsService,
    private readonly router: Router,
    private readonly profileService: SessionService,
  ) {}

  async createRepositories(
    data: GqlRepositoriesCreateInput,
  ): Promise<Repository[]> {
    if (
      this.profileService.isAuthenticated() ||
      this.serverSetting.isEnabled(GqlFeatureName.CanCreateAsAnonymous)
    ) {
      return this.apollo
        .mutate<
          GqlCreateRepositoriesMutation,
          GqlCreateRepositoriesMutationVariables
        >({
          mutation: CreateRepositories,
          variables: {
            data,
          },
        })
        .then((response) => response.data.createRepositories);
    } else {
      // todo mag handle
      // if (this.serverSetting.isEnabled(GqlFeatureName.HasWaitList) && !this.serverSetting.isEnabled(GqlFeatureName.CanSignUp)) {
      if (this.serverSetting.isEnabled(GqlFeatureName.CanSignUp)) {
        await this.router.navigateByUrl('/login');
      } else {
        if (this.serverSetting.isEnabled(GqlFeatureName.HasWaitList)) {
          await this.router.navigateByUrl('/join');
        }
      }
    }
  }

  deleteRepository(data: GqlRepositoryUniqueWhereInput): Promise<void> {
    return this.apollo
      .mutate<
        GqlDeleteRepositoryMutation,
        GqlDeleteRepositoryMutationVariables
      >({
        mutation: DeleteRepository,
        variables: {
          data,
        },
      })
      .then();
  }

  updateRepository(data: GqlRepositoryUpdateInput): Promise<Repository> {
    return this.apollo
      .mutate<
        GqlUpdateRepositoryMutation,
        GqlUpdateRepositoryMutationVariables
      >({
        mutation: UpdateRepository,
        variables: {
          data,
        },
      })
      .then((response) => response.data.updateRepository);
  }

  listRepositoriess(
    data: GqlRepositoriesInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<Repository[]> {
    return this.apollo
      .query<GqlListRepositoriesQuery, GqlListRepositoriesQueryVariables>({
        query: ListRepositories,
        variables: {
          data,
        },
        fetchPolicy,
      })
      .then((response) => response.data.repositories);
  }

  async getRepositoryById(
    id: string,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<Repository> {
    return this.apollo
      .query<GqlRepositoryByIdQuery, GqlRepositoryByIdQueryVariables>({
        query: RepositoryById,
        fetchPolicy,
        variables: {
          data: {
            where: {
              id,
            },
          },
        },
      })
      .then((response) => response.data.repository);
  }
}
