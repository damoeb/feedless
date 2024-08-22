import {
  GqlHttpFetch,
  GqlHttpFetchInput,
  GqlPluginExecution,
  GqlPluginExecutionInput,
  GqlSelectors,
} from '../generated/graphql';
import { isDefined } from './types';

export function getFirstFetchUrlLiteral(
  actions: { fetch?: GqlHttpFetch | GqlHttpFetchInput }[],
): string {
  return getFirstFetch(actions)?.get?.url?.literal;
}

export function getFirstFetch(
  actions: { fetch?: GqlHttpFetch | GqlHttpFetchInput }[],
): GqlHttpFetch {
  const fetchList = actions.filter((action) => isDefined(action.fetch));
  if (fetchList.length > 0) {
    return fetchList[0].fetch as GqlHttpFetch;
  }
}

export function getGenericFeedParams(
  actions: { execute?: GqlPluginExecution | GqlPluginExecutionInput }[],
): GqlSelectors {
  if (!actions) {
    return;
  }
  const list = actions.filter((action) => isDefined(action.execute));
  if (list.length > 0) {
    return (list[0].execute as GqlPluginExecution).params?.org_feedless_feed
      ?.generic;
  }
}
