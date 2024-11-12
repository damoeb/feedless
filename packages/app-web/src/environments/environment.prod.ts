import { AppEnvironment } from '../app/app.module';
import { GqlVertical } from '../generated/graphql';

export const environment: AppEnvironment = {
  production: true,
  offlineSupport: false,
  product: GqlVertical.Feedless,
  officialFeedlessUrl: 'https://feedless.org',
};
