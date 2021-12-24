import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { RichJsonService } from './rich-json.service';
import { FeedModule } from '../feed/feed.module';
import { PrismaModule } from '../../modules/prisma/prisma.module';

@Module({
  imports: [FeedModule, PrismaModule, HttpModule],
  providers: [RichJsonService],
  exports: [RichJsonService],
})
export class RichJsonModule {}
