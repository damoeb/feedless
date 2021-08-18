import { Test, TestingModule } from '@nestjs/testing';
import { ReadabilityService } from './readability.service';

describe('ReadabilityService', () => {
  let service: ReadabilityService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ReadabilityService],
    }).compile();

    service = module.get<ReadabilityService>(ReadabilityService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
