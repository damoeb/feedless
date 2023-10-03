import { Controller, Logger, OnModuleInit } from '@nestjs/common';
import { PuppeteerService } from './puppeteer.service';
import { PuppeteerWaitUntil, ScrapeEmitType, ScrapeRequest } from 'client-lib';

// https://www.browserless.io/docs/scrape

@Controller()
export class PuppeteerController implements OnModuleInit {
  private readonly log = new Logger(PuppeteerController.name);

  constructor(
    private readonly puppeteer: PuppeteerService, // private readonly puppeteerCluster: PuppeteerClusterService,
  ) {}

  async onModuleInit() {
    if (process.env.NODE_ENV !== 'test') {
      await this.validatePuppeteer();
    }
  }

  private async validatePuppeteer() {
    try {
      const job: ScrapeRequest = {
        id: '',
        corrId: '-',
        page: {
          url: 'https://feedless.org',
          actions: [],
          prerender: {
            waitUntil: PuppeteerWaitUntil.Load,
            viewport: {
              isMobile: true,
              width: 1024,
              height: 768,
            },
          },
        },
        elements: ['/'],
        emit: [
          ScrapeEmitType.Markup,
          ScrapeEmitType.Pixel,
          ScrapeEmitType.Text,
        ],
        debug: {
          html: true,
          screenshot: true,
          console: true,
          network: true,
          cookies: true,
        },
      };
      await this.puppeteer.submit(job);
      this.log.log('puppeteer ok');
    } catch (e) {
      this.log.warn(`Self-test failed: ${e.message}`);
      // process.exit(1);
    }
  }

  // http://localhost:3000/api/intern/prerender?url=https://derstandard.at
  // @Get('api/intern/prerender')
  // async prerenderWebsite(
  //   @Query('url') url: string,
  //   @Headers('x-corr-id') corrIdParam: string,
  //   @Query('options') optionsRaw: string,
  // ): Promise<PuppeteerResponse> {
  //   const corrId = corrIdParam || newCorrId();
  //   const options = defaults(
  //     optionsRaw ? (JSON.parse(optionsRaw) as ScrapeOptions) : {},
  //     defaultOptions,
  //   );
  //   // this.logger.log(options)
  //   this.log.log(
  //     `[${corrId}] prerenderWebsite ${url} script=${options.prerenderScript!!}`,
  //   );
  //   const job: GqlScrapeRequest = {
  //     corrId,
  //     url,
  //     options,
  //   };
  //   return this.puppeteer.submit(job);
  // }
}
