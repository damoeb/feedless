import { Controller, Get, Logger, Query } from '@nestjs/common';
import { PuppeteerResponse, PuppeteerService } from './puppeteer.service';
import { newCorrId } from '../../libs/corrId';

@Controller()
export class PuppeteerController {
  private readonly logger = new Logger(PuppeteerController.name);

  constructor(private readonly puppeteer: PuppeteerService) {}

  // http://localhost:3000/api/intern/prerender?url=https://derstandard.at
  @Get('api/intern/prerender')
  async prerenderWebsite(
    @Query('url') url: string,
    @Query('correlationId') cidParam: string,
    @Query('script') beforeScript: string,
    @Query('optimize') optimizeParam: string,
  ): Promise<PuppeteerResponse> {
    const cid = cidParam || newCorrId();
    const optimize = optimizeParam ? optimizeParam === 'true' : true;
    this.logger.log(
      `[${cid}] prerenderWebsite ${url} optimize=${optimize} script=${beforeScript!!}`,
    );
    return this.puppeteer.getMarkup(cid, url, beforeScript, optimize);
  }
}
