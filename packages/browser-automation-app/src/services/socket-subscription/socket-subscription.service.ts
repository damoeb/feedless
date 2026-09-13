import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { PuppeteerService } from '../puppeteer/puppeteer.service';
import * as process from 'process';
import { VerboseConfigService } from '../common/verbose-config.service';
import { GraphqlClient } from '../../graphql-client';
import { ScrapeResponseInput } from '../../generated/graphql';

@Injectable()
export class SocketSubscriptionService implements OnModuleInit {
  private readonly log = new Logger(SocketSubscriptionService.name);
  private isConnected = false;

  constructor(
    private readonly puppeteerService: PuppeteerService,
    private readonly config: VerboseConfigService,
  ) {}

  onModuleInit() {
    if (!this.config.getBoolean('APP_DISABLE_SOCKET_SUBSCRIPTION')) {
      try {
        this.initSubscription();
      } catch (e) {
        this.log.error('init', e);
        process.exit(1);
      }
    }
  }

  private initSubscription() {
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
        if (!this.isConnected) {
          this.isConnected = true;
          this.log.log('Socket subscription connected');
        }
        this.log.debug('incoming event');
        if (event.scrape) {
          this.log.debug(`harvestRequest ${JSON.stringify(event)}`);
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
        this.isConnected = false;
        this.log.error(error);
        console.log(error);
        // process.exit(1);
      },
    );
  }

  isSocketConnected(): boolean {
    return this.isConnected;
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
