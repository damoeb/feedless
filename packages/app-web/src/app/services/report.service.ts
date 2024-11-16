import { Injectable } from '@angular/core';
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
  constructor(private readonly apollo: ApolloClient<any>) {}

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
