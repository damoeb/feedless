import { inject, Injectable } from '@angular/core';
import {
  Annotation,
  CreateAnnotation,
  DeleteAnnotation,
  GqlCreateAnnotationInput,
  GqlCreateAnnotationMutation,
  GqlCreateAnnotationMutationVariables,
  GqlDeleteAnnotationInput,
  GqlDeleteAnnotationMutation,
  GqlDeleteAnnotationMutationVariables,
} from '@feedless/graphql-api';
import { ApolloClient } from '@apollo/client/core';
import { SessionService } from './session.service';
import { Router } from '@angular/router';
import { Nullable } from '@feedless/core';

@Injectable({
  providedIn: 'root',
})
export class AnnotationService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);
  private readonly router = inject(Router);
  private readonly sessionService = inject(SessionService);

  async createAnnotation(
    data: GqlCreateAnnotationInput,
  ): Promise<Nullable<Annotation>> {
    if (this.sessionService.isAuthenticated()) {
      return this.apollo
        .mutate<
          GqlCreateAnnotationMutation,
          GqlCreateAnnotationMutationVariables
        >({
          mutation: CreateAnnotation,
          variables: {
            data,
          },
        })
        .then((response) => response.data!.createAnnotation);
    } else {
      await this.router.navigateByUrl('/login');
      return null;
    }
  }

  deleteAnnotation(data: GqlDeleteAnnotationInput): Promise<void> {
    return this.apollo
      .mutate<
        GqlDeleteAnnotationMutation,
        GqlDeleteAnnotationMutationVariables
      >({
        mutation: DeleteAnnotation,
        variables: {
          data,
        },
      })
      .then();
  }
}
