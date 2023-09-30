import { Test, TestingModule } from '@nestjs/testing';
import { PrefixLoggerService } from './prefix-logger.service';
import { CommonModule } from './common.module';

describe('PrefixLoggerService', () => {
  let service: PrefixLoggerService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [CommonModule],
    }).compile();

    service = module.get<PrefixLoggerService>(PrefixLoggerService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
