import { Test, TestingModule } from '@nestjs/testing';
import { StatsService } from './stats.service';
import { StatsModule } from './stats.module';

describe('StatsService', () => {
  let service: StatsService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [StatsModule],
    }).compile();

    service = module.get<StatsService>(StatsService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
