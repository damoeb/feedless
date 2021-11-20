import { Module } from '@nestjs/common';
import { RichJsonService } from './rich-json.service';
import { FeedModule } from '../feed/feed.module';
import { PrismaModule } from '../../modules/prisma/prisma.module';

@Module({
  imports: [FeedModule, PrismaModule],
  providers: [RichJsonService],
  exports: [RichJsonService],
})
export class RichJsonModule {}
