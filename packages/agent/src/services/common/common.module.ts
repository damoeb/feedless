import { Module } from '@nestjs/common';
import { PrefixLoggerService } from './prefix-logger.service';
import { PuppeteerModule } from '../puppeteer/puppeteer.module';

@Module({
  providers: [PrefixLoggerService],
  exports: [PrefixLoggerService],
  imports: [PuppeteerModule],
})
export class CommonModule {}
