import { inject, Injectable } from '@angular/core';
import {
  GqlUpdateLicenseInput,
  GqlUpdateLicenseMutation,
  GqlUpdateLicenseMutationVariables,
  LocalizedLicense,
  UpdateLicense,
} from '@feedless/graphql-api';
import { ApolloClient } from '@apollo/client/core';
import { ReplaySubject } from 'rxjs';
import { ServerConfigService } from './server-config.service';

@Injectable({
  providedIn: 'root',
})
export class LicenseService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);
  private readonly serverConfig = inject(ServerConfigService);

  public licenseChange = new ReplaySubject<LocalizedLicense>();

  constructor() {
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
        this.serverConfig.setLicense(response.data!.updateLicense);
        this.licenseChange.next(response.data!.updateLicense);
      });
  }
}
