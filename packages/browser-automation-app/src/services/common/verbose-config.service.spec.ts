import { Test, TestingModule } from '@nestjs/testing';
import { CommonModule } from './common.module';
import { VerboseConfigService } from './verbose-config.service';

describe('VerboseConfigService', () => {
  let service: VerboseConfigService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [CommonModule],
    }).compile();

    service = module.get<VerboseConfigService>(VerboseConfigService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
