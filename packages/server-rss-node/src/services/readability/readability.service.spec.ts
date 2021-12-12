import { Test, TestingModule } from '@nestjs/testing';
import { ReadabilityService } from './readability.service';
import { ReadabilityModule } from './readability.module';

describe('ReadabilityService', () => {
  let service: ReadabilityService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [ReadabilityModule],
    }).compile();

    service = module.get<ReadabilityService>(ReadabilityService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
