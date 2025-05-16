import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { PuppeteerService } from '../puppeteer/puppeteer.service';
import * as process from 'process';
import { VerboseConfigService } from '../common/verbose-config.service';
import { GraphqlClient } from '../../graphql-client';
import { ScrapeResponseInput } from '../../generated/graphql';

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
      this.log.error('init', e);
      process.exit(1);
    }
  }

  private init() {
    const graphqlClient = new GraphqlClient(
      this.host(),
      this.useSecure(),
      this.log,
    );
    const email = this.config.getString('APP_EMAIL', {
      fallback: 'admin@localhost',
    });
    const version = this.config.getString('APP_VERSION', { fallback: 'dev' });
    const secretKey = this.config.getString('APP_SECRET_KEY', { mask: 4 });
    graphqlClient.authenticateAgent(email, secretKey, version).subscribe(
      async (event) => {
        this.log.log('incoming event');
        if (event.scrape) {
          this.log.log(`harvestRequest ${JSON.stringify(event)}`);
          try {
            const scrapeResponse = await this.puppeteerService.submit(
              event.scrape as any,
            );
            await graphqlClient.submitJobResponse({
              callbackId: event.callbackId,
              corrId: event.corrId,
              scrapeResponse,
            });
          } catch (e) {
            this.log.error(e?.message);

            const errorResponse: ScrapeResponseInput = {
              ok: false,
              errorMessage: e?.message,
              // url: getHttpGet(event.scrape).url || 'unknown',
              outputs: [],
              logs: [],
              // debug: {
              //   corrId: event.scrape.corrId,
              //   console: [],
              //   cookies: [],
              //   statusCode: 0,
              //   metrics: {
              //     render: Date.now() - startTime,
              //     queue: 0,
              //   },
              //   network: [],
              //   prerendered: true,
              // },
            };
            await graphqlClient.submitJobResponse({
              callbackId: event.callbackId,
              corrId: event.corrId,
              scrapeResponse: errorResponse,
            });
          }
        }
      },
      (error) => {
        this.log.error(error);
        console.log(error);
        // process.exit(1);
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
