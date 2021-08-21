import { Module } from '@nestjs/common';
import { MessageBrokerService } from './messageBroker.service';

@Module({
  providers: [MessageBrokerService],
  exports: [MessageBrokerService],
})
export class MessageBrokerModule {}
