import {
  ApolloClient,
  ApolloLink,
  HttpLink,
  InMemoryCache,
} from '@apollo/client/core';
import { ErrorEvent, WebSocket } from 'ws';
import * as nodeFetch from 'node-fetch-commonjs';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { ClientOptions, createClient } from 'graphql-ws';
import { Observable } from '@apollo/client';
import { arch, platform } from 'os';
import {
  AgentEvent,
  RegisterAgent,
  RegisterAgentSubscription,
  RegisterAgentSubscriptionVariables,
  SubmitAgentDataInput,
  SubmitAgentJobData,
  SubmitAgentJobDataMutation,
  SubmitAgentJobDataMutationVariables,
} from './generated/graphql';
import { Logger } from '@nestjs/common';

// should be a builder
export class GraphqlClient {
  private subscriptionClient: ApolloClient<any>;
  private httpClient: ApolloClient<any>;

  constructor(
    private readonly host: string,
    private readonly useSsl: boolean,
    private readonly log: Logger,
  ) {}

  authenticateAgent(
    email: string,
    secretKey: string,
    version: string,
  ): Observable<AgentEvent> {
    // console.log(`host: ${this.host}`);
    // console.log(`email: ${email}`);
    // console.log(`secretKey: ${secretKey.substring(0, 4)}****`);

    this.createSubscriptionClient({
      // keepAlive: 10000,
      retryWait: () => new Promise((resolve) => setTimeout(resolve, 3000)),
    });
    const connectionId = (Math.random() + 1).toString(36).substring(7);
    const agentName = `${process.env.APP_AGENT_NAME || 'unnamed'}`;
    return this.subscribeAgent(
      agentName,
      email,
      secretKey,
      version,
      connectionId,
    );
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
    agentName: string,
    email: string,
    secretKey: string,
    version: string,
    connectionId: string,
  ): Observable<AgentEvent> {
    return this.subscriptionClient
      .subscribe<RegisterAgentSubscription, RegisterAgentSubscriptionVariables>(
        {
          query: RegisterAgent,
          variables: {
            data: {
              name: agentName,
              version,
              connectionId,
              os: {
                arch: arch(),
                platform: platform(),
              },
              secretKey: {
                email,
                secretKey,
              },
            },
          },
        },
      )
      .map((response) => response.data.registerAgent)
      .filter((event: AgentEvent) => {
        if (event.authentication) {
          this.log.log('[graphql-client] Connected');
          this.createHttpClient(event.authentication.token);
          return false;
        } else {
          return true;
        }
      });
  }

  private createSubscriptionClient(options: Partial<ClientOptions> = {}): void {
    const url = this.useSsl
      ? `wss://${this.host}/subscriptions`
      : `ws://${this.host}/subscriptions`;
    this.log.log(`[graphql-client] Subscribing to ${url}`);
    this.subscriptionClient = new ApolloClient<any>({
      link: ApolloLink.from([
        new GraphQLWsLink(
          createClient({
            ...options,
            webSocketImpl: WebSocket,
            url,
            on: {
              error: (err: ErrorEvent) => {
                this.log.error('graphql ws connection failed', err.error);
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
