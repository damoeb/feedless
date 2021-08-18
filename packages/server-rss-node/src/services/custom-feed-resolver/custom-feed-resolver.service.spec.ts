import { Test, TestingModule } from '@nestjs/testing';
import { CustomFeedResolverService } from './custom-feed-resolver.service';
import { CustomFeedResolverModule } from './custom-feed-resolver.module';

describe('CustomFeedResolverService', () => {
  let service: CustomFeedResolverService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [CustomFeedResolverModule],
    }).compile();

    service = module.get<CustomFeedResolverService>(CustomFeedResolverService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
