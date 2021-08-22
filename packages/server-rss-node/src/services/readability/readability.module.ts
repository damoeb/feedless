import { Module } from '@nestjs/common';
import { ReadabilityService } from './readability.service';
import { MessageBrokerModule } from '../message-broker/message-broker.module';

@Module({
  providers: [ReadabilityService],
  exports: [ReadabilityService],
  imports: [MessageBrokerModule],
})
export class ReadabilityModule {}
