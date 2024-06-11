import { Injectable } from '@angular/core';
import {
  CountRepositories,
  CreateRepositories,
  DeleteRepository,
  GqlCountRepositoriesInput,
  GqlCountRepositoriesQuery,
  GqlCountRepositoriesQueryVariables,
  GqlCreateRepositoriesMutation,
  GqlCreateRepositoriesMutationVariables,
  GqlDeleteRepositoryMutation,
  GqlDeleteRepositoryMutationVariables,
  GqlFeatureName,
  GqlListRepositoriesQuery,
  GqlListRepositoriesQueryVariables,
  GqlRepositoriesCreateInput,
  GqlRepositoriesInput,
  GqlRepositoryByIdQuery,
  GqlRepositoryByIdQueryVariables,
  GqlRepositoryUniqueWhereInput,
  GqlRepositoryUpdateInput,
  GqlUpdateRepositoryMutation,
  GqlUpdateRepositoryMutationVariables,
  ListRepositories,
  RepositoryById,
  UpdateRepository,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { Repository } from '../graphql/types';
import { ServerConfigService } from './server-config.service';
import { SessionService } from './session.service';
import { Router } from '@angular/router';
import { zenToRx } from './agent.service';
import { Observable, of, switchMap } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class RepositoryService {
  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly serverConfig: ServerConfigService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly sessionService: SessionService,
  ) {}

  async createRepositories(
    data: GqlRepositoriesCreateInput,
  ): Promise<Repository[]> {
    if (this.sessionService.isAuthenticated()) {
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
      await this.router.navigateByUrl('/login');
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

  listRepositories(
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

  countRepositories(
    data: GqlCountRepositoriesInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Observable<number> {
    return this.authService.authorizationChange().pipe(
      switchMap((authentication) => {
        if (authentication?.loggedIn) {
          return zenToRx(
            this.apollo
              .watchQuery<
                GqlCountRepositoriesQuery,
                GqlCountRepositoriesQueryVariables
              >({
                query: CountRepositories,
                variables: {
                  data,
                },
                fetchPolicy,
              })
              .map((response) => response.data.countRepositories),
          );
        } else {
          return of(0);
        }
      }),
    );
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
