import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '../app-test.module';
import { ServerConfigService } from './server-config.service';

describe('ServerConfigService', () => {
  let service: ServerConfigService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults()],
    });
    service = TestBed.inject(ServerConfigService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
