import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { GraphqlClient, GqlAgentEvent } from 'client-lib';
import { PuppeteerService } from '../puppeteer/puppeteer.service';
import { PuppeteerWaitUntil } from '../puppeteer/puppeteer.controller';
import { isUndefined } from 'lodash';

@Injectable()
export class AgentService implements OnModuleInit {
  private readonly log = new Logger(AgentService.name);

  constructor(private readonly puppeteerService: PuppeteerService) {}

  onModuleInit() {
    this.init();
  }

  private init() {
    console.log(
      `Connecting agent host=${this.host()} secure=${this.useSecure()}`,
    );
    const graphqlClient = new GraphqlClient(this.host(), this.useSecure());
    const email = this.envValue('APP_EMAIL', 'admin@localhost');
    const secretKey = this.envValue('APP_SECRET_KEY');
    graphqlClient.authenticateAgent(email, secretKey).subscribe(
      async (event: GqlAgentEvent) => {
        if (event.harvestRequest) {
          this.log.log(
            `[${event.harvestRequest.corrId}] harvestRequest ${JSON.stringify(
              event,
            )}`,
          );
          try {
            const response = await this.puppeteerService.submit({
              corrId: event.harvestRequest.corrId,
              options: {
                prerenderWaitUntil: PuppeteerWaitUntil.load,
                prerenderScript: event.harvestRequest.prerenderScript,
                emit: event.harvestRequest.emit,
                baseXpath: event.harvestRequest.baseXpath,
              },
              url: event.harvestRequest.websiteUrl,
              timeoutMillis: 10000,
            });

            await graphqlClient.submitJobResponse({
              corrId: event.harvestRequest.corrId,
              jobId: event.harvestRequest.id,
              harvestResponse: {
                url: response.effectiveUrl,
                dataBase64: response.dataBase64,
                dataAscii: response.dataAscii,
                errorMessage: response.errorMessage,
                isError: response.isError,
              },
            });
          } catch (e) {
            this.log.error(`[${event.harvestRequest.corrId}] ${e?.message}`);
          }
        }
      },
      (error) => this.log.error(error),
    );
  }

  private useSecure(): boolean {
    return this.envValue('APP_SECURE', 'false').toLowerCase().trim() === 'true';
  }

  private host(): string {
    const host = this.envValue('APP_HOST', 'localhost:8080');
    if (host.startsWith('http')) {
      throw new Error(`'Remove protocol from host '${host}'`);
    }
    return host;
  }

  envValue(key: string, fallback?: string): string {
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
}
