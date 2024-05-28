import { AppEnvironment } from '../app/app.module';
import { GqlProductCategory } from '../generated/graphql';

export const environment: AppEnvironment = {
  production: true,
  offlineSupport: false,
  product: GqlProductCategory.Feedless,
  officialFeedlessUrl: 'https://feedless.org'
};
