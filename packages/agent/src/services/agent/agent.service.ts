import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { GraphqlClient } from 'client-lib';
import { PuppeteerService } from '../puppeteer/puppeteer.service';
import { ScrapeResponseInput } from 'client-lib/dist/generated/graphql';
import * as process from 'process';
import { VerboseConfigService } from '../common/verbose-config.service';

@Injectable()
export class AgentService implements OnModuleInit {
  private readonly log = new Logger(AgentService.name);

  constructor(
    private readonly puppeteerService: PuppeteerService,
    private readonly config: VerboseConfigService,
  ) {}

  onModuleInit() {
    try {
      this.init();
    } catch (e) {
      this.log.error(e);
      process.exit(1);
    }
  }

  private init() {
    const graphqlClient = new GraphqlClient(this.host(), this.useSecure());
    const email = this.config.getString('APP_EMAIL', {
      fallback: 'admin@localhost',
    });
    const version = this.config.getOrThrow('APP_VERSION');
    const secretKey = this.config.getString('APP_SECRET_KEY', { mask: 4 });
    graphqlClient.authenticateAgent(email, secretKey, version).subscribe(
      async (event) => {
        if (event.scrape) {
          const startTime = Date.now();
          this.log.log(
            `[${event.scrape.corrId}] harvestRequest ${JSON.stringify(
              event,
              null,
              2,
            )}`,
          );
          try {
            const scrapeResponse = await this.puppeteerService.submit(
              event.scrape as any,
            );
            await graphqlClient.submitJobResponse({
              jobId: event.scrape.id,
              corrId: event.scrape.corrId,
              scrapeResponse,
            });
          } catch (e) {
            this.log.error(`[${event.scrape.corrId}] ${e?.message}`);

            const errorResponse: ScrapeResponseInput = {
              failed: true,
              errorMessage: e?.message,
              url: null,
              elements: [],
              debug: {
                corrId: event.scrape.corrId,
                console: [],
                cookies: [],
                statusCode: 0,
                metrics: {
                  render: Date.now() - startTime,
                  queue: 0,
                },
                network: [],
                prerendered: true,
              },
            };
            await graphqlClient.submitJobResponse({
              jobId: event.scrape.id,
              corrId: event.scrape.corrId,
              scrapeResponse: errorResponse,
            });
          }
        }
      },
      (error) => {
        this.log.error(error);
        process.exit(1);
      },
    );
  }

  private useSecure(): boolean {
    return this.config.getBoolean('APP_SECURE');
  }

  private host(): string {
    const host = this.config.getString('APP_HOST', {
      fallback: 'localhost:8080',
    });
    if (host.startsWith('http')) {
      throw new Error(`'Remove protocol from host '${host}'`);
    }
    return host;
  }
}
