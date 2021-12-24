import { Module } from '@nestjs/common';
import { FeedService } from './feed.service';
import { PrismaModule } from '../../modules/prisma/prisma.module';
import { HttpModule } from '@nestjs/axios';

@Module({
  imports: [PrismaModule, HttpModule],
  providers: [FeedService],
  exports: [FeedService],
})
export class FeedModule {}
