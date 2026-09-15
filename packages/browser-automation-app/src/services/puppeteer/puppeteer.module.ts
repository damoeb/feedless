import { Module } from '@nestjs/common';
import { PuppeteerService } from './puppeteer.service';
import { PuppeteerController } from './puppeteer.controller';
import { CommonModule } from '../common/common.module';

@Module({
  controllers: [PuppeteerController],
  providers: [PuppeteerService],
  exports: [PuppeteerService],
  imports: [CommonModule],
})
export class PuppeteerModule {}
