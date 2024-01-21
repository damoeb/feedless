import { AppEnvironment } from '../app/app.module';
import { GqlProduct } from '../generated/graphql';

export const environment: AppEnvironment = {
  production: true,
  product: () => {
    switch (location.host) {
      // todo
      default:
        return GqlProduct.Feedless;
    }
  },
};
