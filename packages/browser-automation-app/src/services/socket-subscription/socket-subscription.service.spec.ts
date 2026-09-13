import { Test, TestingModule } from '@nestjs/testing';
import { SocketSubscriptionService } from './socket-subscription.service';
import { SocketSubscriptionModule } from './socket-subscription.module';

describe('AgentService', () => {
  let service: SocketSubscriptionService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [SocketSubscriptionModule],
    }).compile();

    service = module.get<SocketSubscriptionService>(SocketSubscriptionService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
