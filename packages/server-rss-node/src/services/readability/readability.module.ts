import { Module } from '@nestjs/common';
import { ReadabilityService } from './readability.service';
import { MessageBrokerModule } from '../messageBroker/messageBroker.module';

@Module({
  providers: [ReadabilityService],
  exports: [ReadabilityService],
  imports: [MessageBrokerModule],
})
export class ReadabilityModule {}
