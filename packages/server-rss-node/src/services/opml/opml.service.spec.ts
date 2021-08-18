import { Test, TestingModule } from '@nestjs/testing';
import { OpmlService } from './opml.service';
import { OpmlModule } from './opml.module';

describe('OpmlService', () => {
  let service: OpmlService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [OpmlModule],
    }).compile();

    service = module.get<OpmlService>(OpmlService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
