import { Module } from '@nestjs/common';
import { PluginService } from './plugin.service';
import { PrismaModule } from '../../modules/prisma/prisma.module';
import { MessageBrokerModule } from '../messageBroker/messageBroker.module';

@Module({
  providers: [PluginService],
  exports: [PluginService],
  imports: [PrismaModule, MessageBrokerModule],
})
export class PluginModule {}
