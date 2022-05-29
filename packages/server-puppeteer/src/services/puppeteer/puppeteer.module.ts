import { Module } from '@nestjs/common';
import { PuppeteerService } from './puppeteer.service';
import { PuppeteerController } from './puppeteer.controller';
import { PuppeteerClusterService } from './puppeteer-cluster.service';

@Module({
  controllers: [PuppeteerController],
  providers: [PuppeteerService, PuppeteerClusterService],
  exports: [PuppeteerService, PuppeteerClusterService],
})
export class PuppeteerModule {}
