import { Injectable } from '@angular/core';
import {
  GqlUpdateLicenseInput,
  GqlUpdateLicenseMutation,
  GqlUpdateLicenseMutationVariables,
  UpdateLicense
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { ReplaySubject } from 'rxjs';
import { ServerConfigService } from './server-config.service';
import { LocalizedLicense } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class LicenseService {
  public licenseChange = new ReplaySubject<LocalizedLicense>();

  constructor(private readonly apollo: ApolloClient<any>,
              private readonly serverConfig: ServerConfigService) {
    if (this.serverConfig.isSelfHosted()) {
      this.initialize();
    }
  }

  private initialize() {
    this.licenseChange.next(this.serverConfig.getLicense());
  }

  updateLicense(data: GqlUpdateLicenseInput) {
    this.apollo
      .mutate<GqlUpdateLicenseMutation, GqlUpdateLicenseMutationVariables>({
        mutation: UpdateLicense,
        variables: {
          data,
        },
      })
      .then((response) => {
        this.serverConfig.setLicense(response.data.updateLicense)
        this.licenseChange.next(response.data.updateLicense);
      });
  }
}
