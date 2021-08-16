import { Test, TestingModule } from '@nestjs/testing';
import { OpmlService } from './opml.service';

describe('OpmlService', () => {
  let service: OpmlService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [OpmlService],
    }).compile();

    service = module.get<OpmlService>(OpmlService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
