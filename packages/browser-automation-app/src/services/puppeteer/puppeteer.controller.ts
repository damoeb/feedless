import { Controller, Get, Logger, Query } from '@nestjs/common';
import { PuppeteerService } from './puppeteer.service';
import { ScrapeResponseInput, Source } from '../../generated/graphql';
import { newCorrId } from '../../corrId';

// https://www.browserless.io/docs/scrape

@Controller()
export class PuppeteerController {
  private readonly log = new Logger(PuppeteerController.name);

  constructor(
    private readonly puppeteer: PuppeteerService, // private readonly puppeteerCluster: PuppeteerClusterService,
  ) {}

  // http://localhost:3000/puppeteer/render?url=https://derstandard.at
  @Get('puppeteer/render')
  async prerenderWebsite(
    @Query('url') url: string,
    // @Headers('x-corr-id') corrIdParam: string,
    // @Query('options') optionsRaw: string,
  ): Promise<ScrapeResponseInput> {
    const corrId = newCorrId();
    // const options = defaults(
    //   optionsRaw ? (JSON.parse(optionsRaw) as ScrapeOptions) : {},
    //   defaultOptions,
    // );
    // this.logger.log(options)
    this.log.log(`[${corrId}] prerenderWebsite ${url}`);
    const job: Source = {
      id: corrId,
      title: `Prerender ${url}`,
      flow: {
        sequence: [
          {
            fetch: {
              get: {
                url: {
                  literal: url,
                },
              },
            },
          },
        ],
      },
    };
    return this.puppeteer.submit(job);
  }
}
