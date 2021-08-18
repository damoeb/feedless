import { Module } from '@nestjs/common';
import { CustomFeedResolverService } from './custom-feed-resolver.service';
import { PrismaModule } from '../../modules/prisma/prisma.module';

@Module({
  providers: [CustomFeedResolverService],
  exports: [CustomFeedResolverService],
  imports: [PrismaModule],
})
export class CustomFeedResolverModule {}
