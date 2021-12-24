import { Module } from '@nestjs/common';
import { MessageBrokerService } from './message-broker.service';
import { ConfigModule } from '@nestjs/config';

@Module({
  imports: [ConfigModule],
  providers: [MessageBrokerService],
  exports: [MessageBrokerService],
})
export class MessageBrokerModule {}
