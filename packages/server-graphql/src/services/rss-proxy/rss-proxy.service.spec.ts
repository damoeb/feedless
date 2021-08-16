import { Test, TestingModule } from '@nestjs/testing';
import { RssProxyService } from './rss-proxy.service';

describe('RssProxyService', () => {
  let service: RssProxyService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [RssProxyService],
    }).compile();

    service = module.get<RssProxyService>(RssProxyService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
