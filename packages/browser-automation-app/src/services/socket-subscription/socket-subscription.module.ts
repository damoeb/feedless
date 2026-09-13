import { Module } from '@nestjs/common';
import { SocketSubscriptionService } from './socket-subscription.service';
import { PuppeteerModule } from '../puppeteer/puppeteer.module';
import { CommonModule } from '../common/common.module';

@Module({
  providers: [SocketSubscriptionService],
  exports: [SocketSubscriptionService],
  imports: [PuppeteerModule, CommonModule],
})
export class SocketSubscriptionModule {}
