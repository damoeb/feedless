import { Test, TestingModule } from '@nestjs/testing';
import { ArticleController } from './article.controller';
import { ReadabilityService } from '../../../services/readability/readability.service';

describe('ArticleController', () => {
  let controller: ArticleController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [ArticleController],
      providers: [ReadabilityService],
    }).compile();

    controller = module.get<ArticleController>(ArticleController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
