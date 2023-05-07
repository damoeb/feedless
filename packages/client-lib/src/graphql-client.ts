import {
  ApolloClient,
  ApolloLink,
  HttpLink,
  InMemoryCache,
} from '@apollo/client/core';
import {
  Articles,
  Feeds,
  GqlAgentEvent,
  GqlArticlesInput,
  GqlArticlesQuery,
  GqlArticlesQueryVariables,
  GqlAuthentication,
  GqlBucketsOrNativeFeedsInput,
  GqlFeedsQuery,
  GqlFeedsQueryVariables,
  GqlRegisterAgentSubscription,
  GqlRegisterAgentSubscriptionVariables,
  GqlRegisterCliSubscription,
  GqlRegisterCliSubscriptionVariables,
  GqlSubmitAgentDataInput,
  GqlSubmitAgentJobDataMutation,
  GqlSubmitAgentJobDataMutationVariables,
  RegisterAgent,
  RegisterCli,
  SubmitAgentJobData
} from './generated/graphql';
import { WebSocket } from 'ws';
import * as nodeFetch from 'node-fetch';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { isUndefined } from 'lodash';
import { Observable } from '@apollo/client';
import { ClientOptions } from 'graphql-ws/lib/client';

export type Authentication = Pick<GqlAuthentication, 'token' | 'corrId'>;

// should be a builder
export class GraphqlClient {
  private subscriptionClient: ApolloClient<any>;
  private httpClient: ApolloClient<any>;

  constructor(private readonly host: string,
              private readonly useSsl: boolean) {
  }

  authenticateAgent(email: string, secretKey: string): Observable<GqlAgentEvent> {
    // console.log(`host: ${this.host}`);
    // console.log(`email: ${email}`);
    // console.log(`secretKey: ${secretKey.substring(0, 4)}****`);

    this.createSubscriptionClient({
      keepAlive: 10000,
      retryWait: () =>
        new Promise((resolve) => setTimeout(resolve, 30000)),
    });
    return this.subscribeAgent(email, secretKey);
  }

  authenticateCli(): Promise<Authentication> {
    this.createSubscriptionClient();
    return this.subscribeCliAuth();
  }

  authenticateCliWithToken(token: string): void {
    this.createHttpClient(token)
  }

  submitJobResponse(data: GqlSubmitAgentDataInput) {
    return this.httpClient.mutate<
      GqlSubmitAgentJobDataMutation,
      GqlSubmitAgentJobDataMutationVariables
    >({
      mutation: SubmitAgentJobData,
      variables: {
        data,
      },
    });
  }

  articles(data: GqlArticlesInput) {
    return this.httpClient.query<
      GqlArticlesQuery,
      GqlArticlesQueryVariables
    >({
      query: Articles,
      variables: {
        data,
      },
    });
  }

  feeds(data: GqlBucketsOrNativeFeedsInput) {
    return this.httpClient.query<
      GqlFeedsQuery,
      GqlFeedsQueryVariables
    >({
      query: Feeds,
      variables: {
        data,
      },
    });
  }

  private subscribeAgent(
    email: string,
    secretKey: string,
  ): Observable<GqlAgentEvent> {
    return this.subscriptionClient
      .subscribe<
        GqlRegisterAgentSubscription,
        GqlRegisterAgentSubscriptionVariables
      >({
        query: RegisterAgent,
        variables: {
          data: {
            secretKey: {
              email,
              secretKey,
            },
          },
        },
      })
      .map((response) => response.data.registerAgent)
      .filter((event: GqlAgentEvent) => {
        if (event.authentication) {
          console.log('Connected');
          this.createHttpClient(event.authentication.token);
          return false;
        } else {
          return true;
        }
      });
  }

  private subscribeCliAuth(): Promise<Authentication> {
    return new Promise<Authentication>((resolve, reject) => {
      setTimeout(() => reject(new Error('Authentication attempt timed out')), 1000 * 60 * 4);
      this.subscriptionClient
        .subscribe<
          GqlRegisterCliSubscription,
          GqlRegisterCliSubscriptionVariables
          >({
          query: RegisterCli,
        })
        // .subscribe(console.log)
        .subscribe((response) => {
          const event = response.data.registerCli;
          if (event.authentication) {
            this.createHttpClient(event.authentication.token);
            resolve(event.authentication);
          } else {
            console.log(event.message.message)
          }
        }, reject, console.log)
    });
  }

  private createSubscriptionClient(options: Partial<ClientOptions> = {}): void {
    this.subscriptionClient = new ApolloClient<any>({
      link: ApolloLink.from([
        new GraphQLWsLink(
          createClient({
            ...options,
            webSocketImpl: WebSocket,
            url: this.useSsl
              ? `wss://${this.host}/subscriptions`
              : `ws://${this.host}/subscriptions`,
            on: {
              error: (err: any) => {
                console.error(err);
                process.exit(1);
              },
            },
          }),
        ),
      ]),
      cache: new InMemoryCache(),
    });
  }

  private createHttpClient(token: string) {
    this.httpClient = new ApolloClient<any>({
      link: ApolloLink.from([
        new HttpLink({
          uri: this.useSsl
            ? `https://${this.host}/graphql`
            : `http://${this.host}/graphql`,
          fetch: nodeFetch as any,
          headers: {
            Authentication: `Bearer ${token}`,
            'x-corr-id': '1234',
          },
        }),
      ]),
      cache: new InMemoryCache(),
    });
  }
}
