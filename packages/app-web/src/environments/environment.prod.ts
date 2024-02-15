import { AppEnvironment } from '../app/app.module';
import { GqlProductName } from '../generated/graphql';

export const environment: AppEnvironment = {
  production: true,
  offlineSupport: () => false,
  product: () => {
    switch (location.host) {
      // todo
      default:
        return GqlProductName.Feedless;
    }
  },
};
