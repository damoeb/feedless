import { Injectable, Logger } from '@nestjs/common';
import {
  ApolloClient,
  ApolloLink,
  HttpLink,
  InMemoryCache,
} from '@apollo/client/core';
import {
  GqlHarvestRequest,
  GqlRegisterAgentSubscription,
  GqlRegisterAgentSubscriptionVariables,
  GqlSubmitAgentJobDataMutation,
  GqlSubmitAgentJobDataMutationVariables,
  RegisterAgent,
  SubmitAgentJobData,
} from '../../generated/graphql';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { WebSocket } from 'ws';
import fetch from 'node-fetch';
import {
  PuppeteerResponse,
  PuppeteerService,
} from '../puppeteer/puppeteer.service';
import { PuppeteerWaitUntil } from '../puppeteer/puppeteer.controller';
import { isUndefined } from 'lodash';

@Injectable()
export class AgentService {
  private readonly log = new Logger(AgentService.name);
  private subscriptionClient: ApolloClient<any>;
  private mutationClient: ApolloClient<any>;

  constructor(private readonly puppeteerService: PuppeteerService) {
    this.init();
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

  private async init() {
    const secretKey = this.envValue('SECRET_KEY');
    const email = this.envValue('EMAIL', 'admin@localhost');

    this.log.log(`host: ${this.host()}`);
    this.log.log(`email: ${email}`);
    this.log.log(`secretKey: ${secretKey.substring(0, 4)}****`);

    this.createSubscriptionClient();
    this.subscribeAgent(email, secretKey);
  }

  private subscribeAgent(email: string, secretKey: string) {
    this.subscriptionClient
      .subscribe<
        GqlRegisterAgentSubscription,
        GqlRegisterAgentSubscriptionVariables
      >({
        query: RegisterAgent,
        variables: {
          data: {
            email,
            secretKey,
          },
        },
      })
      .subscribe(
        async (response) => {
          const data = response.data.registerAgent;
          if (data.authentication) {
            console.log('Connected');
            this.createHttpClient(data.authentication.token);
          }
          if (data.harvestRequest) {
            try {
              const response = await this.puppeteerService.submit({
                corrId: data.harvestRequest.corrId,
                options: {
                  prerenderWaitUntil: PuppeteerWaitUntil.load,
                  prerenderScript: data.harvestRequest.prerenderScript,
                  prerenderWithoutMedia: false,
                },
                url: data.harvestRequest.websiteUrl,
                timeoutMillis: 10000,
              });

              await this.submitJobResponse(data.harvestRequest, response);
            } catch (e) {
              this.log.error(e);
            }
          }
        },
        (error) => this.log.error(error),
      );
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
                this.log.error(err?.message);
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
    this.mutationClient = new ApolloClient<any>({
      link: ApolloLink.from([
        new HttpLink({
          uri: this.useSecure()
            ? `https://${this.host()}/graphql`
            : `http://${this.host()}/graphql`,
          fetch,
          headers: {
            Authentication: `Bearer ${token}`,
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

  private submitJobResponse(
    request: Pick<
      GqlHarvestRequest,
      | 'id'
      | 'corrId'
      | 'websiteUrl'
      | 'prerenderWaitUntil'
      | 'prerender'
      | 'prerenderScript'
    >,
    response: PuppeteerResponse,
  ) {
    return this.mutationClient.mutate<
      GqlSubmitAgentJobDataMutation,
      GqlSubmitAgentJobDataMutationVariables
    >({
      mutation: SubmitAgentJobData,
      variables: {
        data: {
          jobId: request.id,
          corrId: request.corrId,
          harvestResponse: {
            url: response.effectiveUrl,
            html: response.html,
            errorMessage: response.errorMessage,
            isError: response.isError,
          },
        },
      },
    });
  }
}
