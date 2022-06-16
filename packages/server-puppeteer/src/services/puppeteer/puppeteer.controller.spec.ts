import { Test, TestingModule } from '@nestjs/testing';
import { PuppeteerModule } from './puppeteer.module';
import { PuppeteerController } from './puppeteer.controller';

describe('PuppeteerController', () => {
  let puppeteerController: PuppeteerController;

  beforeEach(async () => {
    const app: TestingModule = await Test.createTestingModule({
      imports: [PuppeteerModule],
    }).compile();

    puppeteerController = app.get<PuppeteerController>(PuppeteerController);
  });

  it('should be defined', () => {
    expect(puppeteerController).toBeDefined();
  });
});
