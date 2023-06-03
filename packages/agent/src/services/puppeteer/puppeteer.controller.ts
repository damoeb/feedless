import {
  Controller,
  Get,
  Headers,
  Logger,
  OnModuleInit,
  Query,
} from '@nestjs/common';
import { PuppeteerResponse, PuppeteerService } from './puppeteer.service';
import { newCorrId } from '../../corrId';
import { GqlHarvestEmitType } from 'client-lib';
import { defaults } from 'lodash';

export interface PuppeteerJob {
  corrId: string;
  url: string;
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
  emit: GqlHarvestEmitType;
  baseXpath: String;
}

const defaultOptions: PuppeteerOptions = {
  emit: GqlHarvestEmitType.Pixel,
  baseXpath: '/',
  prerenderScript: '',
  prerenderWaitUntil: PuppeteerWaitUntil.load,
};

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
      const job: PuppeteerJob = {
        corrId: '-',
        url: 'https://feedless.org',
        options: {
          prerenderWaitUntil: PuppeteerWaitUntil.load,
          prerenderScript: '',
          emit: GqlHarvestEmitType.Text,
          baseXpath: '',
        },
      };
      await this.puppeteer.submit(job);
      this.log.log('puppeteer ok');
    } catch (e) {
      this.log.error(`Selftest failed: ${e.message}`);
      process.exit(1);
    }
  }

  // http://localhost:3000/api/intern/prerender?url=https://derstandard.at
  @Get('api/intern/prerender')
  async prerenderWebsite(
    @Query('url') url: string,
    @Headers('x-corr-id') corrIdParam: string,
    @Query('options') optionsRaw: string,
  ): Promise<PuppeteerResponse> {
    const corrId = corrIdParam || newCorrId();
    const options = defaults(
      optionsRaw ? (JSON.parse(optionsRaw) as PuppeteerOptions) : {},
      defaultOptions,
    );
    // this.logger.log(options)
    this.log.log(
      `[${corrId}] prerenderWebsite ${url} script=${options.prerenderScript!!}`,
    );
    const job: PuppeteerJob = {
      corrId,
      url,
      options,
    };
    return this.puppeteer.submit(job);
  }
}
