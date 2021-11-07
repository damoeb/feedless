import { Module } from '@nestjs/common';
import { ReadabilityService } from './readability.service';
import { MessageBrokerModule } from '../message-broker/message-broker.module';
import { PuppeteerModule } from '../puppeteer/puppeteer.module';

@Module({
  providers: [ReadabilityService],
  exports: [ReadabilityService],
  imports: [MessageBrokerModule, PuppeteerModule],
})
export class ReadabilityModule {}
