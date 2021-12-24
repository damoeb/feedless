import { Controller, Get, Logger, Query } from '@nestjs/common';
import { PuppeteerService } from './puppeteer.service';

@Controller()
export class PuppeteerController {
  private readonly logger = new Logger(PuppeteerController.name);

  constructor(private readonly puppeteer: PuppeteerService) {}

  @Get('prerender')
  async prerenderWebsite(
    @Query('url') url: string,
    @Query('correlationId') cid: string,
  ): Promise<string> {
    this.logger.log(`[${cid}] prerenderWebsite ${url}`);
    return this.puppeteer.getMarkup(cid, url);
  }
}
