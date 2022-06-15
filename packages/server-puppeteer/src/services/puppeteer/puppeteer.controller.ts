import { Controller, Get, Logger, Query } from '@nestjs/common';
import { PuppeteerResponse, PuppeteerService } from './puppeteer.service';
import { newCorrId } from '../../libs/corrId';

export interface PuppeteerJob {
  corrId: string;
  url: string;
  beforeScript: string;
  optimize: boolean;
  timeoutMillis: number;
}

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
    @Query('timeout') timeoutParam: string,
    @Query('script') beforeScript: string,
    @Query('optimize') optimizeParam: string,
  ): Promise<PuppeteerResponse> {
    const corrId = corrIdParam || newCorrId();
    const timeoutMillis = parseInt(timeoutParam) || 100000;
    const optimize = optimizeParam ? optimizeParam === 'true' : true;
    this.logger.log(
      `[${corrId}] prerenderWebsite ${url} optimize=${optimize} to=${timeoutMillis} script=${beforeScript!!}`,
    );
    const job: PuppeteerJob = {
      corrId,
      url,
      beforeScript,
      optimize,
      timeoutMillis,
    };
    return this.puppeteer.submit(job);
  }
}
