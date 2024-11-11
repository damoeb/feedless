import { Injectable } from '@angular/core';
import {
  CreateAnnotation,
  DeleteAnnotation,
  GqlCreateAnnotationInput,
  GqlCreateAnnotationMutation,
  GqlCreateAnnotationMutationVariables,
  GqlDeleteAnnotationInput,
  GqlDeleteAnnotationMutation,
  GqlDeleteAnnotationMutationVariables,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { SessionService } from './session.service';
import { Router } from '@angular/router';
import { Annotation } from '../graphql/types';
import { Nullable } from '../types';

@Injectable({
  providedIn: 'root',
})
export class AnnotationService {
  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly router: Router,
    private readonly sessionService: SessionService,
  ) {}

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
