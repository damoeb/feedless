import {
  ApolloClient,
  ApolloLink,
  HttpLink,
  InMemoryCache,
} from '@apollo/client/core';
import {
  GqlAgentEvent,
  GqlRegisterAgentSubscription,
  GqlRegisterAgentSubscriptionVariables,
  GqlSubmitAgentDataInput,
  GqlSubmitAgentJobDataMutation,
  GqlSubmitAgentJobDataMutationVariables,
  RegisterAgent,
  SubmitAgentJobData,
} from './generated/graphql';
import { WebSocket } from 'ws';
import fetch from 'node-fetch';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { isUndefined } from 'lodash';
import { Observable } from '@apollo/client';
import { AuthenticatedClient } from './cli/authenticated-client';

// should be a builder
export class GraphqlClient {
  private subscriptionClient: ApolloClient<any>;
  private httpClient: ApolloClient<any>;

  authenticateAgent(): Observable<GqlAgentEvent> {
    const secretKey = this.envValue('SECRET_KEY');
    const email = this.envValue('EMAIL', 'admin@localhost');

    console.log(`host: ${this.host()}`);
    console.log(`email: ${email}`);
    console.log(`secretKey: ${secretKey.substring(0, 4)}****`);

    this.createSubscriptionClient();
    return this.subscribeAgent(email, secretKey);
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

  private useSecure(): boolean {
    return this.envValue('SECURE', 'false').toLowerCase().trim() === 'true';
  }

  private host(): string {
    const host = this.envValue('HOST', 'localhost:8080');
    if (host.startsWith('http')) {
      throw new Error(`'Remove protocol from host '${host}'`);
    }
    return host;
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

  private createSubscriptionClient(): void {
    this.subscriptionClient = new ApolloClient<any>({
      link: ApolloLink.from([
        new GraphQLWsLink(
          createClient({
            webSocketImpl: WebSocket,
            url: this.useSecure()
              ? `wss://${this.host()}/subscriptions`
              : `ws://${this.host()}/subscriptions`,
            keepAlive: 10000,
            retryWait: () =>
              new Promise((resolve) => setTimeout(resolve, 30000)),
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
          uri: this.useSecure()
            ? `https://${this.host()}/graphql`
            : `http://${this.host()}/graphql`,
          fetch,
          headers: {
            Authentication: `Bearer ${token}`,
            'x-corr-id': '1234',
          },
        }),
      ]),
      cache: new InMemoryCache(),
    });
  }

  private envValue(key: string, fallback?: string): string {
    if (process.env[key]) {
      return process.env[key];
    } else {
      if (!isUndefined(fallback)) {
        return fallback;
      } else {
        throw new Error(`Too few arguments. Requires env variable ${key}`);
      }
    }
  }

  token(token: string) {
    if (!token) {
      throw new Error('Too few arguments. token expected.');
    }
    this.createHttpClient(token);
    return new AuthenticatedClient(this.httpClient);
  }
}
