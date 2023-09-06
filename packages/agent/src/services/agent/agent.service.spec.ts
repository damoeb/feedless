import { Test, TestingModule } from '@nestjs/testing';
import { AgentService } from './agent.service';
import { AgentModule } from './agent.module';

describe('AgentService', () => {
  let service: AgentService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [AgentModule],
    }).compile();

    service = module.get<AgentService>(AgentService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
