import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { GraphqlClient, AgentEvent, ScrapeRequest } from 'client-lib';
import { PuppeteerService } from '../puppeteer/puppeteer.service';
import { isUndefined } from 'lodash';

export const envValue = (key: string, fallback?: string): string => {
  if (process.env[key]) {
    return process.env[key];
  } else {
    if (!isUndefined(fallback)) {
      return fallback;
    } else {
      throw new Error(`Too few arguments. Requires env variable ${key}`);
    }
  }
};

@Injectable()
export class AgentService implements OnModuleInit {
  private readonly log = new Logger(AgentService.name);

  constructor(private readonly puppeteerService: PuppeteerService) {}

  onModuleInit() {
    this.init();
  }

  private init() {
    this.log.log(
      `Connecting agent host=${this.host()} secure=${this.useSecure()}`,
    );
    const graphqlClient = new GraphqlClient(this.host(), this.useSecure());
    const email = envValue('APP_EMAIL', 'admin@localhost');
    const secretKey = envValue('APP_SECRET_KEY');
    graphqlClient.authenticateAgent(email, secretKey).subscribe(
      async (event) => {
        if (event.scrape) {
          this.log.log(
            `[${event.scrape.corrId}] harvestRequest ${JSON.stringify(event, null, 2)}`,
          );
          try {
            await graphqlClient.submitJobResponse({
              jobId: event.scrape.id,
              corrId: event.scrape.corrId,
              scrapeResponse: await this.puppeteerService.submit(
                event.scrape as any,
              ),
            });
          } catch (e) {
            this.log.error(`[${event.scrape.corrId}] ${e?.message}`);
          }
        }
      },
      (error) => this.log.error(error),
    );
  }

  private useSecure(): boolean {
    return envValue('APP_SECURE', 'false').toLowerCase().trim() === 'true';
  }

  private host(): string {
    const host = envValue('APP_HOST', 'localhost:8080');
    if (host.startsWith('http')) {
      throw new Error(`'Remove protocol from host '${host}'`);
    }
    return host;
  }
}
