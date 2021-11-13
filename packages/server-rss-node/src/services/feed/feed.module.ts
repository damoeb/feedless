import { Module } from '@nestjs/common';
import { FeedService } from './feed.service';
import { PrismaModule } from '../../modules/prisma/prisma.module';

@Module({
  imports: [PrismaModule],
  providers: [FeedService],
  exports: [FeedService],
})
export class FeedModule {}
