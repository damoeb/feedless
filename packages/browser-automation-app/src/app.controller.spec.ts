import { Test, TestingModule } from '@nestjs/testing';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { SocketSubscriptionModule } from './services/socket-subscription/socket-subscription.module';
import { CommonModule } from './services/common/common.module';

describe('AppController', () => {
  let appController: AppController;

  beforeEach(async () => {
    const app: TestingModule = await Test.createTestingModule({
      controllers: [AppController],
      providers: [AppService],
      imports: [SocketSubscriptionModule, CommonModule],
    }).compile();

    appController = app.get<AppController>(AppController);
  });

  it('should be defined', () => {
    expect(appController).toBeDefined();
  });
});
