import { GqlRemoteNativeFeed, GqlSourceInput, GqlTransientGenericFeed, RepositoryWithFrequency } from '@feedless/graphql-api';

/**
 * Shared types to avoid circular dependency (TransformWebsiteToFeedComponent, ModalProvider, etc.).
 */
export interface NativeOrGenericFeed {
  genericFeed?: GqlTransientGenericFeed;
  nativeFeed?: GqlRemoteNativeFeed;
}

export enum FeedBuilderModalComponentExitRole {
  dismiss = 'dismiss',
  login = 'login',
}

export type FeedWithRequest = {
  source: GqlSourceInput;
  feed: NativeOrGenericFeed;
  refine: boolean;
};

export type FeedOrRepository = {
  feed: FeedWithRequest;
  repository: RepositoryWithFrequency;
};
