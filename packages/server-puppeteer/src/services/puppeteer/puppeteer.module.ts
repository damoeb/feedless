import { Module } from '@nestjs/common';
import { PuppeteerService } from './puppeteer.service';
import { PuppeteerController } from './puppeteer.controller';

@Module({
  controllers: [PuppeteerController],
  providers: [PuppeteerService],
  exports: [PuppeteerService],
  imports: [],
})
export class PuppeteerModule {}
