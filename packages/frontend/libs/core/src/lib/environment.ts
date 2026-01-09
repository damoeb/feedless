import { GqlVertical } from '@feedless/graphql-api';

export const DEFAULT_FETCH_CRON = '0 0 0 * * *';

export interface Environment {
  production: boolean;
  product: GqlVertical;
  offlineSupport: boolean;
}

/**
 * Shared environment configuration for the feedless frontend.
 * This object is mutable - product and offlineSupport are set at runtime
 * by AppConfigService based on the application configuration.
 */
export const environment: Environment = {
  production: false,
  product: GqlVertical.Feedless,
  offlineSupport: false,
};
