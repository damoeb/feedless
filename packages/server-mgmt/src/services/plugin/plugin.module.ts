import { Module } from '@nestjs/common';
import { PluginService } from './plugin.service';
import { MessageBrokerModule } from '../message-broker/message-broker.module';

@Module({
  providers: [PluginService],
  exports: [PluginService],
  imports: [MessageBrokerModule],
})
export class PluginModule {}
