import { Module } from '@nestjs/common';
import { PluginService } from './plugin.service';
import { PrismaModule } from '../../modules/prisma/prisma.module';
import { EventsModule } from '../events/events.module';

@Module({
  providers: [PluginService],
  exports: [PluginService],
  imports: [PrismaModule, EventsModule],
})
export class PluginModule {}
