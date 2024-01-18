import { AppEnvironment } from '../app/app.module';
import { GqlProduct } from '../generated/graphql';

export const environment: AppEnvironment = {
  production: false,
  product: () => GqlProduct.PageChangeTracker,
};
