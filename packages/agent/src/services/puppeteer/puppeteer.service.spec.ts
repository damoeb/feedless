import { Test, TestingModule } from '@nestjs/testing';
import { PuppeteerModule } from './puppeteer.module';
import { PuppeteerService } from './puppeteer.service';

describe('PuppeteerService', () => {
  let service: PuppeteerService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [PuppeteerModule],
    }).compile();

    service = module.get<PuppeteerService>(PuppeteerService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
