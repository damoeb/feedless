import { Test, TestingModule } from '@nestjs/testing';
import { RichJsonService } from './opml.service';
import { RichJsonModule } from './opml.module';

describe('OpmlService', () => {
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
