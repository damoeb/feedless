import { Injectable, Logger } from '@nestjs/common';
import { GraphqlClient, GqlAgentEvent } from 'client-lib';
import { PuppeteerService } from '../puppeteer/puppeteer.service';
import { PuppeteerWaitUntil } from '../puppeteer/puppeteer.controller';
import { isUndefined } from 'lodash';

@Injectable()
export class AgentService {
  private readonly log = new Logger(AgentService.name);

  constructor(private readonly puppeteerService: PuppeteerService) {
    this.init();
  }

  private init() {
    const graphqlClient = new GraphqlClient(this.host(), this.useSecure());
    const email = this.envValue('EMAIL', 'admin@localhost');
    const secretKey = this.envValue('SECRET_KEY');
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
    return this.envValue('SECURE', 'false').toLowerCase().trim() === 'true';
  }

  private host(): string {
    const host = this.envValue('HOST', 'localhost:8080');
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
