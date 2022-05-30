import { Controller, Get, Logger, Query } from '@nestjs/common';
import { PuppeteerResponse, PuppeteerService } from './puppeteer.service';
import { newCorrId } from '../../libs/corrId';

@Controller()
export class PuppeteerController {
  private readonly logger = new Logger(PuppeteerController.name);

  constructor(
    private readonly puppeteer: PuppeteerService, // private readonly puppeteerCluster: PuppeteerClusterService,
  ) {}

  // http://localhost:3000/api/intern/prerender?url=https://derstandard.at
  @Get('api/intern/prerender')
  async prerenderWebsite(
    @Query('url') url: string,
    @Query('corrId') corrIdParam: string,
    @Query('script') beforeScript: string,
    @Query('optimize') optimizeParam: string,
  ): Promise<PuppeteerResponse> {
    const corrId = corrIdParam || newCorrId();
    const optimize = optimizeParam ? optimizeParam === 'true' : true;
    this.logger.log(
      `[${corrId}] prerenderWebsite ${url} optimize=${optimize} script=${beforeScript!!}`,
    );
    // if (process.env.USE_CLUSER === 'true') {
    // return this.puppeteerCluster.getMarkup(corrId, url, beforeScript, optimize);
    // } else {
    return this.puppeteer.getMarkup(corrId, url, beforeScript, optimize);
    // }
  }
}
