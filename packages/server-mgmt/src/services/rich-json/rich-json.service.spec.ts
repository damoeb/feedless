import { Test, TestingModule } from '@nestjs/testing';
import { RichJsonService } from './rich-json.service';
import { RichJsonModule } from './rich-json.module';

describe(RichJsonService, () => {
  let service: RichJsonService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [RichJsonModule],
    }).compile();

    service = module.get<RichJsonService>(RichJsonService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
