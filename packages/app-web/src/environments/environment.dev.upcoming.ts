import { AppEnvironment } from '../app/app.module';
import { GqlProductName } from '../generated/graphql';

export const environment: AppEnvironment = {
  production: false,
  product: () => GqlProductName.Upcoming,
};
