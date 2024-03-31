import { Injectable } from '@angular/core';
import {
  GqlLicenseQuery,
  GqlLicenseQueryVariables,
  GqlUpdateLicenseInput,
  GqlUpdateLicenseMutation,
  GqlUpdateLicenseMutationVariables,
  License,
  UpdateLicense
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { ReplaySubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LicenseService {
  public licenseChange = new ReplaySubject<GqlLicenseQuery['license']>();

  constructor(private readonly apollo: ApolloClient<any>) {
    this.initialize();
  }

  private initialize() {
    this.apollo
      .query<GqlLicenseQuery, GqlLicenseQueryVariables>({
        query: License,
      })
      .then((response) => this.licenseChange.next(response.data.license));
  }

  updateLicense(data: GqlUpdateLicenseInput) {
    this.apollo
      .mutate<GqlUpdateLicenseMutation, GqlUpdateLicenseMutationVariables>({
        mutation: UpdateLicense,
        variables: {
          data,
        },
      })
      .then((response) => this.licenseChange.next(response.data.updateLicense));
  }
}
