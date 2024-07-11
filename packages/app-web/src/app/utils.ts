import { GqlHttpFetch, GqlHttpFetchInput } from '../generated/graphql';
import { isDefined } from './types';

export function getFirstFetchUrlLiteral(
  actions: { fetch?: GqlHttpFetch | GqlHttpFetchInput }[],
): string {
  const fetchList = actions.filter((action) => isDefined(action.fetch));
  if (fetchList.length > 0) {
    return (fetchList[0].fetch as GqlHttpFetch).get.url.literal;
  }
}
