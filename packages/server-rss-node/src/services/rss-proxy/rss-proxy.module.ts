import { Module } from '@nestjs/common';
import { RssProxyService } from './rss-proxy.service';

@Module({
  providers: [RssProxyService],
  exports: [RssProxyService],
})
export class RssProxyModule {}
