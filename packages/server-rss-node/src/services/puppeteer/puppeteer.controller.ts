import { Controller, Get, Param } from '@nestjs/common';
import { PuppeteerService } from './puppeteer.service';

@Controller()
export class PuppeteerController {
  constructor(private readonly puppeteer: PuppeteerService) {}

  @Get('fetch/:url')
  async getDynamic(@Param('url') url: string): Promise<string> {
    return this.puppeteer.getMarkup(url);
  }
}
