import { Test, TestingModule } from '@nestjs/testing';
import { MessageBrokerService } from './message-broker.service';
import { MessageBrokerModule } from './message-broker.module';

describe('MessageBrokerService', () => {
  let service: MessageBrokerService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [MessageBrokerModule],
    }).compile();

    service = module.get<MessageBrokerService>(MessageBrokerService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
