import { Test, TestingModule } from '@nestjs/testing';
import { FeedService } from './feed.service';
import { FeedModule } from './feed.module';

describe('FeedService', () => {
  let service: FeedService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [FeedModule],
    }).compile();

    service = module.get<FeedService>(FeedService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
