import { Module } from '@nestjs/common';
import { FeedService } from './feed.service';
import { PrismaModule } from '../../modules/prisma/prisma.module';
import { RssProxyModule } from '../rss-proxy/rss-proxy.module';

@Module({
  imports: [PrismaModule, RssProxyModule],
  providers: [FeedService],
  exports: [FeedService],
})
export class FeedModule {}
