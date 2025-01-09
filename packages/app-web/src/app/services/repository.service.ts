import { inject, Injectable } from '@angular/core';
import {
  CountRepositories,
  CreateRepositories,
  DeleteRepository,
  GqlCountRepositoriesInput,
  GqlCountRepositoriesQuery,
  GqlCountRepositoriesQueryVariables,
  GqlCreateRepositoriesMutation,
  GqlCreateRepositoriesMutationVariables,
  GqlCursor,
  GqlDeleteRepositoryMutation,
  GqlDeleteRepositoryMutationVariables,
  GqlListPublicRepositoriesQuery,
  GqlListPublicRepositoriesQueryVariables,
  GqlListRepositoriesQuery,
  GqlListRepositoriesQueryVariables,
  GqlRepositoriesInput,
  GqlRepositoryByIdQuery,
  GqlRepositoryByIdQueryVariables,
  GqlRepositoryCreateInput,
  GqlRepositoryUniqueWhereInput,
  GqlRepositoryUpdateInput,
  GqlSourceInput,
  GqlSourceOrderByInput,
  GqlSourcesByRepositoryQuery,
  GqlSourcesByRepositoryQueryVariables,
  GqlSourcesWhereInput,
  GqlSourcesWithFlowByRepositoryQuery,
  GqlSourcesWithFlowByRepositoryQueryVariables,
  GqlUpdateRepositoryMutation,
  GqlUpdateRepositoryMutationVariables,
  ListPublicRepositories,
  ListRepositories,
  RepositoryById,
  SourcesByRepository,
  SourcesWithFlowByRepository,
  UpdateRepository,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import {
  PublicRepository,
  RepositoryFull,
  RepositoryWithFrequency,
  SourceFull,
} from '../graphql/types';
import { SessionService } from './session.service';
import { Router } from '@angular/router';
import { zenToRx } from './agent.service';
import { Observable, of, switchMap } from 'rxjs';
import { AuthService } from './auth.service';
import dayjs from 'dayjs';
import { ArrayElement } from '../types';

export type Source = ArrayElement<RepositoryFull['sources']>;

@Injectable({
  providedIn: 'root',
})
export class RepositoryService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly sessionService = inject(SessionService);

  async createRepositories(
    data: GqlRepositoryCreateInput[],
  ): Promise<RepositoryWithFrequency[]> {
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
        .then((response) => response.data!.createRepositories!);
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

  async downloadRepositories(
    repositories: RepositoryFull[],
    fileName: string | null = null,
  ) {
    const a = window.document.createElement('a');
    a.href = window.URL.createObjectURL(
      new Blob(
        [
          JSON.stringify(
            repositories.map((it) =>
              this.getRepositoryInputWithSourcesAndFlow(it),
            ),
            null,
            2,
          ),
        ],
        {
          type: 'application/json',
        },
      ),
    );
    a.download =
      fileName || `feedless-backup-${dayjs().format('YYYY-MM-DD')}.json`;

    document.body.appendChild(a);
    a.click();

    document.body.removeChild(a);
  }

  updateRepository(data: GqlRepositoryUpdateInput): Promise<void> {
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
      .then();
  }

  listRepositories(
    data: GqlRepositoriesInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<RepositoryWithFrequency[]> {
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

  listPublicRepositories(
    data: GqlRepositoriesInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<PublicRepository[]> {
    return this.apollo
      .query<
        GqlListPublicRepositoriesQuery,
        GqlListPublicRepositoriesQueryVariables
      >({
        query: ListPublicRepositories,
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
    cursor: GqlCursor,
    where: GqlSourcesWhereInput,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<RepositoryFull> {
    return this.apollo
      .query<GqlRepositoryByIdQuery, GqlRepositoryByIdQueryVariables>({
        query: RepositoryById,
        fetchPolicy,
        variables: {
          repository: {
            where: {
              id,
            },
          },
          cursor,
          where,
        },
      })
      .then((response) => response.data.repository);
  }

  async getSourcesByRepository(
    id: string,
    cursor: GqlCursor,
    where: GqlSourcesWhereInput,
    order: GqlSourceOrderByInput[],
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<Source[]> {
    return this.apollo
      .query<GqlSourcesByRepositoryQuery, GqlSourcesByRepositoryQueryVariables>(
        {
          query: SourcesByRepository,
          fetchPolicy,
          variables: {
            repository: {
              where: {
                id,
              },
            },
            where,
            order,
            cursor,
          },
        },
      )
      .then((response) => response.data.repository.sources);
  }

  async getSourceFullByRepository(
    repositoryId: string,
    sourceId: string,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<SourceFull> {
    return this.apollo
      .query<
        GqlSourcesWithFlowByRepositoryQuery,
        GqlSourcesWithFlowByRepositoryQueryVariables
      >({
        query: SourcesWithFlowByRepository,
        fetchPolicy,
        variables: {
          repository: {
            where: {
              id: repositoryId,
            },
          },
          where: {
            id: {
              eq: sourceId,
            },
          },
          cursor: {
            page: 0,
            pageSize: 1,
          },
        },
      })
      .then((response) => response.data.repository.sources[0]);
  }

  async getSourcesFullByRepository(
    repositoryId: string,
    cursor: GqlCursor,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<SourceFull[]> {
    return this.apollo
      .query<
        GqlSourcesWithFlowByRepositoryQuery,
        GqlSourcesWithFlowByRepositoryQueryVariables
      >({
        query: SourcesByRepository,
        fetchPolicy,
        variables: {
          repository: {
            where: {
              id: repositoryId,
            },
          },
          cursor,
        },
      })
      .then((response) => response.data.repository.sources);
  }

  public async getRepositoryInputWithSourcesAndFlow(
    repository: RepositoryFull,
  ): Promise<GqlRepositoryCreateInput> {
    const sources: GqlSourceInput[] = [];
    for (let page = 0; ; ) {
      const sourcesPage = await this.getSourcesFullByRepository(repository.id, {
        page,
      });
      if (sourcesPage.length === 0) {
        break;
      }
      sources.push(...sourcesPage.map((it) => this.toSourceInput(it)));
      page++;
    }

    return {
      visibility: repository.visibility,
      product: repository.product,
      sources, // SourceInput
      // segmented: SegmentInput
      title: repository.title,
      description: repository.description,
      retention: removeTypename(repository.retention) as any,
      refreshCron: repository.refreshCron,
      // withShareKey: Boolean
      pushNotificationsMuted: false,
      // sunset: SunSetPolicyInput
      plugins: repository.plugins?.map((p) => removeTypename(p) as any),
      // additionalSinks: repository.[WebhookOrEmailInput!]
    };
  }

  public toSourceInput(source: SourceFull): GqlSourceInput {
    const sourceInput: GqlSourceInput = {
      title: source.title,
      flow: removeTypename(source.flow) as any,
      tags: source.tags,
    };
    if (source.latLng) {
      sourceInput.latLng = removeTypename(source.latLng) as any;
    }
    return sourceInput;
  }
}

type AnyObject = {
  [key: string]: any;
};

export function removeTypename(obj: AnyObject): AnyObject {
  // If obj is not an object or array, return it as is
  if (typeof obj !== 'object' || obj === null) {
    return obj;
  }

  // If obj is an array, map over each item and apply removeTypename
  if (Array.isArray(obj)) {
    return obj.map((item) => removeTypename(item));
  }

  // Create a new object without the __typename property
  const newObj: AnyObject = {};
  for (const key in obj) {
    if (key !== '__typename' && obj[key] != null && obj[key] != undefined) {
      newObj[key] = removeTypename(obj[key]);
    }
  }
  return newObj;
}
