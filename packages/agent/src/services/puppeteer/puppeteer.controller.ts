import { Controller, Get, Logger, Query, Headers } from '@nestjs/common';
import { PuppeteerResponse, PuppeteerService } from './puppeteer.service';
import { newCorrId } from '../../libs/corrId';

export interface PuppeteerJob {
  corrId: string;
  url: string;
  timeoutMillis: number;
  options: PuppeteerOptions;
}

export enum PuppeteerWaitUntil {
  networkidle0 = 'networkidle0',
  networkidle2 = 'networkidle2',
  load = 'load',
  domcontentloaded = 'domcontentloaded',
}

export interface PuppeteerOptions {
  prerenderScript: string;
  prerenderWaitUntil: PuppeteerWaitUntil;
  prerenderWithoutMedia: boolean;
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
    @Headers('x-corr-id') corrIdParam: string,
    @Query('timeout') timeoutParam: string,
    @Query('options') optionsRaw: string,
  ): Promise<PuppeteerResponse> {
    const corrId = corrIdParam || newCorrId();
    const timeoutMillis = this.puppeteer.handleTimeoutParam(timeoutParam);
    const options = JSON.parse(optionsRaw) as PuppeteerOptions;
    this.logger.log(
      `[${corrId}] prerenderWebsite ${url} optimize=${
        options.prerenderWithoutMedia
      } to=${timeoutParam} -> ${timeoutMillis} script=${options.prerenderScript!!}`,
    );
    const job: PuppeteerJob = {
      corrId,
      url,
      timeoutMillis,
      options,
    };
    return this.puppeteer.submit(job);
  }
}
