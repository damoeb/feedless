import { Module } from '@nestjs/common';
import { FeedService } from './feed.service';
import { PrismaModule } from '../../modules/prisma/prisma.module';
import { RssProxyModule } from '../rss-proxy/rss-proxy.module';
import { CustomFeedResolverModule } from '../custom-feed-resolver/custom-feed-resolver.module';

@Module({
  imports: [PrismaModule, RssProxyModule, CustomFeedResolverModule],
  providers: [FeedService],
  exports: [FeedService],
})
export class FeedModule {}
