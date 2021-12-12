import { Test, TestingModule } from '@nestjs/testing';
import { PluginService } from './plugin.service';
import { PluginModule } from './plugin.module';

describe('PluginService', () => {
  let service: PluginService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [PluginModule],
    }).compile();

    service = module.get<PluginService>(PluginService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
