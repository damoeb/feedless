import { Injectable } from '@angular/core';
import {
  GqlRemoteNativeFeedInput,
  GqlRemoteNativeFeedQuery,
  GqlRemoteNativeFeedQueryVariables,
  RemoteNativeFeed,
} from '../../generated/graphql';
import { ApolloClient } from '@apollo/client/core';
import { RemoteFeed, RemoteFeedItem } from '../graphql/types';

@Injectable({
  providedIn: 'root',
})
export class ApolloAbortControllerService extends AbortController {

}
