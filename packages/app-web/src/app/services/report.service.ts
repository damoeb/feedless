import { Injectable, inject } from '@angular/core';
import { ApolloClient } from '@apollo/client/core';
import {
  CreateReport,
  GqlCreateReportMutation,
  GqlCreateReportMutationVariables,
  GqlSegmentInput,
} from '../../generated/graphql';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);


  async createReport(
    repositoryId: string,
    segmentation: GqlSegmentInput,
  ): Promise<void> {
    return this.apollo
      .mutate<GqlCreateReportMutation, GqlCreateReportMutationVariables>({
        mutation: CreateReport,
        variables: {
          repositoryId,
          segmentation,
        },
      })
      .then();
  }
}
