import { ApolloClient } from '@apollo/client/core';

export function createApolloClientMock(
  query: jasmine.Spy,
  subscribe: jasmine.Spy,
  mutate: jasmine.Spy
): ApolloClient<any> {
  return {
    query,
    subscribe,
    mutate,
  } as any;
}
