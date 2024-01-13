import { ApolloClient, ApolloLink, HttpLink, InMemoryCache } from '@apollo/client/core';
import {
  AgentEvent,
  Authentication,
  RegisterAgent,
  RegisterAgentSubscription,
  RegisterAgentSubscriptionVariables,
  RegisterCli,
  RegisterCliSubscription,
  RegisterCliSubscriptionVariables,
  SubmitAgentDataInput,
  SubmitAgentJobData,
  SubmitAgentJobDataMutation,
  SubmitAgentJobDataMutationVariables
} from './generated/graphql';
import { WebSocket } from 'ws';
import * as nodeFetch from 'node-fetch';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { Observable } from '@apollo/client';
import { ClientOptions } from 'graphql-ws/lib/client';
import { arch, platform } from 'os';

// should be a builder
export class GraphqlClient {
  private subscriptionClient: ApolloClient<any>;
  private httpClient: ApolloClient<any>;

  constructor(private readonly host: string,
              private readonly useSsl: boolean) {
  }

  authenticateAgent(email: string, secretKey: string, version: string): Observable<AgentEvent> {
    // console.log(`host: ${this.host}`);
    // console.log(`email: ${email}`);
    // console.log(`secretKey: ${secretKey.substring(0, 4)}****`);

    this.createSubscriptionClient({
      keepAlive: 10000,
      retryWait: () =>
        new Promise((resolve) => setTimeout(resolve, 30000)),
    });
    const connectionId = (Math.random() + 1).toString(36).substring(7);
    return this.subscribeAgent(email, secretKey, version, connectionId);
  }

  authenticateCliWithToken(token: string): void {
    this.createHttpClient(token)
  }

  submitJobResponse(data: SubmitAgentDataInput) {
    return this.httpClient.mutate<
      SubmitAgentJobDataMutation,
      SubmitAgentJobDataMutationVariables
    >({
      mutation: SubmitAgentJobData,
      variables: {
        data,
      },
    });
  }

  private subscribeAgent(
    email: string,
    secretKey: string,
    version: string,
    connectionId: string,
  ): Observable<AgentEvent> {
    return this.subscriptionClient
      .subscribe<
        RegisterAgentSubscription,
        RegisterAgentSubscriptionVariables
      >({
        query: RegisterAgent,
        variables: {
          data: {
            version,
            connectionId,
            os: {
              arch: arch(),
              platform: platform()
            },
            secretKey: {
              email,
              secretKey,
            },
          },
        },
      })
      .map((response) => response.data.registerAgent)
      .filter((event: AgentEvent) => {
        if (event.authentication) {
          console.log('[graphql-client] Connected');
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
          RegisterCliSubscription,
          RegisterCliSubscriptionVariables
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
            console.log(`[graphql-client] ${event.message.message}`)
          }
        }, reject, console.log)
    });
  }

  private createSubscriptionClient(options: Partial<ClientOptions> = {}): void {
    const url = this.useSsl
      ? `wss://${this.host}/subscriptions`
      : `ws://${this.host}/subscriptions`;
    console.log(`[graphql-client] Subscribing to ${url}`)
    this.subscriptionClient = new ApolloClient<any>({
      link: ApolloLink.from([
        new GraphQLWsLink(
          createClient({
            ...options,
            webSocketImpl: WebSocket,
            url,
            on: {
              error: (err: any) => {
                if (process.env.DEBUG) {
                  console.error(`[graphql-client] ${err}`);
                } else {
                  console.error(`[graphql-client] ${err?.message}`)
                }
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
