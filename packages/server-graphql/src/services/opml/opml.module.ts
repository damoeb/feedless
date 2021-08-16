import { Module } from '@nestjs/common';
import { OpmlService } from './opml.service';
import { FeedModule } from '../feed/feed.module';
import { PrismaModule } from '../../modules/prisma/prisma.module';

@Module({
  imports: [FeedModule, PrismaModule],
  providers: [OpmlService],
  exports: [OpmlService],
})
export class OpmlModule {}
